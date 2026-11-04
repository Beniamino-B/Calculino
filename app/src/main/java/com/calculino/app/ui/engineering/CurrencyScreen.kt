package com.calculino.app.ui.engineering

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.calculino.app.R
import com.calculino.app.data.currency.CurrencyRepository
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val currencyCodes = listOf(
    "USD",
    "EUR",
    "MXN",
    "COP",
    "ARS",
    "CLP",
    "PEN",
    "BRL",
    "GBP",
    "JPY",
    "CAD",
    "AUD",
    "CHF",
)

data class CurrencyUiState(
    val amount: String = "1",
    val from: String = "USD",
    val to: String = "EUR",
    val rates: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val manualRate: String = "",
)

class CurrencyViewModel(
    private val repository: CurrencyRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadRates(mutableState.value.from)
    }

    fun onAmountChange(value: String) {
        mutableState.update { state -> state.copy(amount = value) }
    }

    fun onFromChange(code: String) {
        if (mutableState.value.from == code) return
        mutableState.update { state ->
            state.copy(
                from = code,
                rates = emptyMap(),
                hasError = false,
                manualRate = "",
            )
        }
        loadRates(code)
    }

    fun onToChange(code: String) {
        mutableState.update { state -> state.copy(to = code) }
    }

    fun onManualRateChange(value: String) {
        mutableState.update { state -> state.copy(manualRate = value) }
    }

    fun retry() {
        loadRates(mutableState.value.from)
    }

    private fun loadRates(base: String) {
        loadJob?.cancel()
        mutableState.update { state ->
            state.copy(isLoading = true, hasError = false, rates = emptyMap())
        }
        loadJob = viewModelScope.launch {
            repository.latestRates(base)
                .onSuccess { rates ->
                    mutableState.update { state ->
                        if (state.from == base) {
                            state.copy(rates = rates, isLoading = false, hasError = false)
                        } else {
                            state
                        }
                    }
                }
                .onFailure {
                    mutableState.update { state ->
                        if (state.from == base) {
                            state.copy(isLoading = false, hasError = true, rates = emptyMap())
                        } else {
                            state
                        }
                    }
                }
        }
    }

    companion object {
        fun factory(repository: CurrencyRepository) = viewModelFactory {
            initializer { CurrencyViewModel(repository) }
        }
    }
}

@Composable
fun CurrencyRoute(
    repository: CurrencyRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val factory = remember(repository) { CurrencyViewModel.factory(repository) }
    val viewModel: CurrencyViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CurrencyScreen(
        state = state,
        onAmountChange = viewModel::onAmountChange,
        onFromChange = viewModel::onFromChange,
        onToChange = viewModel::onToChange,
        onManualRateChange = viewModel::onManualRateChange,
        onRetry = viewModel::retry,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySheet(
    repository: CurrencyRepository,
    onDismiss: () -> Unit,
) {
    val factory = remember(repository) { CurrencyViewModel.factory(repository) }
    val viewModel: CurrencyViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalKawaiiPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = CalculinoSpacing.lg,
                    end = CalculinoSpacing.lg,
                    bottom = CalculinoSpacing.xl,
                ),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.tool_currency_title),
                style = MaterialTheme.typography.headlineSmall,
                color = palette.title,
            )
            Text(
                text = stringResource(R.string.tool_currency_description),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            CurrencyContent(
                state = state,
                onAmountChange = viewModel::onAmountChange,
                onFromChange = viewModel::onFromChange,
                onToChange = viewModel::onToChange,
                onManualRateChange = viewModel::onManualRateChange,
                onRetry = viewModel::retry,
            )
        }
    }
}

@Composable
fun CurrencyScreen(
    state: CurrencyUiState,
    onAmountChange: (String) -> Unit,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onManualRateChange: (String) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_currency_title),
        subtitle = stringResource(R.string.tool_currency_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        CurrencyContent(
            state = state,
            onAmountChange = onAmountChange,
            onFromChange = onFromChange,
            onToChange = onToChange,
            onManualRateChange = onManualRateChange,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun CurrencyContent(
    state: CurrencyUiState,
    onAmountChange: (String) -> Unit,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onManualRateChange: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val amountValue = state.amount.trim().replace(',', '.').toDoubleOrNull()
    val rate = if (state.hasError) {
        state.manualRate.trim().replace(',', '.').toDoubleOrNull()
    } else {
        state.rates[state.to]
    }
    val converted = if (amountValue != null && rate != null) amountValue * rate else null

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        EngineeringCard(title = stringResource(R.string.currency_input_title)) {
            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.currency_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.amount.isNotBlank() && amountValue == null,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                CurrencyDropdown(
                    label = stringResource(R.string.currency_from),
                    selectedCode = state.from,
                    onSelected = onFromChange,
                    modifier = Modifier.weight(1f),
                )
                CurrencyDropdown(
                    label = stringResource(R.string.currency_to),
                    selectedCode = state.to,
                    onSelected = onToChange,
                    modifier = Modifier.weight(1f),
                )
            }
            if (state.amount.isNotBlank() && amountValue == null) {
                EngineeringError(stringResource(R.string.currency_invalid_amount))
            }
        }
        if (state.isLoading) {
            EngineeringCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(CalculinoSizes.iconMedium),
                        strokeWidth = CalculinoSizes.focusBorder,
                    )
                    Text(
                        text = stringResource(R.string.currency_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            }
        }
        if (state.hasError) {
            EngineeringCard(title = stringResource(R.string.currency_error_title)) {
                EngineeringError(stringResource(R.string.currency_error_message))
                OutlinedTextField(
                    value = state.manualRate,
                    onValueChange = onManualRateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.currency_manual_rate)) },
                    placeholder = { Text(stringResource(R.string.currency_manual_rate_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    isError = state.manualRate.isNotBlank() && rate == null,
                )
                if (state.manualRate.isNotBlank() && rate == null) {
                    EngineeringError(stringResource(R.string.currency_invalid_manual))
                }
                Text(
                    text = stringResource(
                        R.string.currency_manual_rate_description,
                        state.from,
                        state.to,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
        if (converted != null && rate != null) {
            EngineeringCard(title = stringResource(R.string.currency_result)) {
                Text(
                    text = stringResource(
                        R.string.currency_result_format,
                        formatAmount(amountValue ?: 0.0),
                        state.from,
                        formatAmount(converted),
                        state.to,
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.title,
                )
                Text(
                    text = stringResource(
                        R.string.currency_rate_format,
                        state.from,
                        formatRate(rate),
                        state.to,
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = palette.textSecondary,
                )
                Text(
                    text = stringResource(
                        if (state.hasError) {
                            R.string.currency_manual_source
                        } else {
                            R.string.currency_source
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selectedCode: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = currencyName(selectedCode),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            textStyle = MaterialTheme.typography.bodyMedium,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            currencyCodes.forEach { code ->
                DropdownMenuItem(
                    text = { Text(currencyName(code)) },
                    onClick = {
                        expanded = false
                        onSelected(code)
                    },
                )
            }
        }
    }
}

private fun currencyName(code: String): String {
    val name = runCatching { Currency.getInstance(code).displayName }.getOrNull()
    return if (name.isNullOrBlank()) code else "$code · $name"
}

private fun formatAmount(value: Double): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value)
}

private fun formatRate(value: Double): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 6
    }.format(value)
}
