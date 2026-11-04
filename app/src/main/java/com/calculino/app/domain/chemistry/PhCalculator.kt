package com.calculino.app.domain.chemistry

import kotlin.math.log10
import kotlin.math.sqrt

enum class SolutionType {
    StrongAcid,
    WeakAcid,
    StrongBase,
    WeakBase,
}

data class PhResult(
    val ph: Double,
    val poh: Double,
    val hydrogenConcentration: Double,
    val hydroxideConcentration: Double,
)

object PhCalculator {
    fun calculate(
        type: SolutionType,
        concentration: Double,
        constant: Double?,
    ): PhResult? {
        if (!concentration.isFinite() || concentration <= 0.0) return null
        val hydrogen: Double
        val hydroxide: Double
        when (type) {
            SolutionType.StrongAcid -> {
                hydrogen = concentration
                hydroxide = WATER_PRODUCT / hydrogen
            }
            SolutionType.WeakAcid -> {
                val ka = constant?.takeIf { it.isFinite() && it > 0.0 } ?: return null
                hydrogen = weakConcentration(ka, concentration)
                hydroxide = WATER_PRODUCT / hydrogen
            }
            SolutionType.StrongBase -> {
                hydroxide = concentration
                hydrogen = WATER_PRODUCT / hydroxide
            }
            SolutionType.WeakBase -> {
                val kb = constant?.takeIf { it.isFinite() && it > 0.0 } ?: return null
                hydroxide = weakConcentration(kb, concentration)
                hydrogen = WATER_PRODUCT / hydroxide
            }
        }
        return PhResult(
            ph = -log10(hydrogen),
            poh = -log10(hydroxide),
            hydrogenConcentration = hydrogen,
            hydroxideConcentration = hydroxide,
        )
    }

    private fun weakConcentration(constant: Double, concentration: Double): Double {
        val discriminant = constant * constant + 4.0 * constant * concentration
        return (-constant + sqrt(discriminant)) / 2.0
    }
}

private const val WATER_PRODUCT = 1.0e-14
