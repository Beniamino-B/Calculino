package com.calculino.app.domain.chemistry

import com.calculino.app.domain.periodic.ChemicalElement
import com.calculino.app.domain.periodic.PeriodicTable
import kotlin.math.abs
import kotlin.math.round

data class EmpiricalFormulaResult(
    val empiricalFormula: String,
    val molecularFormula: String?,
    val empiricalMass: Double,
    val multiplier: Int,
)

enum class EmpiricalFormulaError {
    EmptyComposition,
    InvalidAmount,
    UnknownElement,
    TooManyElements,
    ImpossibleMoles,
    ImpossibleRatio,
    InvalidMolarMass,
}

sealed interface EmpiricalFormulaOutcome {
    data class Success(val result: EmpiricalFormulaResult) : EmpiricalFormulaOutcome
    data class Failure(
        val error: EmpiricalFormulaError,
        val symbol: String? = null,
    ) : EmpiricalFormulaOutcome
}

object EmpiricalFormulaFinder {
    fun find(
        amounts: Map<String, Double>,
        molarMass: Double? = null,
    ): EmpiricalFormulaOutcome {
        if (amounts.isEmpty()) return EmpiricalFormulaOutcome.Failure(EmpiricalFormulaError.EmptyComposition)
        val elements = mutableListOf<Pair<ChemicalElement, Double>>()
        amounts.forEach { (symbol, amount) ->
            if (!amount.isFinite() || amount <= 0.0) {
                return EmpiricalFormulaOutcome.Failure(EmpiricalFormulaError.InvalidAmount)
            }
            val normalized = symbol.trim().replaceFirstChar { it.uppercase() }
            val element = PeriodicTable.bySymbol[normalized]
                ?: return EmpiricalFormulaOutcome.Failure(
                    EmpiricalFormulaError.UnknownElement,
                    symbol = normalized,
                )
            elements += element to amount
        }
        if (elements.size > 12) {
            return EmpiricalFormulaOutcome.Failure(EmpiricalFormulaError.TooManyElements)
        }
        val moles = elements.map { (element, amount) ->
            element to amount / element.atomicMass
        }
        val smallest = moles.minOf { (_, value) -> value }
        if (smallest <= 0.0) return EmpiricalFormulaOutcome.Failure(EmpiricalFormulaError.ImpossibleMoles)
        val ratios = moles.map { (element, value) -> element to value / smallest }
        val multiplier = findMultiplier(ratios.map { (_, ratio) -> ratio })
            ?: return EmpiricalFormulaOutcome.Failure(EmpiricalFormulaError.ImpossibleRatio)
        val counts = ratios.associate { (element, ratio) ->
            element to (round(ratio * multiplier).toInt().coerceAtLeast(1))
        }
        val empiricalMass = counts.entries.sumOf { (element, count) ->
            element.atomicMass * count
        }
        val empirical = formatFormula(counts)
        val molecularMultiplier = if (molarMass != null) {
            if (!molarMass.isFinite() || molarMass <= 0.0) {
                return EmpiricalFormulaOutcome.Failure(EmpiricalFormulaError.InvalidMolarMass)
            }
            round(molarMass / empiricalMass).toInt().coerceAtLeast(1)
        } else {
            1
        }
        val molecular = if (molarMass != null) {
            formatFormula(counts.mapValues { (_, count) -> count * molecularMultiplier })
        } else {
            null
        }
        return EmpiricalFormulaOutcome.Success(
            EmpiricalFormulaResult(
                empiricalFormula = empirical,
                molecularFormula = molecular,
                empiricalMass = empiricalMass,
                multiplier = molecularMultiplier,
            ),
        )
    }

    private fun findMultiplier(ratios: List<Double>): Int? {
        for (multiplier in 1..MAX_MULTIPLIER) {
            val aligned = ratios.all { ratio ->
                abs(ratio * multiplier - round(ratio * multiplier)) < RATIO_TOLERANCE
            }
            if (aligned) return multiplier
        }
        return null
    }

    private fun formatFormula(counts: Map<ChemicalElement, Int>): String {
        val ordered = counts.entries.sortedWith(
            compareBy<Map.Entry<ChemicalElement, Int>> { (element, _) ->
                when (element.symbol) {
                    "C" -> 0
                    "H" -> 1
                    else -> 2
                }
            }.thenBy { (element, _) -> element.symbol },
        )
        return ordered.joinToString("") { (element, count) ->
            if (count == 1) element.symbol else "${element.symbol}$count"
        }
    }
}

private const val MAX_MULTIPLIER = 12
private const val RATIO_TOLERANCE = 0.08

