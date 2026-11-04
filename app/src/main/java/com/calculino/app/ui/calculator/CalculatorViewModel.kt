package com.calculino.app.ui.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculino.app.data.repository.HistoryRepository
import com.calculino.app.domain.math.AngleMode
import com.calculino.app.domain.math.LongDivisionProcedure
import com.calculino.app.domain.math.LongDivisionProcedureBuilder
import com.calculino.app.domain.math.MathEngine
import com.calculino.app.domain.math.MathEvaluationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val outcome: MathEvaluationResult? = null,
    val procedure: LongDivisionProcedure? = null,
    val angleMode: AngleMode = AngleMode.Radians,
) {
    val canEvaluate: Boolean get() = expression.isNotBlank()
}

class CalculatorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val engine = MathEngine()
    private var lastSavedSignature: String? = null

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            expression = savedStateHandle.get<String>(KEY_EXPRESSION).orEmpty(),
            angleMode = savedStateHandle.get<String>(KEY_ANGLE_MODE)
                ?.let { savedMode -> AngleMode.entries.firstOrNull { it.name == savedMode } }
                ?: AngleMode.Radians,
        ),
    )
    val uiState = _uiState.asStateFlow()

    fun onToken(token: String) {
        updateExpression { current ->
            when {
                current.length >= MAX_LENGTH -> current
                token in OPERATORS && current.isEmpty() -> if (token == MINUS) token else current
                token in OPERATORS && current.last().toString() in OPERATORS -> current.dropLast(1) + token
                token == DECIMAL -> appendDecimal(current)
                token == PERCENT -> if (current.isEmpty() || current.last().toString() in OPERATORS) current else current + token
                else -> current + token
            }
        }
    }

    fun onToggleSign() {
        updateExpression { current ->
            when {
                current.isEmpty() -> MINUS
                current.startsWith(MINUS) -> current.drop(1)
                else -> MINUS + current
            }
        }
    }

    fun onToggleAngleMode() {
        val current = _uiState.value
        val updatedMode = if (current.angleMode == AngleMode.Radians) AngleMode.Degrees else AngleMode.Radians
        val outcome = if (current.expression.isBlank()) {
            null
        } else {
            engine.evaluate(current.expression, updatedMode)
        }
        val procedure = if (outcome is MathEvaluationResult.Success) {
            LongDivisionProcedureBuilder.build(current.expression, updatedMode)
        } else {
            null
        }
        lastSavedSignature = null
        _uiState.value = current.copy(
            angleMode = updatedMode,
            outcome = outcome,
            procedure = procedure,
        )
        savedStateHandle[KEY_ANGLE_MODE] = updatedMode.name
    }

    fun onClear() {
        updateExpression { "" }
    }

    fun onBackspace() {
        updateExpression { it.dropLast(1) }
    }

    fun onScannedExpression(expression: String) {
        val normalized = expression.trim()
        if (normalized.isEmpty()) return
        updateExpression { normalized.take(MAX_LENGTH) }
    }

    fun onEquals() {
        val current = _uiState.value
        if (!current.canEvaluate) return
        val outcome = engine.evaluate(current.expression, current.angleMode)
        val procedure = if (outcome is MathEvaluationResult.Success) {
            LongDivisionProcedureBuilder.build(current.expression, current.angleMode)
        } else {
            null
        }
        _uiState.value = current.copy(outcome = outcome, procedure = procedure)
        if (outcome is MathEvaluationResult.Success) {
            saveToHistory(current, outcome)
        }
    }

    private fun saveToHistory(
        state: CalculatorUiState,
        outcome: MathEvaluationResult.Success,
    ) {
        val display = outcome.display
        val signature = "${state.expression}|${display.main}|${display.isExact}|${state.angleMode.name}"
        if (signature == lastSavedSignature) return
        lastSavedSignature = signature
        val expression = state.expression
        val angleMode = state.angleMode
        viewModelScope.launch {
            runCatching {
                historyRepository.add(
                    expression = expression,
                    result = display.main,
                    approximation = display.approximation,
                    isExact = display.isExact,
                    angleMode = angleMode,
                    createdAt = System.currentTimeMillis(),
                )
            }
        }
    }

    private fun appendDecimal(current: String): String {
        if (current.isEmpty() || current.last().toString() in OPERATORS) return current + "0$DECIMAL"
        val number = current.takeLastWhile { it.isDigit() || it == DECIMAL_CHAR }
        return if (number.contains(DECIMAL)) current else current + DECIMAL
    }

    private fun updateExpression(transform: (String) -> String) {
        val current = _uiState.value
        val updated = transform(current.expression)
        if (updated == current.expression) return
        lastSavedSignature = null
        _uiState.value = current.copy(expression = updated, outcome = null, procedure = null)
        savedStateHandle[KEY_EXPRESSION] = updated
    }

    private companion object {
        const val KEY_EXPRESSION = "expression"
        const val KEY_ANGLE_MODE = "angle_mode"
        const val MAX_LENGTH = 64
        const val MINUS = "−"
        const val DECIMAL = "."
        const val DECIMAL_CHAR = '.'
        const val PERCENT = "%"
        val OPERATORS = setOf("+", MINUS, "×", "÷", "^")
    }
}
