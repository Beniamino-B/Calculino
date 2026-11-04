package com.calculino.app.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class MlKitOcrEngine(
    context: Context,
) : OcrEngine {
    private val appContext = context.applicationContext
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognize(imagePath: String): OcrResult {
        val image = runCatching {
            InputImage.fromFilePath(appContext, Uri.fromFile(File(imagePath)))
        }.getOrElse {
            return OcrResult.Failure(OcrError.RecognitionFailed)
        }
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    val content = text.textBlocks
                        .joinToString(separator = "\n") { block -> block.text }
                        .trim()
                    continuation.resume(
                        if (content.isEmpty()) {
                            OcrResult.Failure(OcrError.EmptyText)
                        } else {
                            OcrResult.Success(content)
                        },
                    )
                }
                .addOnFailureListener {
                    continuation.resume(OcrResult.Failure(OcrError.RecognitionFailed))
                }
        }
    }
}
