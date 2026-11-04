package com.calculino.app.ui.mathematics

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
import com.calculino.app.domain.math.ComplexNumber
import com.calculino.app.ui.engineering.EngineeringCard
import com.calculino.app.ui.engineering.EngineeringError
import com.calculino.app.ui.engineering.EngineeringResultRow
import com.calculino.app.ui.engineering.EngineeringToolScaffold
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ComplexComputation(
    val sum: ComplexNumber,
    val product: ComplexNumber,
    val firstMagnitude: Double,
    val firstPhase: Double,
    val secondMagnitude: Double,
    val secondPhase: Double,
)

data class ComplexUiState(
    val first: String = "3+4i",
    val second: String = "1-2i",
    val computation: ComplexComputation? = null,
    val invalid: Boolean = false,
)

class ComplexViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(ComplexUiState())
    val uiState: StateFlow<ComplexUiState> = mutableState.asStateFlow()

    fun onFirstChange(value: String) {
        mutableState.update { state -> state.copy(first = value, computation = null, invalid = false) }
    }

    fun onSecondChange(value: String) {
        mutableState.update { state -> state.copy(second = value, computation = null, invalid = false) }
    }

    fun calculate() {
        mutableState.update { state ->
            val first = ComplexNumber.parse(state.first)
            val second = ComplexNumber.parse(state.second)
            if (first == null || second == null) {
                state.copy(computation = null, invalid = true)
            } else {
                state.copy(
                    computation = ComplexComputation(
                        sum = first + second,
                        product = first * second,
                        firstMagnitude = first.magnitude,
                        firstPhase = first.phaseDegrees,
                        secondMagnitude = second.magnitude,
                        secondPhase = second.phaseDegrees,
                    ),
                    invalid = false,
                )
            }
        }
    }
}

@Composable
fun ComplexRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ComplexViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ComplexScreen(
        state = state,
        onFirstChange = viewModel::onFirstChange,
        onSecondChange = viewModel::onSecondChange,
        onCalculate = viewModel::calculate,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun ComplexScreen(
    state: ComplexUiState,
    onFirstChange: (String) -> Unit,
    onSecondChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_complex_title),
        subtitle = stringResource(R.string.tool_complex_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        EngineeringCard(title = stringResource(R.string.complex_input)) {
            OutlinedTextField(
                value = state.first,
                onValueChange = onFirstChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.complex_first)) },
                placeholder = { Text(stringResource(R.string.complex_hint)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
            OutlinedTextField(
                value = state.second,
                onValueChange = onSecondChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.complex_second)) },
                placeholder = { Text(stringResource(R.string.complex_hint)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
            if (state.invalid) {
                EngineeringError(stringResource(R.string.complex_invalid))
            }
            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.complex_calculate))
            }
        }
        state.computation?.let { computation ->
            EngineeringCard(title = stringResource(R.string.complex_results)) {
                EngineeringResultRow(
                    label = stringResource(R.string.complex_sum),
                    value = computation.sum.format(),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.complex_product),
                    value = computation.product.format(),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.complex_magnitude_first),
                    value = formatComplexValue(computation.firstMagnitude),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.complex_phase_first),
                    value = "${formatComplexValue(computation.firstPhase)}°",
                )
                EngineeringResultRow(
                    label = stringResource(R.string.complex_magnitude_second),
                    value = formatComplexValue(computation.secondMagnitude),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.complex_phase_second),
                    value = "${formatComplexValue(computation.secondPhase)}°",
                )
            }
            Text(
                text = stringResource(R.string.complex_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
    }
}

private fun formatComplexValue(value: Double): String {
    val rounded = kotlin.math.round(value * 1_000_000.0) / 1_000_000.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
