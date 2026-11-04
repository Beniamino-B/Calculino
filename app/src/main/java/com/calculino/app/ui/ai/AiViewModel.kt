package com.calculino.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculino.app.data.ai.LocalAiEngine
import com.calculino.app.data.ai.LocalAiGenerationException
import com.calculino.app.data.ai.LocalModelRepository
import com.calculino.app.data.repository.HistoryRepository
import com.calculino.app.domain.ai.AiChatTurn
import com.calculino.app.domain.ai.AiError
import com.calculino.app.domain.ai.AiGenerationState
import com.calculino.app.domain.ai.AiMode
import com.calculino.app.domain.ai.AiPromptBuilder
import com.calculino.app.domain.ai.DownloadStartResult
import com.calculino.app.domain.ai.LocalAiResult
import com.calculino.app.domain.ai.LocalModel
import com.calculino.app.domain.ai.LocalModelState
import com.calculino.app.domain.ai.VerifiedCalculation
import com.calculino.app.domain.math.AngleMode
import com.calculino.app.domain.math.MathEngine
import com.calculino.app.domain.math.MathEvaluationResult
import com.calculino.app.domain.math.MathStepBuilder
import com.calculino.app.domain.math.VerifiedMathSteps
import com.calculino.app.domain.model.HistoryEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiUiState(
    val selectedModelId: String? = null,
    val mode: AiMode = AiMode.Chat,
    val chatInput: String = "",
    val chatMessages: List<AiChatTurn> = emptyList(),
    val chatGeneration: AiGenerationState = AiGenerationState.Idle,
    val explainInput: String = "",
    val explainGeneration: AiGenerationState = AiGenerationState.Idle,
    val currentCalculation: VerifiedCalculation? = null,
    val currentSteps: VerifiedMathSteps? = null,
    val explainInputError: Boolean = false,
    val downloadStartError: DownloadStartResult? = null,
)

class AiViewModel(
    private val modelRepository: LocalModelRepository,
    private val engine: LocalAiEngine,
    private val historyRepository: HistoryRepository,
    private val initialRequest: VerifiedCalculation? = null,
    private val inferenceDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    val isDeviceSupported: Boolean = engine.isDeviceSupported

    val modelStates = modelRepository.observeStates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap<String, LocalModelState>(),
        )

    val history = historyRepository.observeHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<HistoryEntry>(),
        )

    private val mathEngine = MathEngine()
    private val _uiState = MutableStateFlow(AiUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initialRequest?.let { request ->
            _uiState.update {
                it.copy(
                    mode = AiMode.Explain,
                    explainInput = request.expression,
                    currentCalculation = request,
                    currentSteps = buildSteps(request),
                )
            }
        }
    }

    fun selectModel(model: LocalModel) {
        _uiState.update { it.copy(selectedModelId = model.id) }
    }

    fun selectMode(mode: AiMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun updateChatInput(value: String) {
        _uiState.update { it.copy(chatInput = value.take(MAX_CHAT_INPUT)) }
    }

    fun updateExplainInput(value: String) {
        _uiState.update {
            it.copy(
                explainInput = value.take(MAX_EXPLAIN_INPUT),
                currentCalculation = null,
                currentSteps = null,
                explainInputError = false,
                explainGeneration = AiGenerationState.Idle,
            )
        }
    }

    fun selectHistory(entry: HistoryEntry) {
        val calculation = entry.toCalculation()
        _uiState.update {
            it.copy(
                mode = AiMode.Explain,
                explainInput = entry.expression,
                currentCalculation = calculation,
                currentSteps = buildSteps(calculation),
                explainInputError = false,
                explainGeneration = AiGenerationState.Idle,
            )
        }
    }

    fun download(model: LocalModel) {
        viewModelScope.launch {
            val result = runCatching { modelRepository.startDownload(model) }
                .getOrDefault(DownloadStartResult.Unavailable)
            if (result != DownloadStartResult.Started) {
                _uiState.update { it.copy(downloadStartError = result) }
            }
        }
    }

    fun cancelDownload(model: LocalModel) {
        viewModelScope.launch { runCatching { modelRepository.cancelDownload(model) } }
    }

    fun deleteModel(model: LocalModel) {
        viewModelScope.launch { runCatching { modelRepository.deleteModel(model) } }
    }

    fun consumeDownloadStartError() {
        _uiState.update { it.copy(downloadStartError = null) }
    }

    fun sendChat() {
        val state = _uiState.value
        val message = state.chatInput.trim()
        if (message.isEmpty() || state.chatGeneration.isBusy()) return
        val conversation = state.chatMessages + AiChatTurn(fromUser = true, text = message)
        _uiState.update {
            it.copy(
                chatInput = "",
                chatMessages = conversation,
                chatGeneration = AiGenerationState.VerifyingModel,
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(chatGeneration = AiGenerationState.Generating) }
            try {
                var response = ""
                engine.generateResponse(AiPromptBuilder.chat(conversation)).collect { chunk ->
                    response = chunk
                }
                _uiState.update { current ->
                    current.copy(
                        chatMessages = current.chatMessages + AiChatTurn(
                            fromUser = false,
                            text = response,
                        ),
                        chatGeneration = AiGenerationState.Idle,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (generation: LocalAiGenerationException) {
                _uiState.update { current ->
                    current.copy(chatGeneration = AiGenerationState.Failure(generation.error))
                }
            } catch (_: Throwable) {
                _uiState.update { current ->
                    current.copy(chatGeneration = AiGenerationState.Failure(AiError.InferenceFailed))
                }
            }
        }
    }

    fun explain(model: LocalModel) {
        val state = _uiState.value
        if (state.explainGeneration.isBusy()) return
        val normalizedInput = MathStepBuilder.normalize(state.explainInput)
        val calculation = state.currentCalculation?.takeIf { it.expression == normalizedInput }
            ?: evaluateCalculation(
                expression = normalizedInput,
                angleMode = state.currentCalculation?.angleMode ?: AngleMode.Radians,
            )
        if (calculation == null) {
            _uiState.update { it.copy(explainInputError = true) }
            return
        }
        val steps = buildSteps(calculation)
        _uiState.update {
            it.copy(
                currentCalculation = calculation,
                currentSteps = steps,
                explainInputError = false,
                explainGeneration = AiGenerationState.VerifyingModel,
            )
        }
        viewModelScope.launch {
            val result = runGeneration(
                model = model,
                systemPrompt = AiPromptBuilder.EXPLAIN_SYSTEM,
                prompt = AiPromptBuilder.explain(calculation, steps),
                maxTokens = EXPLAIN_MAX_TOKENS,
                temperature = EXPLAIN_TEMPERATURE,
                onState = { generation ->
                    _uiState.update { it.copy(explainGeneration = generation) }
                },
            )
            _uiState.update { current ->
                current.copy(
                    explainGeneration = when (result) {
                        is LocalAiResult.Success -> AiGenerationState.Success(
                            text = result.text,
                            tokensPerSecond = result.tokensPerSecond,
                            model = model,
                        )
                        is LocalAiResult.Failure -> AiGenerationState.Failure(result.error)
                    },
                )
            }
        }
    }

    private suspend fun runGeneration(
        model: LocalModel,
        systemPrompt: String,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        onState: (AiGenerationState) -> Unit,
    ): LocalAiResult = try {
        withContext(inferenceDispatcher) {
            engine.generate(
                model = model,
                systemPrompt = systemPrompt,
                prompt = prompt,
                maxTokens = maxTokens,
                temperature = temperature,
                onVerifying = { onState(AiGenerationState.VerifyingModel) },
                onLoading = { onState(AiGenerationState.LoadingModel) },
                onGenerating = { onState(AiGenerationState.Generating) },
            )
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Throwable) {
        LocalAiResult.Failure(AiError.InferenceFailed)
    }

    private fun evaluateCalculation(
        expression: String,
        angleMode: AngleMode,
    ): VerifiedCalculation? {
        if (expression.isBlank()) return null
        return when (val result = mathEngine.evaluate(expression, angleMode)) {
            is MathEvaluationResult.Success -> VerifiedCalculation(
                expression = expression,
                result = result.display.main,
                approximation = result.display.approximation,
                isExact = result.display.isExact,
                angleMode = angleMode,
            )
            is MathEvaluationResult.Failure -> null
        }
    }

    private fun buildSteps(calculation: VerifiedCalculation): VerifiedMathSteps =
        MathStepBuilder.build(
            expression = calculation.expression,
            fallbackResult = calculation.result,
            fallbackApproximation = calculation.approximation,
            angleMode = calculation.angleMode,
        )

    private fun HistoryEntry.toCalculation(): VerifiedCalculation = VerifiedCalculation(
        expression = expression,
        result = result,
        approximation = approximation,
        isExact = isExact,
        angleMode = angleMode,
    )
}

private fun AiGenerationState.isBusy(): Boolean =
    this is AiGenerationState.VerifyingModel ||
        this is AiGenerationState.LoadingModel ||
        this is AiGenerationState.Generating

private const val MAX_CHAT_INPUT = 1_000
private const val MAX_EXPLAIN_INPUT = 512
private const val EXPLAIN_MAX_TOKENS = 360
private const val EXPLAIN_TEMPERATURE = 0.2f
