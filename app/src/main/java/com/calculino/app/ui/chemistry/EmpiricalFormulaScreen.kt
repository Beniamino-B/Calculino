package com.calculino.app.ui.chemistry

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.chemistry.EmpiricalFormulaError
import com.calculino.app.domain.chemistry.EmpiricalFormulaFinder
import com.calculino.app.domain.chemistry.EmpiricalFormulaOutcome
import com.calculino.app.ui.engineering.EngineeringCard
import com.calculino.app.ui.engineering.EngineeringError
import com.calculino.app.ui.engineering.EngineeringResultRow
import com.calculino.app.ui.engineering.EngineeringToolScaffold
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class CompositionBasis {
    Percentage,
    Mass,
}

data class EmpiricalUiState(
    val composition: String = "",
    val molarMass: String = "",
    val basis: CompositionBasis = CompositionBasis.Percentage,
    val result: EmpiricalFormulaOutcome? = null,
    val invalidComposition: Boolean = false,
)

class EmpiricalViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(EmpiricalUiState())
    val uiState: StateFlow<EmpiricalUiState> = mutableState.asStateFlow()

    fun onCompositionChange(value: String) {
        mutableState.update { state ->
            state.copy(composition = value, result = null, invalidComposition = false)
        }
    }

    fun onMolarMassChange(value: String) {
        mutableState.update { state -> state.copy(molarMass = value, result = null) }
    }

    fun onBasisChange(basis: CompositionBasis) {
        mutableState.update { state -> state.copy(basis = basis, result = null) }
    }

    fun calculate() {
        mutableState.update { state ->
            val amounts = parseComposition(state.composition)
            if (amounts == null) {
                state.copy(result = null, invalidComposition = true)
            } else {
                val molarMass = state.molarMass.trim()
                    .takeIf(String::isNotEmpty)
                    ?.replace(',', '.')
                    ?.toDoubleOrNull()
                state.copy(
                    result = EmpiricalFormulaFinder.find(amounts, molarMass),
                    invalidComposition = false,
                )
            }
        }
    }
}

@Composable
fun EmpiricalFormulaRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: EmpiricalViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EmpiricalFormulaScreen(
        state = state,
        onCompositionChange = viewModel::onCompositionChange,
        onMolarMassChange = viewModel::onMolarMassChange,
        onBasisChange = viewModel::onBasisChange,
        onCalculate = viewModel::calculate,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun EmpiricalFormulaScreen(
    state: EmpiricalUiState,
    onCompositionChange: (String) -> Unit,
    onMolarMassChange: (String) -> Unit,
    onBasisChange: (CompositionBasis) -> Unit,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_empirical_title),
        subtitle = stringResource(R.string.tool_empirical_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        EngineeringCard(title = stringResource(R.string.empirical_input)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                CompositionBasis.entries.forEach { basis ->
                    FilterChip(
                        selected = state.basis == basis,
                        onClick = { onBasisChange(basis) },
                        label = {
                            Text(
                                stringResource(
                                    if (basis == CompositionBasis.Percentage) {
                                        R.string.empirical_percentage
                                    } else {
                                        R.string.empirical_mass
                                    },
                                ),
                            )
                        },
                    )
                }
            }
            OutlinedTextField(
                value = state.composition,
                onValueChange = onCompositionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.empirical_composition)) },
                placeholder = { Text(stringResource(R.string.empirical_composition_hint)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
            OutlinedTextField(
                value = state.molarMass,
                onValueChange = onMolarMassChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.empirical_molar_mass)) },
                singleLine = true,
            )
            if (state.invalidComposition) {
                EngineeringError(stringResource(R.string.empirical_invalid_composition))
            }
            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.composition.isNotBlank(),
            ) {
                Text(stringResource(R.string.empirical_calculate))
            }
        }
        when (val result = state.result) {
            is EmpiricalFormulaOutcome.Success -> EngineeringCard(
                title = stringResource(R.string.empirical_result),
            ) {
                EngineeringResultRow(
                    label = stringResource(R.string.empirical_formula),
                    value = result.result.empiricalFormula,
                )
                result.result.molecularFormula?.let { molecular ->
                    EngineeringResultRow(
                        label = stringResource(R.string.empirical_molecular_formula),
                        value = molecular,
                    )
                }
                EngineeringResultRow(
                    label = stringResource(R.string.empirical_formula_mass),
                    value = "${formatEmpirical(result.result.empiricalMass)} ${stringResource(R.string.unit_g_mol)}",
                )
                EngineeringResultRow(
                    label = stringResource(R.string.empirical_multiplier),
                    value = result.result.multiplier.toString(),
                )
            }
            is EmpiricalFormulaOutcome.Failure -> EngineeringCard(
                title = stringResource(R.string.empirical_error),
            ) {
                EngineeringError(
                    message = when (result.error) {
                        EmpiricalFormulaError.EmptyComposition ->
                            stringResource(R.string.empirical_error_empty)
                        EmpiricalFormulaError.InvalidAmount ->
                            stringResource(R.string.empirical_error_amount)
                        EmpiricalFormulaError.UnknownElement ->
                            stringResource(
                                R.string.empirical_error_unknown_element,
                                result.symbol.orEmpty(),
                            )
                        EmpiricalFormulaError.TooManyElements ->
                            stringResource(R.string.empirical_error_too_many)
                        EmpiricalFormulaError.ImpossibleMoles ->
                            stringResource(R.string.empirical_error_moles)
                        EmpiricalFormulaError.ImpossibleRatio ->
                            stringResource(R.string.empirical_error_ratio)
                        EmpiricalFormulaError.InvalidMolarMass ->
                            stringResource(R.string.empirical_error_molar_mass)
                    },
                )
            }
            null -> Unit
        }
        Text(
            text = stringResource(R.string.empirical_note),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}

private fun parseComposition(input: String): Map<String, Double>? {
    val parts = input.split(',', ';', '\n')
        .map(String::trim)
        .filter(String::isNotEmpty)
    if (parts.isEmpty()) return null
    val result = mutableMapOf<String, Double>()
    parts.forEach { part ->
        val separator = when {
            ':' in part -> ':'
            '=' in part -> '='
            else -> return null
        }
        val symbol = part.substringBefore(separator).trim().replaceFirstChar { it.uppercase() }
        val amount = part.substringAfter(separator).trim().replace(',', '.').toDoubleOrNull() ?: return null
        if (symbol.isEmpty() || !symbol.all(Char::isLetter) || amount <= 0.0) return null
        result[symbol] = (result[symbol] ?: 0.0) + amount
    }
    return result.takeIf(Map<String, Double>::isNotEmpty)
}

private fun formatEmpirical(value: Double): String {
    val rounded = kotlin.math.round(value * 1_000_000.0) / 1_000_000.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
