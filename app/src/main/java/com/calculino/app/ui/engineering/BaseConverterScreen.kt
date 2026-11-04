package com.calculino.app.ui.engineering

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.calculino.app.domain.engineering.BaseValues
import com.calculino.app.domain.engineering.BitwiseOperation
import com.calculino.app.domain.engineering.BitwiseResult
import com.calculino.app.domain.engineering.applyBitwise
import com.calculino.app.domain.engineering.convertBases
import com.calculino.app.domain.engineering.supportedBases
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class BaseToolTab {
    Conversion,
    Bitwise,
}

data class BitwiseUiState(
    val left: String = "12",
    val right: String = "10",
    val base: Int = 10,
    val operation: BitwiseOperation = BitwiseOperation.And,
    val result: BitwiseResult? = applyBitwise("12", "10", 10, BitwiseOperation.And),
    val isInvalid: Boolean = false,
)

data class BaseConverterUiState(
    val tab: BaseToolTab = BaseToolTab.Conversion,
    val input: String = "42",
    val base: Int = 10,
    val values: BaseValues? = convertBases("42", 10),
    val isInvalid: Boolean = false,
    val bitwise: BitwiseUiState = BitwiseUiState(),
)

class BaseConverterViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(BaseConverterUiState())
    val uiState: StateFlow<BaseConverterUiState> = mutableState.asStateFlow()

    fun onTabChange(tab: BaseToolTab) {
        mutableState.update { state -> state.copy(tab = tab) }
    }

    fun onInputChange(value: String) {
        mutableState.update { state -> state.updated(input = value) }
    }

    fun onBaseChange(base: Int) {
        mutableState.update { state -> state.updated(base = base) }
    }

    fun onBitwiseLeftChange(value: String) {
        mutableState.update { state -> state.updatedBitwise(left = value) }
    }

    fun onBitwiseRightChange(value: String) {
        mutableState.update { state -> state.updatedBitwise(right = value) }
    }

    fun onBitwiseBaseChange(base: Int) {
        mutableState.update { state -> state.updatedBitwise(base = base) }
    }

    fun onBitwiseOperationChange(operation: BitwiseOperation) {
        mutableState.update { state -> state.updatedBitwise(operation = operation) }
    }

    private fun BaseConverterUiState.updated(
        input: String = this.input,
        base: Int = this.base,
    ): BaseConverterUiState {
        val values = convertBases(input, base)
        return copy(
            input = input,
            base = base,
            values = values,
            isInvalid = input.isNotBlank() && values == null,
        )
    }

    private fun BaseConverterUiState.updatedBitwise(
        left: String = bitwise.left,
        right: String = bitwise.right,
        base: Int = bitwise.base,
        operation: BitwiseOperation = bitwise.operation,
    ): BaseConverterUiState {
        val result = applyBitwise(left, right, base, operation)
        return copy(
            bitwise = BitwiseUiState(
                left = left,
                right = right,
                base = base,
                operation = operation,
                result = result,
                isInvalid = result == null,
            ),
        )
    }
}

@Composable
fun BaseConverterRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: BaseConverterViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BaseConverterScreen(
        state = state,
        onTabChange = viewModel::onTabChange,
        onInputChange = viewModel::onInputChange,
        onBaseChange = viewModel::onBaseChange,
        onBitwiseLeftChange = viewModel::onBitwiseLeftChange,
        onBitwiseRightChange = viewModel::onBitwiseRightChange,
        onBitwiseBaseChange = viewModel::onBitwiseBaseChange,
        onBitwiseOperationChange = viewModel::onBitwiseOperationChange,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun BaseConverterScreen(
    state: BaseConverterUiState,
    onTabChange: (BaseToolTab) -> Unit,
    onInputChange: (String) -> Unit,
    onBaseChange: (Int) -> Unit,
    onBitwiseLeftChange: (String) -> Unit,
    onBitwiseRightChange: (String) -> Unit,
    onBitwiseBaseChange: (Int) -> Unit,
    onBitwiseOperationChange: (BitwiseOperation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_base_title),
        subtitle = stringResource(R.string.tool_base_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        TabRow(
            selectedTabIndex = state.tab.ordinal,
            containerColor = palette.surface,
            contentColor = palette.title,
        ) {
            BaseToolTab.entries.forEach { tab ->
                Tab(
                    selected = state.tab == tab,
                    onClick = { onTabChange(tab) },
                    text = {
                        Text(
                            text = stringResource(
                                if (tab == BaseToolTab.Conversion) {
                                    R.string.base_tab_conversion
                                } else {
                                    R.string.base_tab_bitwise
                                },
                            ),
                        )
                    },
                )
            }
        }
        when (state.tab) {
            BaseToolTab.Conversion -> ConversionContent(
                state = state,
                onInputChange = onInputChange,
                onBaseChange = onBaseChange,
            )
            BaseToolTab.Bitwise -> BitwiseContent(
                state = state.bitwise,
                onLeftChange = onBitwiseLeftChange,
                onRightChange = onBitwiseRightChange,
                onBaseChange = onBitwiseBaseChange,
                onOperationChange = onBitwiseOperationChange,
            )
        }
    }
}

@Composable
private fun ConversionContent(
    state: BaseConverterUiState,
    onInputChange: (String) -> Unit,
    onBaseChange: (Int) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringCard(title = stringResource(R.string.base_input_title)) {
        BaseChips(selected = state.base, onSelect = onBaseChange)
        OutlinedTextField(
            value = state.input,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.base_input_label, state.base)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        )
        if (state.isInvalid) {
            EngineeringError(stringResource(R.string.base_invalid))
        }
    }
    state.values?.let { values ->
        EngineeringCard(title = stringResource(R.string.base_results_title)) {
            BaseResultRows(values = values)
        }
    }
    Text(
        text = stringResource(R.string.base_live_note),
        style = MaterialTheme.typography.bodySmall,
        color = palette.textSecondary,
    )
}

@Composable
private fun BitwiseContent(
    state: BitwiseUiState,
    onLeftChange: (String) -> Unit,
    onRightChange: (String) -> Unit,
    onBaseChange: (Int) -> Unit,
    onOperationChange: (BitwiseOperation) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringCard(title = stringResource(R.string.bitwise_title)) {
        BaseChips(selected = state.base, onSelect = onBaseChange)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            BitwiseOperation.entries.forEach { operation ->
                FilterChip(
                    selected = state.operation == operation,
                    onClick = { onOperationChange(operation) },
                    label = { Text(bitwiseLabel(operation), maxLines = 1) },
                )
            }
        }
        OutlinedTextField(
            value = state.left,
            onValueChange = onLeftChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.bitwise_left)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        )
        if (state.operation != BitwiseOperation.Not) {
            OutlinedTextField(
                value = state.right,
                onValueChange = onRightChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.bitwise_right)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
        }
        if (state.isInvalid) {
            EngineeringError(stringResource(R.string.bitwise_invalid))
        }
    }
    state.result?.let { result ->
        EngineeringCard(title = stringResource(R.string.bitwise_result)) {
            BaseResultRows(values = result.bases)
        }
    }
    Text(
        text = stringResource(R.string.bitwise_note),
        style = MaterialTheme.typography.bodySmall,
        color = palette.textSecondary,
    )
}

@Composable
private fun BaseChips(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        supportedBases.forEach { base ->
            FilterChip(
                selected = selected == base,
                onClick = { onSelect(base) },
                label = { Text(stringResource(baseLabelRes(base)), maxLines = 1) },
            )
        }
    }
}

@Composable
private fun BaseResultRows(values: BaseValues) {
    EngineeringResultRow(label = stringResource(R.string.base_decimal), value = values.decimal)
    EngineeringResultRow(label = stringResource(R.string.base_binary), value = values.binary)
    EngineeringResultRow(label = stringResource(R.string.base_octal), value = values.octal)
    EngineeringResultRow(label = stringResource(R.string.base_hexadecimal), value = values.hexadecimal)
}

private fun baseLabelRes(base: Int): Int = when (base) {
    2 -> R.string.base_binary
    8 -> R.string.base_octal
    16 -> R.string.base_hexadecimal
    else -> R.string.base_decimal
}

private fun bitwiseLabel(operation: BitwiseOperation): String = when (operation) {
    BitwiseOperation.And -> "AND"
    BitwiseOperation.Or -> "OR"
    BitwiseOperation.Xor -> "XOR"
    BitwiseOperation.Not -> "NOT"
    BitwiseOperation.ShiftLeft -> "SHL"
    BitwiseOperation.ShiftRight -> "SHR"
}
