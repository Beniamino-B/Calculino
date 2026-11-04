package com.calculino.app.ui.physics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.physics.PhysicsField
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.science.ScienceToolHeader
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun PhysicsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PhysicsViewModel = viewModel(
        initializer = { PhysicsViewModel(createSavedStateHandle()) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PhysicsScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun PhysicsScreen(
    state: PhysicsUiState,
    onInputChange: (PhysicsField, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val scroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        KawaiiBackdrop()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = CalculinoSizes.screenMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CalculinoSpacing.lg)
                .imePadding(),
        ) {
            ScienceToolHeader(
                title = stringResource(R.string.physics_title),
                subtitle = stringResource(R.string.physics_subtitle),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
                    .padding(vertical = CalculinoSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            ) {
                PhysicsCard(
                    title = stringResource(R.string.physics_kinematics_title),
                    description = stringResource(R.string.physics_kinematics_description),
                ) {
                    PhysicsInput(
                        field = PhysicsField.InitialVelocity,
                        label = stringResource(R.string.physics_initial_velocity),
                        value = state.inputs[PhysicsField.InitialVelocity].orEmpty(),
                        isError = PhysicsField.InitialVelocity in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    PhysicsInput(
                        field = PhysicsField.KinematicsAcceleration,
                        label = stringResource(R.string.physics_acceleration),
                        value = state.inputs[PhysicsField.KinematicsAcceleration].orEmpty(),
                        isError = PhysicsField.KinematicsAcceleration in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    PhysicsInput(
                        field = PhysicsField.Time,
                        label = stringResource(R.string.physics_time),
                        value = state.inputs[PhysicsField.Time].orEmpty(),
                        isError = PhysicsField.Time in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    val velocity = state.results.finalVelocity
                    val distance = state.results.distance
                    if (velocity != null) {
                        PhysicsResultRow(
                            label = stringResource(R.string.physics_final_velocity),
                            value = formatPhysicsValue(velocity),
                            unit = stringResource(R.string.unit_meter_per_second),
                        )
                    }
                    if (distance != null) {
                        PhysicsResultRow(
                            label = stringResource(R.string.physics_distance),
                            value = formatPhysicsValue(distance),
                            unit = stringResource(R.string.unit_meter),
                        )
                    }
                    if (velocity == null && distance == null) {
                        EmptyResultHint()
                    }
                }
                PhysicsCard(
                    title = stringResource(R.string.physics_force_title),
                    description = stringResource(R.string.physics_force_description),
                ) {
                    PhysicsInput(
                        field = PhysicsField.ForceMass,
                        label = stringResource(R.string.physics_mass),
                        value = state.inputs[PhysicsField.ForceMass].orEmpty(),
                        isError = PhysicsField.ForceMass in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    PhysicsInput(
                        field = PhysicsField.ForceAcceleration,
                        label = stringResource(R.string.physics_acceleration),
                        value = state.inputs[PhysicsField.ForceAcceleration].orEmpty(),
                        isError = PhysicsField.ForceAcceleration in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    val force = state.results.force
                    if (force != null) {
                        PhysicsResultRow(
                            label = stringResource(R.string.physics_force_result),
                            value = formatPhysicsValue(force),
                            unit = stringResource(R.string.unit_newton),
                        )
                    } else {
                        EmptyResultHint()
                    }
                }
                PhysicsCard(
                    title = stringResource(R.string.physics_energy_title),
                    description = stringResource(R.string.physics_energy_description),
                ) {
                    PhysicsInput(
                        field = PhysicsField.EnergyMass,
                        label = stringResource(R.string.physics_mass),
                        value = state.inputs[PhysicsField.EnergyMass].orEmpty(),
                        isError = PhysicsField.EnergyMass in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    PhysicsInput(
                        field = PhysicsField.EnergyVelocity,
                        label = stringResource(R.string.physics_velocity),
                        value = state.inputs[PhysicsField.EnergyVelocity].orEmpty(),
                        isError = PhysicsField.EnergyVelocity in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    PhysicsInput(
                        field = PhysicsField.EnergyHeight,
                        label = stringResource(R.string.physics_height),
                        value = state.inputs[PhysicsField.EnergyHeight].orEmpty(),
                        isError = PhysicsField.EnergyHeight in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    val kinetic = state.results.kineticEnergy
                    val potential = state.results.potentialEnergy
                    if (kinetic != null) {
                        PhysicsResultRow(
                            label = stringResource(R.string.physics_kinetic_energy),
                            value = formatPhysicsValue(kinetic),
                            unit = stringResource(R.string.unit_joule),
                        )
                    }
                    if (potential != null) {
                        PhysicsResultRow(
                            label = stringResource(R.string.physics_potential_energy),
                            value = formatPhysicsValue(potential),
                            unit = stringResource(R.string.unit_joule),
                        )
                    }
                    if (kinetic == null && potential == null) {
                        EmptyResultHint()
                    }
                }
                PhysicsCard(
                    title = stringResource(R.string.physics_ohm_title),
                    description = stringResource(R.string.physics_ohm_description),
                ) {
                    PhysicsInput(
                        field = PhysicsField.Voltage,
                        label = stringResource(R.string.physics_voltage),
                        value = state.inputs[PhysicsField.Voltage].orEmpty(),
                        isError = PhysicsField.Voltage in state.results.invalidFields,
                        onValueChange = onInputChange,
                    )
                    PhysicsInput(
                        field = PhysicsField.Resistance,
                        label = stringResource(R.string.physics_resistance),
                        value = state.inputs[PhysicsField.Resistance].orEmpty(),
                        isError = PhysicsField.Resistance in state.results.invalidFields,
                        errorText = stringResource(R.string.physics_resistance_error),
                        onValueChange = onInputChange,
                    )
                    val current = state.results.current
                    val power = state.results.power
                    if (current != null) {
                        PhysicsResultRow(
                            label = stringResource(R.string.physics_current),
                            value = formatPhysicsValue(current),
                            unit = stringResource(R.string.unit_ampere),
                        )
                    }
                    if (power != null) {
                        PhysicsResultRow(
                            label = stringResource(R.string.physics_power),
                            value = formatPhysicsValue(power),
                            unit = stringResource(R.string.unit_watt),
                        )
                    }
                    if (current == null && power == null) {
                        EmptyResultHint()
                    }
                }
                Text(
                    text = stringResource(R.string.physics_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                Spacer(modifier = Modifier.height(CalculinoSpacing.xxl))
            }
        }
    }
}

@Composable
private fun PhysicsCard(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.title,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            content()
        }
    }
}

@Composable
private fun PhysicsInput(
    field: PhysicsField,
    label: String,
    value: String,
    isError: Boolean,
    onValueChange: (PhysicsField, String) -> Unit,
    errorText: String = stringResource(R.string.physics_invalid_number),
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(field, it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = if (isError) {
            { Text(errorText) }
        } else {
            null
        },
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
        ),
    )
}

@Composable
private fun PhysicsResultRow(
    label: String,
    value: String,
    unit: String,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surfaceMuted,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CalculinoSpacing.md,
                    vertical = CalculinoSpacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun EmptyResultHint() {
    Text(
        text = stringResource(R.string.physics_empty_result),
        style = MaterialTheme.typography.bodySmall,
        color = LocalKawaiiPalette.current.textSecondary,
    )
}

private fun formatPhysicsValue(value: Double): String {
    if (!value.isFinite()) return "—"
    val rounded = BigDecimal.valueOf(value)
        .setScale(3, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    val text = if (rounded.compareTo(BigDecimal.ZERO) == 0) "0" else rounded.toPlainString()
    return text.replace('-', '−')
}
