package com.calculino.app.ui.chemistry

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.chemistry.PhCalculator
import com.calculino.app.domain.chemistry.PhResult
import com.calculino.app.domain.chemistry.SolutionType
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

data class PhUiState(
    val type: SolutionType = SolutionType.StrongAcid,
    val concentration: String = "",
    val constant: String = "",
    val result: PhResult? = null,
    val invalid: Boolean = false,
)

class PhViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(PhUiState())
    val uiState: StateFlow<PhUiState> = mutableState.asStateFlow()

    fun onTypeChange(type: SolutionType) {
        mutableState.update { state -> state.copy(type = type, result = null, invalid = false) }
    }

    fun onConcentrationChange(value: String) {
        mutableState.update { state -> state.copy(concentration = value, result = null, invalid = false) }
    }

    fun onConstantChange(value: String) {
        mutableState.update { state -> state.copy(constant = value, result = null, invalid = false) }
    }

    fun calculate() {
        mutableState.update { state ->
            val concentration = state.concentration.trim().replace(',', '.').toDoubleOrNull()
            val constant = state.constant.trim().replace(',', '.').toDoubleOrNull()
            val result = if (concentration != null) {
                PhCalculator.calculate(state.type, concentration, constant)
            } else {
                null
            }
            state.copy(result = result, invalid = result == null)
        }
    }
}

@Composable
fun PhRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PhViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PhScreen(
        state = state,
        onTypeChange = viewModel::onTypeChange,
        onConcentrationChange = viewModel::onConcentrationChange,
        onConstantChange = viewModel::onConstantChange,
        onCalculate = viewModel::calculate,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun PhScreen(
    state: PhUiState,
    onTypeChange: (SolutionType) -> Unit,
    onConcentrationChange: (String) -> Unit,
    onConstantChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val needsConstant = state.type == SolutionType.WeakAcid || state.type == SolutionType.WeakBase
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_ph_title),
        subtitle = stringResource(R.string.tool_ph_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        EngineeringCard(title = stringResource(R.string.ph_input)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                SolutionType.entries.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { onTypeChange(type) },
                        label = { Text(solutionTypeLabel(type), maxLines = 1) },
                    )
                }
            }
            OutlinedTextField(
                value = state.concentration,
                onValueChange = onConcentrationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.ph_concentration)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
            if (needsConstant) {
                OutlinedTextField(
                    value = state.constant,
                    onValueChange = onConstantChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            stringResource(
                                if (state.type == SolutionType.WeakAcid) {
                                    R.string.ph_ka
                                } else {
                                    R.string.ph_kb
                                },
                            ),
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                )
            }
            if (state.invalid) {
                EngineeringError(stringResource(R.string.ph_invalid))
            }
            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.ph_calculate))
            }
        }
        state.result?.let { result ->
            EngineeringCard(title = stringResource(R.string.ph_result)) {
                EngineeringResultRow(
                    label = stringResource(R.string.ph_value),
                    value = formatPh(result.ph),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.poh_value),
                    value = formatPh(result.poh),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.ph_hydrogen),
                    value = formatPh(result.hydrogenConcentration),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.ph_hydroxide),
                    value = formatPh(result.hydroxideConcentration),
                )
            }
            Text(
                text = stringResource(R.string.ph_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun solutionTypeLabel(type: SolutionType): String = stringResource(
    when (type) {
        SolutionType.StrongAcid -> R.string.ph_type_strong_acid
        SolutionType.WeakAcid -> R.string.ph_type_weak_acid
        SolutionType.StrongBase -> R.string.ph_type_strong_base
        SolutionType.WeakBase -> R.string.ph_type_weak_base
    },
)

private fun formatPh(value: Double): String = String.format(java.util.Locale.US, "%.5f", value)
