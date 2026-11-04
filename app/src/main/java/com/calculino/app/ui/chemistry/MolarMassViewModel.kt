package com.calculino.app.ui.chemistry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.calculino.app.domain.chemistry.MolarMassCalculator
import com.calculino.app.domain.chemistry.MolarMassOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MolarMassUiState(
    val formula: String = "",
    val outcome: MolarMassOutcome? = null,
)

class MolarMassViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialFormula = savedStateHandle.get<String>(KEY_FORMULA).orEmpty()

    private val _uiState = MutableStateFlow(
        MolarMassUiState(
            formula = initialFormula,
            outcome = evaluate(initialFormula),
        ),
    )
    val uiState = _uiState.asStateFlow()

    fun onFormulaChange(value: String) {
        val formula = value.take(MAX_FORMULA_LENGTH)
        _uiState.value = MolarMassUiState(
            formula = formula,
            outcome = evaluate(formula),
        )
        savedStateHandle[KEY_FORMULA] = formula
    }

    fun clear() {
        onFormulaChange("")
    }

    private fun evaluate(formula: String): MolarMassOutcome? =
        if (formula.isBlank()) null else MolarMassCalculator.calculate(formula)

    private companion object {
        const val KEY_FORMULA = "formula"
        const val MAX_FORMULA_LENGTH = 120
    }
}
