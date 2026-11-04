package com.calculino.app.domain.ai

import com.calculino.app.BuildConfig
import com.calculino.app.domain.math.AngleMode

enum class LocalModel(
    val id: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val sha256: String,
    val quantization: String,
    val recommendedForChat: Boolean,
    val recommendedForMath: Boolean,
) {
    Qwen2_5_1_5B(
        id = "qwen2_5_1_5b",
        fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
        downloadUrl = BuildConfig.QWEN_MODEL_URL,
        sizeBytes = 1_117_320_736L,
        sha256 = "6a1a2eb6d15622bf3c96857206351ba97e1af16c30d7a74ee38970e434e9407e",
        quantization = "Q4_K_M",
        recommendedForChat = true,
        recommendedForMath = true,
    ),
}

enum class AiMode {
    Chat,
    Explain,
}

data class VerifiedCalculation(
    val expression: String,
    val result: String,
    val approximation: String?,
    val isExact: Boolean,
    val angleMode: AngleMode,
)

data class AiChatTurn(
    val fromUser: Boolean,
    val text: String,
)

sealed interface LocalModelState {
    data object NotDownloaded : LocalModelState
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : LocalModelState
    data object Ready : LocalModelState
    data class Failed(val failure: ModelFailure) : LocalModelState
}

enum class ModelFailure {
    DownloadFailed,
    CorruptFile,
}

enum class DownloadStartResult {
    Started,
    InsufficientSpace,
    Unavailable,
}

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data object VerifyingModel : AiGenerationState
    data object LoadingModel : AiGenerationState
    data object Generating : AiGenerationState
    data class Success(
        val text: String,
        val tokensPerSecond: Float,
        val model: LocalModel,
    ) : AiGenerationState

    data class Failure(val error: AiError) : AiGenerationState
}

enum class AiError {
    UnsupportedDevice,
    ModelMissing,
    ModelCorrupt,
    ModelLoadFailed,
    OutOfMemory,
    InferenceFailed,
    EmptyResponse,
}

sealed interface LocalAiResult {
    data class Success(
        val text: String,
        val tokensPerSecond: Float,
    ) : LocalAiResult

    data class Failure(val error: AiError) : LocalAiResult
}
