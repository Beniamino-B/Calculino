package com.calculino.app.domain.physics

enum class PhysicsField {
    InitialVelocity,
    KinematicsAcceleration,
    Time,
    ForceMass,
    ForceAcceleration,
    EnergyMass,
    EnergyVelocity,
    EnergyHeight,
    Voltage,
    Resistance,
}

data class KinematicsResult(
    val finalVelocity: Double,
    val distance: Double,
)

data class OhmResult(
    val current: Double,
    val power: Double,
)

data class PhysicsResults(
    val finalVelocity: Double? = null,
    val distance: Double? = null,
    val force: Double? = null,
    val kineticEnergy: Double? = null,
    val potentialEnergy: Double? = null,
    val current: Double? = null,
    val power: Double? = null,
    val invalidFields: Set<PhysicsField> = emptySet(),
)

object PhysicsCalculator {
    const val GRAVITY = 9.81

    fun kinematics(
        initialVelocity: Double,
        acceleration: Double,
        time: Double,
    ): KinematicsResult = KinematicsResult(
        finalVelocity = initialVelocity + acceleration * time,
        distance = initialVelocity * time + 0.5 * acceleration * time * time,
    )

    fun force(mass: Double, acceleration: Double): Double = mass * acceleration

    fun kineticEnergy(mass: Double, velocity: Double): Double =
        0.5 * mass * velocity * velocity

    fun potentialEnergy(mass: Double, height: Double): Double =
        mass * GRAVITY * height

    fun ohmsLaw(voltage: Double, resistance: Double): OhmResult? {
        if (resistance <= 0.0) return null
        val current = voltage / resistance
        return OhmResult(current = current, power = voltage * current)
    }
}
