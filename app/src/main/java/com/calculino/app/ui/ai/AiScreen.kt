package com.calculino.app.ui.ai

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.AppContainer
import com.calculino.app.R
import com.calculino.app.domain.ai.AiChatTurn
import com.calculino.app.domain.ai.AiError
import com.calculino.app.domain.ai.AiGenerationState
import com.calculino.app.domain.ai.AiMode
import com.calculino.app.domain.ai.DownloadStartResult
import com.calculino.app.domain.ai.LocalModel
import com.calculino.app.domain.ai.LocalModelState
import com.calculino.app.domain.ai.ModelFailure
import com.calculino.app.domain.ai.VerifiedCalculation
import com.calculino.app.domain.math.AngleMode
import com.calculino.app.domain.math.VerifiedMathSteps
import com.calculino.app.domain.model.HistoryEntry
import com.calculino.app.ui.components.ExactnessBadge
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.science.ScienceToolHeader
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import com.calculino.app.ui.theme.PillShape
import java.util.Locale

@Composable
fun AiRoute(
    container: AppContainer,
    request: VerifiedCalculation?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AiViewModel = viewModel(
        initializer = {
            AiViewModel(
                modelRepository = container.localModelRepository,
                engine = container.localAiEngine,
                historyRepository = container.historyRepository,
                initialRequest = request,
            )
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modelStates by viewModel.modelStates.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    AiScreen(
        uiState = uiState,
        modelStates = modelStates,
        history = history,
        isDeviceSupported = viewModel.isDeviceSupported,
        onBack = onBack,
        onModeChange = viewModel::selectMode,
        onChatInputChange = viewModel::updateChatInput,
        onSendChat = viewModel::sendChat,
        onExplainInputChange = viewModel::updateExplainInput,
        onExplain = viewModel::explain,
        onSelectHistory = viewModel::selectHistory,
        onSelectModel = viewModel::selectModel,
        onDownload = viewModel::download,
        onCancelDownload = viewModel::cancelDownload,
        onDeleteModel = viewModel::deleteModel,
        onDownloadErrorConsumed = viewModel::consumeDownloadStartError,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
    uiState: AiUiState,
    modelStates: Map<String, LocalModelState>,
    history: List<HistoryEntry>,
    isDeviceSupported: Boolean,
    onBack: () -> Unit,
    onModeChange: (AiMode) -> Unit,
    onChatInputChange: (String) -> Unit,
    onSendChat: () -> Unit,
    onExplainInputChange: (String) -> Unit,
    onExplain: (LocalModel) -> Unit,
    onSelectHistory: (HistoryEntry) -> Unit,
    onSelectModel: (LocalModel) -> Unit,
    onDownload: (LocalModel) -> Unit,
    onCancelDownload: (LocalModel) -> Unit,
    onDeleteModel: (LocalModel) -> Unit,
    onDownloadErrorConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showModels by rememberSaveable { mutableStateOf(false) }
    val readyModels = LocalModel.entries.filter { model ->
        modelStates[model.id] is LocalModelState.Ready
    }
    val selectedModel = readyModels.firstOrNull { it.id == uiState.selectedModelId }
        ?: readyModels.firstOrNull()
    val busy = uiState.chatGeneration.isBusy() || uiState.explainGeneration.isBusy()
    val insufficientSpaceMessage = stringResource(R.string.ai_download_start_space)
    val unavailableMessage = stringResource(R.string.ai_download_start_unavailable)

    LaunchedEffect(uiState.downloadStartError) {
        when (uiState.downloadStartError) {
            DownloadStartResult.InsufficientSpace -> {
                snackbarHostState.showSnackbar(insufficientSpaceMessage)
                onDownloadErrorConsumed()
            }
            DownloadStartResult.Unavailable -> {
                snackbarHostState.showSnackbar(unavailableMessage)
                onDownloadErrorConsumed()
            }
            DownloadStartResult.Started, null -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        KawaiiBackdrop()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = CalculinoSizes.screenMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CalculinoSpacing.lg)
                .imePadding(),
        ) {
            ScienceToolHeader(
                title = stringResource(R.string.ai_title),
                subtitle = stringResource(R.string.ai_subtitle),
                onBack = onBack,
            )
            TabRow(
                selectedTabIndex = uiState.mode.ordinal,
                modifier = Modifier.padding(top = CalculinoSpacing.sm),
            ) {
                Tab(
                    selected = uiState.mode == AiMode.Chat,
                    onClick = { onModeChange(AiMode.Chat) },
                    text = { Text(stringResource(R.string.ai_mode_chat)) },
                )
                Tab(
                    selected = uiState.mode == AiMode.Explain,
                    onClick = { onModeChange(AiMode.Explain) },
                    text = { Text(stringResource(R.string.ai_mode_explain)) },
                )
            }
            Spacer(modifier = Modifier.height(CalculinoSpacing.sm))
            ModelSelectorButton(
                model = selectedModel,
                onClick = { showModels = true },
            )
            if (!isDeviceSupported) {
                Spacer(modifier = Modifier.height(CalculinoSpacing.sm))
                UnsupportedDeviceCard()
            }
            when (uiState.mode) {
                AiMode.Chat -> ChatContent(
                    messages = uiState.chatMessages,
                    input = uiState.chatInput,
                    generation = uiState.chatGeneration,
                    hasModel = selectedModel != null,
                    isDeviceSupported = isDeviceSupported,
                    busy = busy,
                    onInputChange = onChatInputChange,
                    onSend = {
                        selectedModel?.let { model ->
                            haptic(HapticFeedbackType.TextHandleMove)
                            onSendChat()
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                AiMode.Explain -> ExplainContent(
                    state = uiState,
                    history = history,
                    selectedModel = selectedModel,
                    hasReadyModels = readyModels.isNotEmpty(),
                    isDeviceSupported = isDeviceSupported,
                    busy = busy,
                    onInputChange = onExplainInputChange,
                    onSelectHistory = onSelectHistory,
                    onExplain = {
                        selectedModel?.let { model ->
                            haptic(HapticFeedbackType.TextHandleMove)
                            onExplain(model)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(CalculinoSpacing.lg),
        )
    }

    if (showModels) {
        ModalBottomSheet(
            onDismissRequest = { showModels = false },
            sheetState = sheetState,
        ) {
            ModelsSheet(
                modelStates = modelStates,
                selectedModel = selectedModel,
                isDeviceSupported = isDeviceSupported,
                onDownload = {
                    haptic(HapticFeedbackType.TextHandleMove)
                    onDownload(it)
                },
                onCancelDownload = {
                    haptic(HapticFeedbackType.TextHandleMove)
                    onCancelDownload(it)
                },
                onDeleteModel = {
                    haptic(HapticFeedbackType.TextHandleMove)
                    onDeleteModel(it)
                },
                onSelectModel = {
                    haptic(HapticFeedbackType.TextHandleMove)
                    onSelectModel(it)
                    showModels = false
                },
            )
        }
    }
}

@Composable
private fun VerifiedCalculationCard(request: VerifiedCalculation) {
    val palette = LocalKawaiiPalette.current
    val angleLabel = stringResource(
        if (request.angleMode == AngleMode.Degrees) {
            R.string.calculator_angle_degrees
        } else {
            R.string.calculator_angle_radians
        },
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ai_verified_label),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                ExactnessBadge(isExact = request.isExact)
            }
            Text(
                text = request.expression,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                Text(
                    text = stringResource(
                        if (request.isExact) {
                            R.string.calculator_result_equals
                        } else {
                            R.string.calculator_result_approx
                        },
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.title,
                )
                Text(
                    text = request.result,
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            request.approximation?.let { approximation ->
                Text(
                    text = stringResource(R.string.calculator_approx_detail, approximation),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
            Text(
                text = stringResource(R.string.ai_angle_mode, angleLabel),
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary,
            )
            Text(
                text = stringResource(R.string.ai_verified_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun ModelSelectorButton(
    model: LocalModel?,
    onClick: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    OutlinedButton(
        onClick = onClick,
        shape = PillShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Rounded.SmartToy,
            contentDescription = null,
            tint = palette.title,
            modifier = Modifier.size(CalculinoSizes.iconSmall),
        )
        Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
        Text(
            text = model?.let {
                stringResource(R.string.ai_model_button, modelName(it))
            } ?: stringResource(R.string.ai_model_button_none),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(CalculinoSizes.iconSmall),
        )
    }
}

@Composable
private fun ChatContent(
    messages: List<AiChatTurn>,
    input: String,
    generation: AiGenerationState,
    hasModel: Boolean,
    isDeviceSupported: Boolean,
    busy: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (messages.isEmpty()) {
            EmptyChatCard(
                hasModel = hasModel,
                isDeviceSupported = isDeviceSupported,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = CalculinoSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                itemsIndexed(
                    items = messages,
                    key = { index, _ -> index },
                    contentType = { _, message -> message.fromUser },
                ) { _, message ->
                    ChatBubble(message)
                }
                when (generation) {
                    AiGenerationState.VerifyingModel -> item {
                        ProgressCard(text = stringResource(R.string.ai_verifying))
                    }
                    AiGenerationState.LoadingModel -> item {
                        ProgressCard(text = stringResource(R.string.ai_loading))
                    }
                    AiGenerationState.Generating -> item {
                        ProgressCard(text = stringResource(R.string.ai_generating))
                    }
                    is AiGenerationState.Failure -> item {
                        ErrorCard(message = aiErrorMessage(generation.error))
                    }
                    is AiGenerationState.Success, AiGenerationState.Idle -> Unit
                }
            }
        }
        Spacer(modifier = Modifier.height(CalculinoSpacing.sm))
        ChatInputBar(
            input = input,
            enabled = hasModel && isDeviceSupported,
            busy = busy,
            onInputChange = onInputChange,
            onSend = onSend,
        )
    }
}

@Composable
private fun ChatBubble(message: AiChatTurn) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = if (message.fromUser) palette.keyPlus else palette.surface,
            border = if (message.fromUser) null else BorderStroke(
                CalculinoSizes.hairline,
                palette.border,
            ),
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .then(if (message.fromUser) Modifier else Modifier.kawaiiCard()),
        ) {
            FormattedAiText(
                text = message.text,
                color = if (message.fromUser) palette.onKeyPlus else palette.textPrimary,
                modifier = Modifier.padding(
                    horizontal = CalculinoSpacing.md,
                    vertical = CalculinoSpacing.sm,
                ),
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    enabled: Boolean,
    busy: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.ai_chat_input_hint)) },
            shape = MaterialTheme.shapes.extraLarge,
            minLines = 1,
            maxLines = 4,
        )
        FilledIconButton(
            onClick = onSend,
            enabled = enabled && !busy && input.isNotBlank(),
        ) {
            Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(R.string.ai_chat_send),
            )
        }
    }
}

@Composable
private fun EmptyChatCard(
    hasModel: Boolean,
    isDeviceSupported: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = modifier.kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Rounded.SmartToy,
                contentDescription = null,
                tint = palette.title,
                modifier = Modifier.size(CalculinoSizes.iconLarge),
            )
            Text(
                text = stringResource(R.string.ai_chat_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )
            Text(
                text = if (hasModel) {
                    stringResource(R.string.ai_chat_empty_text)
                } else if (isDeviceSupported) {
                    stringResource(R.string.ai_no_model_hint)
                } else {
                    stringResource(R.string.ai_device_unsupported_text)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun ExplainContent(
    state: AiUiState,
    history: List<HistoryEntry>,
    selectedModel: LocalModel?,
    hasReadyModels: Boolean,
    isDeviceSupported: Boolean,
    busy: Boolean,
    onInputChange: (String) -> Unit,
    onSelectHistory: (HistoryEntry) -> Unit,
    onExplain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = CalculinoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.ai_explain_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary,
        )
        OutlinedTextField(
            value = state.explainInput,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.ai_explain_input_label)) },
            placeholder = { Text(stringResource(R.string.ai_explain_input_hint)) },
            isError = state.explainInputError,
            supportingText = if (state.explainInputError) {
                { Text(stringResource(R.string.ai_explain_input_error)) }
            } else {
                null
            },
            shape = MaterialTheme.shapes.extraLarge,
            minLines = 1,
            maxLines = 3,
        )
        if (!hasReadyModels) {
            Text(
                text = stringResource(R.string.ai_no_model_hint),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
        Button(
            shape = PillShape,
            onClick = onExplain,
            enabled = isDeviceSupported && selectedModel != null && !busy &&
                state.explainInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.keyPlus,
                contentColor = palette.onKeyPlus,
                disabledContainerColor = palette.surfaceStrong,
                disabledContentColor = palette.textSecondary,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.SmartToy,
                contentDescription = null,
                modifier = Modifier.size(CalculinoSizes.iconSmall),
            )
            Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
            Text(stringResource(R.string.ai_explain_button))
        }
        if (history.isNotEmpty()) {
            Text(
                text = stringResource(R.string.ai_history_title),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                history.take(8).forEach { entry ->
                    TextButton(onClick = { onSelectHistory(entry) }) {
                        Text(
                            text = entry.expression,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        state.currentCalculation?.let { calculation ->
            VerifiedCalculationCard(calculation)
        }
        state.currentSteps?.let { steps ->
            StepsCard(steps)
        }
        when (val generation = state.explainGeneration) {
            AiGenerationState.VerifyingModel -> ProgressCard(
                text = stringResource(R.string.ai_verifying),
            )
            AiGenerationState.LoadingModel -> ProgressCard(
                text = stringResource(R.string.ai_loading),
            )
            AiGenerationState.Generating -> ProgressCard(
                text = stringResource(R.string.ai_generating),
            )
            is AiGenerationState.Failure -> ErrorCard(
                message = aiErrorMessage(generation.error),
            )
            is AiGenerationState.Success -> ExplanationCard(state = generation)
            AiGenerationState.Idle -> Unit
        }
        Surface(
            shape = MaterialTheme.shapes.large,
            color = palette.surfaceMuted,
        ) {
            Text(
                text = stringResource(R.string.ai_disclaimer),
                modifier = Modifier.padding(CalculinoSpacing.md),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
        Spacer(modifier = Modifier.height(CalculinoSpacing.xxl))
    }
}

@Composable
private fun StepsCard(steps: VerifiedMathSteps) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.ai_steps_title),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
                    steps.steps.forEachIndexed { index, step ->
                        val approximation = step.approximation?.let { " ≈ $it" }.orEmpty()
                        Text(
                            text = "${index + 1}. ${step.expression} = ${step.result}$approximation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelsSheet(
    modelStates: Map<String, LocalModelState>,
    selectedModel: LocalModel?,
    isDeviceSupported: Boolean,
    onDownload: (LocalModel) -> Unit,
    onCancelDownload: (LocalModel) -> Unit,
    onDeleteModel: (LocalModel) -> Unit,
    onSelectModel: (LocalModel) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CalculinoSpacing.lg)
            .padding(bottom = CalculinoSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.ai_models_title),
            style = MaterialTheme.typography.titleLarge,
            color = palette.textPrimary,
        )
        Text(
            text = stringResource(R.string.ai_models_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
        LocalModel.entries.forEach { model ->
            ModelCard(
                model = model,
                state = modelStates[model.id] ?: LocalModelState.NotDownloaded,
                selected = selectedModel?.id == model.id,
                isDeviceSupported = isDeviceSupported,
                onDownload = { onDownload(model) },
                onCancel = { onCancelDownload(model) },
                onDelete = { onDeleteModel(model) },
                onSelect = { onSelectModel(model) },
            )
        }
        Text(
            text = stringResource(R.string.ai_models_note),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun UnsupportedDeviceCard() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(CalculinoSizes.iconMedium),
            )
            Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs)) {
                Text(
                    text = stringResource(R.string.ai_device_unsupported_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = stringResource(R.string.ai_device_unsupported_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: LocalModel,
    state: LocalModelState,
    selected: Boolean,
    isDeviceSupported: Boolean,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val context = LocalContext.current
    val size = Formatter.formatFileSize(context, model.sizeBytes)
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(
            width = if (selected) CalculinoSizes.focusBorder else CalculinoSizes.hairline,
            color = if (selected) palette.focusRing else palette.border,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = modelName(model),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                    )
                    Text(
                        text = stringResource(
                            R.string.ai_model_meta,
                            model.quantization,
                            size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
                if (selected) {
                    SelectedBadge()
                }
            }
            Text(
                text = modelDescription(model),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            if (model.recommendedForChat || model.recommendedForMath) {
                Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs)) {
                    if (model.recommendedForChat) {
                        RecommendationBadge(stringResource(R.string.ai_recommended_chat))
                    }
                    if (model.recommendedForMath) {
                        RecommendationBadge(stringResource(R.string.ai_recommended_math))
                    }
                }
            }
            when (state) {
                LocalModelState.NotDownloaded -> DownloadRow(
                    label = stringResource(R.string.ai_download),
                    enabled = isDeviceSupported,
                    onDownload = onDownload,
                )
                is LocalModelState.Downloading -> DownloadingContent(
                    state = state,
                    onCancel = onCancel,
                )
                LocalModelState.Ready -> ReadyContent(
                    selected = selected,
                    onSelect = onSelect,
                    onDelete = onDelete,
                )
                is LocalModelState.Failed -> {
                    Text(
                        text = failureMessage(state.failure),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    DownloadRow(
                        label = stringResource(R.string.ai_retry_download),
                        enabled = isDeviceSupported,
                        onDownload = onDownload,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    label: String,
    enabled: Boolean,
    onDownload: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        Button(
            shape = PillShape,
            onClick = onDownload,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.keyPlus,
                contentColor = palette.onKeyPlus,
                disabledContainerColor = palette.surfaceStrong,
                disabledContentColor = palette.textSecondary,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = null,
                modifier = Modifier.size(CalculinoSizes.iconSmall),
            )
            Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
            Text(label)
        }
        Text(
            text = stringResource(R.string.ai_download_wifi),
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun DownloadingContent(
    state: LocalModelState.Downloading,
    onCancel: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val context = LocalContext.current
    val fraction = if (state.totalBytes > 0) {
        (state.downloadedBytes.toFloat() / state.totalBytes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
        Text(
            text = stringResource(
                R.string.ai_downloading,
                Formatter.formatFileSize(context, state.downloadedBytes),
                Formatter.formatFileSize(context, state.totalBytes),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CalculinoSizes.downloadProgressHeight)
                .background(palette.surfaceStrong, CircleShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(CalculinoSizes.downloadProgressHeight)
                    .background(palette.keyPlus, CircleShape),
            )
        }
        TextButton(
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(R.string.ai_cancel_download))
        }
    }
}

@Composable
private fun ReadyContent(
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.ai_ready),
            style = MaterialTheme.typography.labelMedium,
            color = palette.title,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (!selected) {
            TextButton(onClick = onSelect) {
                Text(stringResource(R.string.ai_use_model))
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.ai_delete_model),
                tint = palette.icon,
            )
        }
    }
}

@Composable
private fun SelectedBadge() {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.keyClear,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = CalculinoSpacing.sm,
                vertical = CalculinoSpacing.xxs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = palette.onKeyClear,
                modifier = Modifier.size(CalculinoSizes.iconSmall),
            )
            Text(
                text = stringResource(R.string.ai_selected_model),
                style = MaterialTheme.typography.labelSmall,
                color = palette.onKeyClear,
            )
        }
    }
}

@Composable
private fun RecommendationBadge(text: String) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surfaceStrong,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = CalculinoSpacing.sm,
                vertical = CalculinoSpacing.xxs,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun ProgressCard(text: String) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surfaceMuted,
    ) {
        Row(
            modifier = Modifier.padding(CalculinoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(CalculinoSizes.iconMedium),
                color = palette.title,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(CalculinoSpacing.md),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun ExplanationCard(state: AiGenerationState.Success) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.SmartToy,
                    contentDescription = null,
                    tint = palette.title,
                    modifier = Modifier.size(CalculinoSizes.iconSmall),
                )
                Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
                Text(
                    text = modelName(state.model),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(
                        R.string.ai_result_meta,
                        formatTokensPerSecond(state.tokensPerSecond),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                )
            }
            SelectionContainer {
                FormattedAiText(
                    text = state.text,
                    color = palette.textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun FormattedAiText(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val formatted = remember(text) { formatAiText(text) }
    Text(
        text = formatted,
        modifier = modifier,
        style = style,
        color = color,
    )
}

private fun formatAiText(text: String): AnnotatedString = buildAnnotatedString {
    text.lines().forEachIndexed { index, rawLine ->
        if (index > 0) append('\n')
        val line = rawLine.trimEnd()
        val trimmed = line.trimStart()
        val bullet = trimmed.startsWith("- ") || trimmed.startsWith("* ") ||
            trimmed.startsWith("• ")
        if (bullet) append("• ")
        val content = when {
            bullet -> trimmed.drop(2)
            trimmed.startsWith("### ") -> trimmed.drop(4)
            trimmed.startsWith("## ") -> trimmed.drop(3)
            trimmed.startsWith("# ") -> trimmed.drop(2)
            else -> line
        }
        appendBold(content)
    }
}

private fun AnnotatedString.Builder.appendBold(text: String) {
    var index = 0
    while (index < text.length) {
        val start = text.indexOf("**", index)
        if (start < 0) {
            append(text.substring(index))
            return
        }
        append(text.substring(index, start))
        val end = text.indexOf("**", start + 2)
        if (end < 0) {
            append(text.substring(start))
            return
        }
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(text.substring(start + 2, end))
        }
        index = end + 2
    }
}

@Composable
private fun aiErrorMessage(error: AiError): String = stringResource(
    when (error) {
        AiError.UnsupportedDevice -> R.string.ai_error_unsupported
        AiError.ModelMissing -> R.string.ai_error_missing
        AiError.ModelCorrupt -> R.string.ai_error_corrupt
        AiError.ModelLoadFailed -> R.string.ai_error_load
        AiError.OutOfMemory -> R.string.ai_error_memory
        AiError.InferenceFailed -> R.string.ai_error_inference
        AiError.EmptyResponse -> R.string.ai_error_empty
    },
)

@Composable
private fun failureMessage(failure: ModelFailure): String = stringResource(
    when (failure) {
        ModelFailure.DownloadFailed -> R.string.ai_download_failed
        ModelFailure.CorruptFile -> R.string.ai_model_corrupt
    },
)

@Composable
private fun modelName(model: LocalModel): String = stringResource(
    when (model) {
        LocalModel.Qwen2_5_1_5B -> R.string.ai_model_qwen25_name
    },
)

@Composable
private fun modelDescription(model: LocalModel): String = stringResource(
    when (model) {
        LocalModel.Qwen2_5_1_5B -> R.string.ai_model_qwen25_description
    },
)

private fun formatTokensPerSecond(value: Float): String =
    String.format(Locale.getDefault(), "%.1f", value)

private fun AiGenerationState.isBusy(): Boolean =
    this is AiGenerationState.VerifyingModel ||
        this is AiGenerationState.LoadingModel ||
        this is AiGenerationState.Generating
