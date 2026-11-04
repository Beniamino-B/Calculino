package com.calculino.app.ui.algebra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculino.app.data.mathjs.MathJsApiRepository
import com.calculino.app.domain.algebra.AlgebraResult
import com.calculino.app.domain.algebra.AlgebraSolver
import com.calculino.app.domain.mathjs.MathJsOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AlgebraMode {
    Local,
    Online,
}

data class AlgebraUiState(
    val input: String = "",
    val result: AlgebraResult? = null,
    val mode: AlgebraMode = AlgebraMode.Local,
    val precision: Int = 6,
    val onlineOutcome: MathJsOutcome? = null,
    val onlineLoading: Boolean = false,
)

class AlgebraViewModel(
    private val mathJsRepository: MathJsApiRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AlgebraUiState())
    val uiState: StateFlow<AlgebraUiState> = mutableState.asStateFlow()

    fun onInputChange(value: String) {
        mutableState.update { state ->
            state.copy(input = value, result = null, onlineOutcome = null)
        }
    }

    fun onExample(value: String) {
        mutableState.update { state ->
            state.copy(input = value, result = null, onlineOutcome = null)
        }
    }

    fun onModeChange(mode: AlgebraMode) {
        mutableState.update { state ->
            state.copy(mode = mode, result = null, onlineOutcome = null)
        }
    }

    fun onPrecisionChange(precision: Int) {
        mutableState.update { state -> state.copy(precision = precision, onlineOutcome = null) }
    }

    fun solve() {
        val state = mutableState.value
        val input = state.input.trim()
        if (input.isEmpty() || state.onlineLoading) return
        if (state.mode == AlgebraMode.Local) {
            mutableState.update { current -> current.copy(result = AlgebraSolver.solve(input)) }
            return
        }
        mutableState.update { current -> current.copy(onlineLoading = true, onlineOutcome = null) }
        viewModelScope.launch {
            val outcome = mathJsRepository.evaluate(input, state.precision)
            mutableState.update { current ->
                current.copy(onlineLoading = false, onlineOutcome = outcome)
            }
        }
    }
}
