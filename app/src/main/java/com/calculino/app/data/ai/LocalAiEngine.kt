package com.calculino.app.data.ai

import android.os.Build
import com.calculino.app.domain.ai.AiError
import com.calculino.app.domain.ai.AiPromptBuilder
import com.calculino.app.domain.ai.LocalAiResult
import com.calculino.app.domain.ai.LocalModel
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaException
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LocalAiEngine(
    private val modelRepository: LocalModelRepository,
) : LocalAiRepository {
    override val isDeviceSupported: Boolean by lazy {
        val arm64 = Build.SUPPORTED_ABIS.any { abi -> abi.equals("arm64-v8a", ignoreCase = true) }
        arm64 && runCatching { Llama.getSystemInfo() }.isSuccess
    }

    override fun generateResponse(prompt: String): Flow<String> = flow {
        val result = generate(
            model = LocalModel.Qwen2_5_1_5B,
            systemPrompt = AiPromptBuilder.CHAT_SYSTEM,
            prompt = prompt,
            maxTokens = CHAT_MAX_TOKENS,
            temperature = CHAT_TEMPERATURE,
        )
        when (result) {
            is LocalAiResult.Success -> emit(result.text)
            is LocalAiResult.Failure -> throw LocalAiGenerationException(result.error)
        }
    }.flowOn(Dispatchers.Default)

    suspend fun generate(
        model: LocalModel,
        systemPrompt: String,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        onVerifying: suspend () -> Unit = {},
        onLoading: suspend () -> Unit = {},
        onGenerating: suspend () -> Unit = {},
    ): LocalAiResult {
        if (!isDeviceSupported) return LocalAiResult.Failure(AiError.UnsupportedDevice)
        val file = modelRepository.fileFor(model)
        if (!file.exists() || file.length() != model.sizeBytes) {
            return LocalAiResult.Failure(AiError.ModelMissing)
        }
        onVerifying()
        val verified = runCatching { modelRepository.ensureVerified(model) }.getOrDefault(false)
        if (!verified) {
            return LocalAiResult.Failure(AiError.ModelCorrupt)
        }
        onLoading()
        val threads = min(4, Runtime.getRuntime().availableProcessors()).coerceAtLeast(1)
        val llamaModel = try {
            Llama.loadModel(
                modelPath = file.absolutePath,
                config = LlamaConfig(
                    contextSize = 2048,
                    threads = threads,
                    gpuLayers = 0,
                    temperature = temperature,
                    topP = 0.9f,
                    topK = 40,
                    seed = 42,
                ),
            )
        } catch (_: OutOfMemoryError) {
            return LocalAiResult.Failure(AiError.OutOfMemory)
        } catch (_: LlamaException) {
            return LocalAiResult.Failure(AiError.ModelLoadFailed)
        } catch (_: UnsatisfiedLinkError) {
            return LocalAiResult.Failure(AiError.UnsupportedDevice)
        } catch (_: Throwable) {
            return LocalAiResult.Failure(AiError.ModelLoadFailed)
        }
        return try {
            onGenerating()
            val response = Llama.complete(
                model = llamaModel,
                prompt = prompt,
                systemPrompt = systemPrompt,
                maxTokens = maxTokens,
            )
            val text = cleanResponse(response.text)
            if (text.isBlank()) {
                LocalAiResult.Failure(AiError.EmptyResponse)
            } else {
                LocalAiResult.Success(
                    text = text,
                    tokensPerSecond = response.tokensPerSecond,
                )
            }
        } catch (_: OutOfMemoryError) {
            LocalAiResult.Failure(AiError.OutOfMemory)
        } catch (_: LlamaException) {
            LocalAiResult.Failure(AiError.InferenceFailed)
        } catch (_: UnsatisfiedLinkError) {
            LocalAiResult.Failure(AiError.UnsupportedDevice)
        } catch (_: Throwable) {
            LocalAiResult.Failure(AiError.InferenceFailed)
        } finally {
            runCatching { Llama.releaseModel(llamaModel) }
        }
    }

    private fun cleanResponse(raw: String): String {
        var text = raw
        text = Regex("(?s) thinking.*?(<｜end▁of▁thinking｜>|<｜end▁of▁thinking｜>)").replace(text, "")
        text = Regex("(?s) thinking.*$").replace(text, "")
        text = text.replace(" response", "")
        text = text.replace("<｜end▁of▁thinking｜>", "")
        text = text.replace("<|im_end|>", "").replace("<|endoftext|>", "")
        text = text.replace("<|im_start|>", "")
        return text.trim()
    }
}

private const val CHAT_MAX_TOKENS = 420
private const val CHAT_TEMPERATURE = 0.7f
