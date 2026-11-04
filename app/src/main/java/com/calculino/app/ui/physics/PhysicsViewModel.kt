package com.calculino.app.ui.physics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.calculino.app.domain.physics.PhysicsCalculator
import com.calculino.app.domain.physics.PhysicsField
import com.calculino.app.domain.physics.PhysicsResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PhysicsUiState(
    val inputs: Map<PhysicsField, String> = emptyMap(),
    val results: PhysicsResults = PhysicsResults(),
)

class PhysicsViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialInputs = PhysicsField.entries.associateWith { field ->
        savedStateHandle.get<String>(keyOf(field)).orEmpty()
    }

    private val _uiState = MutableStateFlow(buildState(initialInputs))
    val uiState = _uiState.asStateFlow()

    fun onInputChange(field: PhysicsField, value: String) {
        val sanitized = value.take(MAX_INPUT_LENGTH)
        val inputs = _uiState.value.inputs.toMutableMap().apply { this[field] = sanitized }
        savedStateHandle[keyOf(field)] = sanitized
        _uiState.value = buildState(inputs)
    }

    private fun buildState(inputs: Map<PhysicsField, String>): PhysicsUiState {
        val invalid = mutableSetOf<PhysicsField>()
        fun parse(field: PhysicsField): Double? {
            val raw = inputs[field].orEmpty().trim()
            if (raw.isEmpty()) return null
            val normalized = raw.replace(',', '.')
            val value = normalized.toDoubleOrNull()
            if (value == null) invalid += field
            return value
        }

        val initialVelocity = parse(PhysicsField.InitialVelocity)
        val kinematicsAcceleration = parse(PhysicsField.KinematicsAcceleration)
        val time = parse(PhysicsField.Time)
        val kinematics = if (initialVelocity != null && kinematicsAcceleration != null && time != null) {
            PhysicsCalculator.kinematics(initialVelocity, kinematicsAcceleration, time)
        } else {
            null
        }

        val forceMass = parse(PhysicsField.ForceMass)
        val forceAcceleration = parse(PhysicsField.ForceAcceleration)
        val force = if (forceMass != null && forceAcceleration != null) {
            PhysicsCalculator.force(forceMass, forceAcceleration)
        } else {
            null
        }

        val energyMass = parse(PhysicsField.EnergyMass)
        val energyVelocity = parse(PhysicsField.EnergyVelocity)
        val energyHeight = parse(PhysicsField.EnergyHeight)
        val kineticEnergy = if (energyMass != null && energyVelocity != null) {
            PhysicsCalculator.kineticEnergy(energyMass, energyVelocity)
        } else {
            null
        }
        val potentialEnergy = if (energyMass != null && energyHeight != null) {
            PhysicsCalculator.potentialEnergy(energyMass, energyHeight)
        } else {
            null
        }

        val voltage = parse(PhysicsField.Voltage)
        val resistance = parse(PhysicsField.Resistance)
        if (resistance != null && resistance <= 0.0) invalid += PhysicsField.Resistance
        val ohm = if (voltage != null && resistance != null && resistance > 0.0) {
            PhysicsCalculator.ohmsLaw(voltage, resistance)
        } else {
            null
        }

        return PhysicsUiState(
            inputs = inputs,
            results = PhysicsResults(
                finalVelocity = kinematics?.finalVelocity,
                distance = kinematics?.distance,
                force = force,
                kineticEnergy = kineticEnergy,
                potentialEnergy = potentialEnergy,
                current = ohm?.current,
                power = ohm?.power,
                invalidFields = invalid,
            ),
        )
    }

    private fun keyOf(field: PhysicsField): String = "physics_${field.name}"

    private companion object {
        const val MAX_INPUT_LENGTH = 16
    }
}
