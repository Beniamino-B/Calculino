package com.calculino.app.data.ai

import com.calculino.app.domain.ai.AiError
import kotlinx.coroutines.flow.Flow

interface LocalAiRepository {
    val isDeviceSupported: Boolean
    fun generateResponse(prompt: String): Flow<String>
}

class LocalAiGenerationException(
    val error: AiError,
) : Exception()
