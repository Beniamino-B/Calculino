package com.calculino.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculino.app.data.ocr.OcrEngine
import com.calculino.app.data.ocr.OcrError
import com.calculino.app.data.ocr.OcrResult
import com.calculino.app.domain.ocr.FormulaTextNormalizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraOcrUiState(
    val recognizing: Boolean = false,
    val error: OcrError? = null,
    val recognizedFormula: String? = null,
)

class CameraOcrViewModel(
    private val engine: OcrEngine,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CameraOcrUiState())
    val uiState: StateFlow<CameraOcrUiState> = mutableState.asStateFlow()

    fun recognize(imagePath: String) {
        if (mutableState.value.recognizing) return
        mutableState.update { state ->
            state.copy(recognizing = true, error = null, recognizedFormula = null)
        }
        viewModelScope.launch {
            try {
                when (val result = engine.recognize(imagePath)) {
                    is OcrResult.Success -> {
                        val formula = FormulaTextNormalizer.normalize(result.text)
                        if (formula.isEmpty()) {
                            mutableState.update { state ->
                                state.copy(recognizing = false, error = OcrError.EmptyText)
                            }
                        } else {
                            mutableState.update { state ->
                                state.copy(recognizing = false, recognizedFormula = formula)
                            }
                        }
                    }
                    is OcrResult.Failure -> mutableState.update { state ->
                        state.copy(recognizing = false, error = result.error)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                mutableState.update { state ->
                    state.copy(recognizing = false, error = OcrError.RecognitionFailed)
                }
            }
        }
    }

    fun clearError() {
        mutableState.update { state -> state.copy(error = null) }
    }
}
