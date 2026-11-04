package com.calculino.app.data.ocr

sealed interface OcrResult {
    data class Success(val text: String) : OcrResult
    data class Failure(val error: OcrError) : OcrResult
}

enum class OcrError {
    EmptyText,
    RecognitionFailed,
}

interface OcrEngine {
    suspend fun recognize(imagePath: String): OcrResult
}
