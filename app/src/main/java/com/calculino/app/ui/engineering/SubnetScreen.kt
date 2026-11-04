package com.calculino.app.ui.engineering

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import com.calculino.app.domain.engineering.SubnetInfo
import com.calculino.app.domain.engineering.SubnetResult
import com.calculino.app.domain.engineering.calculateSubnet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SubnetUiState(
    val cidr: String,
    val info: SubnetInfo?,
    val invalidIp: Boolean,
    val invalidMask: Boolean,
)

class SubnetViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(subnetState("192.168.1.10/24"))
    val uiState: StateFlow<SubnetUiState> = mutableState.asStateFlow()

    fun onCidrChange(value: String) {
        mutableState.update { subnetState(value) }
    }
}

private fun subnetState(cidr: String): SubnetUiState {
    val outcome = calculateSubnet(cidr, "")
    return SubnetUiState(
        cidr = cidr,
        info = (outcome as? SubnetResult.Success)?.info,
        invalidIp = outcome == SubnetResult.InvalidIp,
        invalidMask = outcome == SubnetResult.InvalidMask,
    )
}

@Composable
fun SubnetRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SubnetViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SubnetScreen(
        state = state,
        onCidrChange = viewModel::onCidrChange,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun SubnetScreen(
    state: SubnetUiState,
    onCidrChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_subnet_title),
        subtitle = stringResource(R.string.tool_subnet_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        EngineeringCard(title = stringResource(R.string.subnet_input_title)) {
            OutlinedTextField(
                value = state.cidr,
                onValueChange = onCidrChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.subnet_cidr_label)) },
                placeholder = { Text(stringResource(R.string.subnet_cidr_hint)) },
                singleLine = true,
                isError = state.invalidIp || state.invalidMask,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
            if (state.invalidIp) {
                EngineeringError(stringResource(R.string.subnet_invalid_ip))
            }
            if (state.invalidMask) {
                EngineeringError(stringResource(R.string.subnet_invalid_mask))
            }
        }
        state.info?.let { info ->
            EngineeringCard(title = stringResource(R.string.subnet_results_title)) {
                EngineeringResultRow(
                    label = stringResource(R.string.subnet_prefix),
                    value = "/${info.prefixLength}",
                )
                EngineeringResultRow(
                    label = stringResource(R.string.subnet_network),
                    value = info.network,
                )
                EngineeringResultRow(
                    label = stringResource(R.string.subnet_broadcast),
                    value = info.broadcast,
                )
                EngineeringResultRow(
                    label = stringResource(R.string.subnet_host_range),
                    value = "${info.firstHost} – ${info.lastHost}",
                )
                EngineeringResultRow(
                    label = stringResource(R.string.subnet_usable_hosts),
                    value = info.usableHosts.toString(),
                )
                EngineeringResultRow(
                    label = stringResource(R.string.subnet_mask_result),
                    value = info.subnetMask,
                )
                EngineeringResultRow(
                    label = stringResource(R.string.subnet_wildcard),
                    value = info.wildcardMask,
                )
            }
        }
    }
}
