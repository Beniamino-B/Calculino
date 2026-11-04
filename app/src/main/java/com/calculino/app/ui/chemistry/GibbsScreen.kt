package com.calculino.app.ui.chemistry

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import com.calculino.app.domain.chemistry.GibbsCalculator
import com.calculino.app.domain.chemistry.GibbsResult
import com.calculino.app.domain.chemistry.Spontaneity
import com.calculino.app.ui.engineering.EngineeringCard
import com.calculino.app.ui.engineering.EngineeringError
import com.calculino.app.ui.engineering.EngineeringResultRow
import com.calculino.app.ui.engineering.EngineeringToolScaffold
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GibbsUiState(
    val enthalpy: String = "",
    val entropy: String = "",
    val temperature: String = "",
    val result: GibbsResult? = null,
    val invalid: Boolean = false,
)

class GibbsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(GibbsUiState())
    val uiState: StateFlow<GibbsUiState> = mutableState.asStateFlow()

    fun onEnthalpyChange(value: String) {
        mutableState.update { state -> state.copy(enthalpy = value, result = null, invalid = false) }
    }

    fun onEntropyChange(value: String) {
        mutableState.update { state -> state.copy(entropy = value, result = null, invalid = false) }
    }

    fun onTemperatureChange(value: String) {
        mutableState.update { state -> state.copy(temperature = value, result = null, invalid = false) }
    }

    fun calculate() {
        mutableState.update { state ->
            val enthalpy = state.enthalpy.trim().replace(',', '.').toDoubleOrNull()
            val entropy = state.entropy.trim().replace(',', '.').toDoubleOrNull()
            val temperature = state.temperature.trim().replace(',', '.').toDoubleOrNull()
            val result = if (enthalpy != null && entropy != null && temperature != null) {
                GibbsCalculator.calculate(enthalpy, entropy, temperature)
            } else {
                null
            }
            state.copy(result = result, invalid = result == null)
        }
    }
}

@Composable
fun GibbsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GibbsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GibbsScreen(
        state = state,
        onEnthalpyChange = viewModel::onEnthalpyChange,
        onEntropyChange = viewModel::onEntropyChange,
        onTemperatureChange = viewModel::onTemperatureChange,
        onCalculate = viewModel::calculate,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun GibbsScreen(
    state: GibbsUiState,
    onEnthalpyChange: (String) -> Unit,
    onEntropyChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_gibbs_title),
        subtitle = stringResource(R.string.tool_gibbs_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        EngineeringCard(title = stringResource(R.string.gibbs_input)) {
            GibbsField(
                value = state.enthalpy,
                onValueChange = onEnthalpyChange,
                label = stringResource(R.string.gibbs_enthalpy),
            )
            GibbsField(
                value = state.entropy,
                onValueChange = onEntropyChange,
                label = stringResource(R.string.gibbs_entropy),
            )
            GibbsField(
                value = state.temperature,
                onValueChange = onTemperatureChange,
                label = stringResource(R.string.gibbs_temperature),
            )
            if (state.invalid) {
                EngineeringError(stringResource(R.string.gibbs_invalid))
            }
            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.gibbs_calculate))
            }
        }
        state.result?.let { result ->
            EngineeringCard(title = stringResource(R.string.gibbs_result)) {
                EngineeringResultRow(
                    label = stringResource(R.string.gibbs_delta_g),
                    value = "${formatGibbs(result.deltaG)} ${stringResource(R.string.unit_kj_mol)}",
                )
                EngineeringResultRow(
                    label = stringResource(R.string.gibbs_spontaneity),
                    value = stringResource(
                        when (result.spontaneity) {
                            Spontaneity.Spontaneous -> R.string.gibbs_spontaneous
                            Spontaneity.NonSpontaneous -> R.string.gibbs_non_spontaneous
                            Spontaneity.Equilibrium -> R.string.gibbs_equilibrium
                        },
                    ),
                )
            }
            Text(
                text = stringResource(R.string.gibbs_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun GibbsField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
    )
}

private fun formatGibbs(value: Double): String {
    val rounded = kotlin.math.round(value * 1_000_000.0) / 1_000_000.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
