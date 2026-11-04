package com.calculino.app.domain.chemistry

import com.calculino.app.domain.periodic.ChemicalElement
import com.calculino.app.domain.periodic.PeriodicTable
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class ElementContribution(
    val element: ChemicalElement,
    val atomCount: Int,
    val mass: BigDecimal,
    val percentage: BigDecimal,
) {
    val massDisplay: String get() = mass.toDisplayString(4)
    val percentageDisplay: String get() = percentage.toDisplayString(2)
}

data class MolarMassResult(
    val formula: String,
    val molarMass: BigDecimal,
    val totalAtoms: Int,
    val composition: List<ElementContribution>,
) {
    val molarMassDisplay: String get() = molarMass.toDisplayString(4)
}

sealed interface MolarMassError {
    data object EmptyFormula : MolarMassError
    data class UnknownElement(val symbol: String, val position: Int) : MolarMassError
    data class UnexpectedCharacter(val character: String, val position: Int) : MolarMassError
    data class UnbalancedParenthesis(val position: Int) : MolarMassError
    data class EmptyGroup(val position: Int) : MolarMassError
    data class InvalidCount(val position: Int) : MolarMassError
    data object TooLarge : MolarMassError
}

sealed interface MolarMassOutcome {
    data class Success(val result: MolarMassResult) : MolarMassOutcome
    data class Failure(val error: MolarMassError) : MolarMassOutcome
}

object MolarMassCalculator {
    fun calculate(formula: String): MolarMassOutcome {
        return try {
            val counts = FormulaParser(formula).parse()
            if (counts.isEmpty()) return MolarMassOutcome.Failure(MolarMassError.EmptyFormula)
            val contributions = counts.entries
                .sortedBy { it.key.number }
                .map { (element, count) ->
                    val mass = BigDecimal.valueOf(element.atomicMass)
                        .multiply(BigDecimal.valueOf(count.toLong()))
                    ElementContribution(
                        element = element,
                        atomCount = count,
                        mass = mass,
                        percentage = BigDecimal.ZERO,
                    )
                }
            val totalMass = contributions.fold(BigDecimal.ZERO) { total, contribution ->
                total.add(contribution.mass)
            }
            val totalAtoms = counts.values.sum()
            val withPercentages = contributions.map { contribution ->
                val percentage = contribution.mass
                    .divide(totalMass, MathContext(10, RoundingMode.HALF_UP))
                    .multiply(BigDecimal(100))
                contribution.copy(percentage = percentage)
            }
            MolarMassOutcome.Success(
                MolarMassResult(
                    formula = formula.filterNot { it.isWhitespace() },
                    molarMass = totalMass,
                    totalAtoms = totalAtoms,
                    composition = withPercentages,
                ),
            )
        } catch (exception: FormulaException) {
            MolarMassOutcome.Failure(exception.error)
        }
    }
}

private class FormulaException(val error: MolarMassError) : RuntimeException()

private class FormulaParser(source: String) {
    private val input = source.filterNot { it.isWhitespace() }
    private var index = 0

    fun parse(): Map<ChemicalElement, Int> {
        if (input.isEmpty()) throw FormulaException(MolarMassError.EmptyFormula)
        val result = mutableMapOf<ChemicalElement, Int>()
        while (true) {
            val multiplier = readNumber() ?: 1
            val part = parseSequence(null)
            if (part.isEmpty()) throw FormulaException(MolarMassError.EmptyFormula)
            addScaled(result, part, multiplier)
            if (index >= input.length) break
            val character = input[index]
            if (character in HYDRATE_SEPARATORS) {
                index++
                if (index >= input.length) {
                    throw FormulaException(MolarMassError.UnexpectedCharacter(character.toString(), index - 1))
                }
            } else {
                throw FormulaException(MolarMassError.UnexpectedCharacter(character.toString(), index))
            }
        }
        return result
    }

    private fun parseSequence(expectedClosing: Char?): Map<ChemicalElement, Int> {
        val result = mutableMapOf<ChemicalElement, Int>()
        while (index < input.length) {
            val character = input[index]
            when {
                character == '(' || character == '[' -> {
                    val openingPosition = index
                    val closing = if (character == '(') ')' else ']'
                    index++
                    val inner = parseSequence(closing)
                    if (inner.isEmpty()) throw FormulaException(MolarMassError.EmptyGroup(openingPosition))
                    if (index >= input.length) throw FormulaException(MolarMassError.UnbalancedParenthesis(openingPosition))
                    if (input[index] != closing) {
                        throw FormulaException(MolarMassError.UnbalancedParenthesis(openingPosition))
                    }
                    index++
                    val countPosition = index
                    val count = readNumber() ?: 1
                    if (count <= 0) throw FormulaException(MolarMassError.InvalidCount(countPosition))
                    addScaled(result, inner, count)
                }
                character.isUpperCase() -> {
                    val start = index
                    index++
                    if (index < input.length && input[index].isLowerCase()) index++
                    val symbol = input.substring(start, index)
                    val element = PeriodicTable.bySymbol[symbol]
                        ?: throw FormulaException(MolarMassError.UnknownElement(symbol, start))
                    val countPosition = index
                    val count = readNumber() ?: 1
                    if (count <= 0) throw FormulaException(MolarMassError.InvalidCount(countPosition))
                    addScaled(result, mapOf(element to 1), count)
                }
                expectedClosing != null && character == expectedClosing -> return result
                expectedClosing == null && character in HYDRATE_SEPARATORS -> return result
                else -> throw FormulaException(MolarMassError.UnexpectedCharacter(character.toString(), index))
            }
        }
        if (expectedClosing != null) throw FormulaException(MolarMassError.UnbalancedParenthesis(index))
        return result
    }

    private fun readNumber(): Int? {
        if (index >= input.length || !input[index].isDigit()) return null
        val start = index
        while (index < input.length && input[index].isDigit()) index++
        val text = input.substring(start, index)
        val value = text.toIntOrNull() ?: throw FormulaException(MolarMassError.InvalidCount(start))
        if (value <= 0 && text.isNotEmpty()) throw FormulaException(MolarMassError.InvalidCount(start))
        return value
    }

    private fun addScaled(
        target: MutableMap<ChemicalElement, Int>,
        source: Map<ChemicalElement, Int>,
        multiplier: Int,
    ) {
        source.forEach { (element, count) ->
            val updated = count.toLong() * multiplier.toLong()
            if (updated > MAX_ATOMS) throw FormulaException(MolarMassError.TooLarge)
            val newCount = (target[element] ?: 0).toLong() + updated
            if (newCount > MAX_ATOMS) throw FormulaException(MolarMassError.TooLarge)
            target[element] = newCount.toInt()
        }
    }

    private companion object {
        const val MAX_ATOMS = 10_000
        val HYDRATE_SEPARATORS = setOf('·', '•', '*', '.')
    }
}

private fun BigDecimal.toDisplayString(scale: Int): String =
    setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
