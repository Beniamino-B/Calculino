package com.calculino.app.ui.mathematics

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
import com.calculino.app.domain.math.StatisticsCalculator
import com.calculino.app.domain.math.StatisticsOutcome
import com.calculino.app.ui.engineering.EngineeringCard
import com.calculino.app.ui.engineering.EngineeringError
import com.calculino.app.ui.engineering.EngineeringResultRow
import com.calculino.app.ui.engineering.EngineeringToolScaffold
import java.math.BigInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StatisticsUiState(
    val dataset: String = "2, 4, 4, 4, 5, 5, 7, 9",
    val n: String = "5",
    val r: String = "2",
    val result: StatisticsOutcome? = null,
    val combinations: BigInteger? = null,
    val permutations: BigInteger? = null,
    val combinatoricsInvalid: Boolean = false,
)

class StatisticsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = mutableState.asStateFlow()

    fun onDatasetChange(value: String) {
        mutableState.update { state -> state.copy(dataset = value, result = null) }
    }

    fun onNChange(value: String) {
        mutableState.update { state ->
            state.copy(n = value, combinations = null, permutations = null, combinatoricsInvalid = false)
        }
    }

    fun onRChange(value: String) {
        mutableState.update { state ->
            state.copy(r = value, combinations = null, permutations = null, combinatoricsInvalid = false)
        }
    }

    fun calculate() {
        mutableState.update { state ->
            val n = state.n.trim().toIntOrNull()
            val r = state.r.trim().toIntOrNull()
            val combinations = if (n != null && r != null) {
                StatisticsCalculator.combinations(n, r)
            } else {
                null
            }
            val permutations = if (n != null && r != null) {
                StatisticsCalculator.permutations(n, r)
            } else {
                null
            }
            state.copy(
                result = StatisticsCalculator.calculate(state.dataset),
                combinations = combinations,
                permutations = permutations,
                combinatoricsInvalid = combinations == null || permutations == null,
            )
        }
    }
}

@Composable
fun StatisticsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StatisticsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsScreen(
        state = state,
        onDatasetChange = viewModel::onDatasetChange,
        onNChange = viewModel::onNChange,
        onRChange = viewModel::onRChange,
        onCalculate = viewModel::calculate,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onDatasetChange: (String) -> Unit,
    onNChange: (String) -> Unit,
    onRChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_statistics_title),
        subtitle = stringResource(R.string.tool_statistics_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        EngineeringCard(title = stringResource(R.string.statistics_dataset)) {
            OutlinedTextField(
                value = state.dataset,
                onValueChange = onDatasetChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.statistics_dataset_label)) },
                placeholder = { Text(stringResource(R.string.statistics_dataset_hint)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
        }
        EngineeringCard(title = stringResource(R.string.statistics_combinatorics)) {
            OutlinedTextField(
                value = state.n,
                onValueChange = onNChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.statistics_n)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = state.r,
                onValueChange = onRChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.statistics_r)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.dataset.isNotBlank(),
        ) {
            Text(stringResource(R.string.statistics_calculate))
        }
        when (val result = state.result) {
            is StatisticsOutcome.Success -> {
                val statistics = result.result
                EngineeringCard(title = stringResource(R.string.statistics_results)) {
                    EngineeringResultRow(
                        label = stringResource(R.string.statistics_count),
                        value = statistics.count.toString(),
                    )
                    EngineeringResultRow(
                        label = stringResource(R.string.statistics_mean),
                        value = formatStatistics(statistics.mean),
                    )
                    EngineeringResultRow(
                        label = stringResource(R.string.statistics_median),
                        value = formatStatistics(statistics.median),
                    )
                    EngineeringResultRow(
                        label = stringResource(R.string.statistics_mode),
                        value = if (statistics.mode.isEmpty()) {
                            stringResource(R.string.statistics_no_mode)
                        } else {
                            statistics.mode.joinToString(", ") { value -> formatStatistics(value) }
                        },
                    )
                    EngineeringResultRow(
                        label = stringResource(R.string.statistics_variance),
                        value = formatStatistics(statistics.variance),
                    )
                    EngineeringResultRow(
                        label = stringResource(R.string.statistics_standard_deviation),
                        value = formatStatistics(statistics.standardDeviation),
                    )
                    EngineeringResultRow(
                        label = stringResource(R.string.statistics_range),
                        value = "${formatStatistics(statistics.minimum)} – ${formatStatistics(statistics.maximum)}",
                    )
                }
                EngineeringCard(title = stringResource(R.string.statistics_combinatorics)) {
                    if (state.combinatoricsInvalid) {
                        EngineeringError(stringResource(R.string.statistics_invalid_nr))
                    } else {
                        EngineeringResultRow(
                            label = stringResource(R.string.statistics_combinations, state.n, state.r),
                            value = state.combinations?.toString().orEmpty(),
                        )
                        EngineeringResultRow(
                            label = stringResource(R.string.statistics_permutations, state.n, state.r),
                            value = state.permutations?.toString().orEmpty(),
                        )
                    }
                }
            }
            is StatisticsOutcome.InvalidData -> EngineeringCard(
                title = stringResource(R.string.statistics_invalid_data),
            ) {
                EngineeringError(stringResource(R.string.statistics_invalid_dataset))
            }
            null -> Unit
        }
        Text(
            text = stringResource(R.string.statistics_note),
            style = MaterialTheme.typography.bodySmall,
            color = com.calculino.app.ui.theme.LocalKawaiiPalette.current.textSecondary,
        )
    }
}

private fun formatStatistics(value: Double): String {
    val rounded = kotlin.math.round(value * 1_000_000.0) / 1_000_000.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
