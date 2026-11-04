package com.calculino.app.domain.chemistry

enum class Spontaneity {
    Spontaneous,
    NonSpontaneous,
    Equilibrium,
}

data class GibbsResult(
    val deltaG: Double,
    val spontaneity: Spontaneity,
)

object GibbsCalculator {
    fun calculate(
        enthalpyKj: Double,
        entropyJoules: Double,
        temperatureKelvin: Double,
    ): GibbsResult? {
        if (!enthalpyKj.isFinite() || !entropyJoules.isFinite() || !temperatureKelvin.isFinite()) {
            return null
        }
        if (temperatureKelvin <= 0.0) return null
        val deltaG = enthalpyKj - temperatureKelvin * entropyJoules / 1000.0
        val spontaneity = when {
            deltaG < -EQUILIBRIUM_EPSILON -> Spontaneity.Spontaneous
            deltaG > EQUILIBRIUM_EPSILON -> Spontaneity.NonSpontaneous
            else -> Spontaneity.Equilibrium
        }
        return GibbsResult(deltaG = deltaG, spontaneity = spontaneity)
    }
}

private const val EQUILIBRIUM_EPSILON = 1e-9
