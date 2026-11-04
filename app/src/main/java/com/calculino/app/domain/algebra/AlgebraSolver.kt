package com.calculino.app.domain.algebra

import com.calculino.app.domain.math.MathStep
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

data class AlgebraSolution(
    val headline: String,
    val steps: List<MathStep>,
)

sealed interface AlgebraResult {
    data class Success(val solution: AlgebraSolution) : AlgebraResult
    data class Failure(val error: AlgebraError) : AlgebraResult
}

enum class AlgebraError {
    InvalidInput,
    Unsupported,
    NoRealSolution,
    NoUniqueSolution,
    InfiniteSolutions,
}

object AlgebraSolver {
    fun solve(input: String): AlgebraResult {
        val equations = input.lines()
            .flatMap { line -> line.split(';') }
            .map(String::trim)
            .filter(String::isNotEmpty)
        if (equations.isEmpty()) return AlgebraResult.Failure(AlgebraError.InvalidInput)
        return try {
            val parsed = equations.map(::parseEquation)
            when (parsed.size) {
                1 -> solveSingle(parsed.first())
                2 -> solveSystem(parsed)
                else -> AlgebraResult.Failure(AlgebraError.Unsupported)
            }
        } catch (_: Exception) {
            AlgebraResult.Failure(AlgebraError.InvalidInput)
        }
    }

    private fun solveSingle(equation: ParsedEquation): AlgebraResult {
        val polynomial = equation.polynomial
        val variables = polynomial.variables
        if (variables.isEmpty()) {
            return AlgebraResult.Failure(
                if (polynomial.isZero) {
                    AlgebraError.InfiniteSolutions
                } else {
                    AlgebraError.NoUniqueSolution
                },
            )
        }
        if (variables.size > 1 || !polynomial.degreeAtMost(2)) {
            return AlgebraResult.Failure(AlgebraError.Unsupported)
        }
        val variable = variables.first()
        return when (polynomial.degree) {
            0 -> AlgebraResult.Failure(
                if (polynomial.isZero) {
                    AlgebraError.InfiniteSolutions
                } else {
                    AlgebraError.NoUniqueSolution
                },
            )
            1 -> solveLinear(equation, polynomial, variable)
            2 -> solveQuadratic(equation, polynomial, variable)
            else -> AlgebraResult.Failure(AlgebraError.Unsupported)
        }
    }

    private fun solveLinear(
        equation: ParsedEquation,
        polynomial: Polynomial,
        variable: String,
    ): AlgebraResult {
        val a = polynomial.coefficient(variable)
        val b = polynomial.constant()
        if (abs(a) < EPSILON) {
            return AlgebraResult.Failure(
                if (abs(b) < EPSILON) {
                    AlgebraError.InfiniteSolutions
                } else {
                    AlgebraError.NoUniqueSolution
                },
            )
        }
        val normalized = formatEquation(polynomial)
        val isolated = "${formatVariableTerm(a, variable)} = ${formatNumber(-b)}"
        val root = -b / a
        val solution = "$variable = ${formatNumber(root)}"
        val steps = buildList {
            if (equation.text != normalized) add(MathStep(equation.text, normalized, null))
            if (abs(b) > EPSILON) add(MathStep(normalized, isolated, null))
            add(MathStep(isolated, solution, null))
        }
        return AlgebraResult.Success(AlgebraSolution(solution, steps))
    }

    private fun solveQuadratic(
        equation: ParsedEquation,
        polynomial: Polynomial,
        variable: String,
    ): AlgebraResult {
        val a = polynomial.coefficient(variable, 2)
        val b = polynomial.coefficient(variable)
        val c = polynomial.constant()
        val normalized = formatEquation(polynomial)
        val coefficients = "a = ${formatNumber(a)}, b = ${formatNumber(b)}, c = ${formatNumber(c)}"
        val discriminant = b * b - 4 * a * c
        val steps = buildList {
            if (equation.text != normalized) add(MathStep(equation.text, normalized, null))
            add(MathStep(normalized, coefficients, null))
            add(
                MathStep(
                    coefficients,
                    "Δ = b² − 4ac = ${formatFactor(b)}² − 4·${formatFactor(a)}·${formatFactor(c)} = " +
                        formatNumber(discriminant),
                    null,
                ),
            )
        }
        if (discriminant < -EPSILON) {
            return AlgebraResult.Failure(AlgebraError.NoRealSolution)
        }
        val root = sqrt(max(0.0, discriminant))
        if (abs(discriminant) < EPSILON) {
            val value = -b / (2 * a)
            val answer = "$variable = ${formatNumber(value)}"
            val allSteps = steps + MathStep("Δ = 0", answer, null)
            return AlgebraResult.Success(AlgebraSolution(answer, allSteps))
        }
        val first = (-b + root) / (2 * a)
        val second = (-b - root) / (2 * a)
        val roots = listOf(first, second).sorted()
        val answer =
            "$variable₁ = ${formatNumber(roots[0])}, $variable₂ = ${formatNumber(roots[1])}"
        val allSteps = steps + listOf(
            MathStep(
                "Δ = ${formatNumber(discriminant)}",
                "√Δ = ${formatNumber(root)}",
                null,
            ),
            MathStep(
                "√Δ = ${formatNumber(root)}",
                "$variable = (${formatNumber(-b)} ± ${formatNumber(root)}) / ${formatFactor(2 * a)}",
                null,
            ),
            MathStep(
                "$variable = (${formatNumber(-b)} ± ${formatNumber(root)}) / ${formatFactor(2 * a)}",
                answer,
                null,
            ),
        )
        return AlgebraResult.Success(AlgebraSolution(answer, allSteps))
    }

    private fun solveSystem(equations: List<ParsedEquation>): AlgebraResult {
        val allVariables = equations
            .flatMap { equation -> equation.polynomial.variables }
            .distinct()
            .sorted()
        if (allVariables.size > 2 || equations.any { !it.polynomial.degreeAtMost(1) }) {
            return AlgebraResult.Failure(AlgebraError.Unsupported)
        }
        val variables = allVariables.toMutableList()
        while (variables.size < 2) {
            variables += if ("x" !in variables) "x" else "y"
        }
        val firstVariable = variables[0]
        val secondVariable = variables[1]
        val lines = equations.map { equation ->
            val polynomial = equation.polynomial
            LinearEquation(
                text = equation.text,
                firstCoefficient = polynomial.coefficient(firstVariable),
                secondCoefficient = polynomial.coefficient(secondVariable),
                constant = -polynomial.constant(),
            )
        }
        val first = lines[0]
        val second = lines[1]
        val determinant = first.firstCoefficient * second.secondCoefficient -
            second.firstCoefficient * first.secondCoefficient
        val steps = mutableListOf(
            MathStep("Ecuación 1", first.text, null),
            MathStep("Ecuación 2", second.text, null),
            MathStep(
                "Determinante",
                "D = ${formatFactor(first.firstCoefficient)}·${formatFactor(second.secondCoefficient)} − " +
                    "${formatFactor(second.firstCoefficient)}·${formatFactor(first.secondCoefficient)} = " +
                    formatNumber(determinant),
                null,
            ),
        )
        if (abs(determinant) < EPSILON) {
            return AlgebraResult.Failure(AlgebraError.NoUniqueSolution)
        }
        val xDeterminant = first.constant * second.secondCoefficient -
            second.constant * first.secondCoefficient
        val yDeterminant = first.firstCoefficient * second.constant -
            second.firstCoefficient * first.constant
        val x = xDeterminant / determinant
        val y = yDeterminant / determinant
        val answer =
            "$firstVariable = ${formatNumber(x)}, $secondVariable = ${formatNumber(y)}"
        steps += MathStep(
            "D = ${formatNumber(determinant)}",
            "D$firstVariable = ${formatNumber(xDeterminant)}, D$secondVariable = ${formatNumber(yDeterminant)}",
            null,
        )
        steps += MathStep(
            "D$firstVariable / D, D$secondVariable / D",
            answer,
            null,
        )
        return AlgebraResult.Success(AlgebraSolution(answer, steps))
    }

    private fun parseEquation(text: String): ParsedEquation {
        val parts = text.split('=')
        if (parts.size != 2) throw AlgebraParseException()
        val left = AlgebraExpressionParser(parts[0]).parse()
        val right = AlgebraExpressionParser(parts[1]).parse()
        return ParsedEquation(text.trim().replace('-', '−'), left - right)
    }
}

private data class ParsedEquation(
    val text: String,
    val polynomial: Polynomial,
)

private data class LinearEquation(
    val text: String,
    val firstCoefficient: Double,
    val secondCoefficient: Double,
    val constant: Double,
)

private data class Monomial(
    val powers: Map<String, Int>,
) {
    val degree: Int = powers.values.sum()

    fun multiply(other: Monomial): Monomial {
        val result = powers.toMutableMap()
        other.powers.forEach { (variable, power) ->
            result[variable] = (result[variable] ?: 0) + power
        }
        return Monomial(result.filterValues { power -> power > 0 })
    }
}

private class Polynomial private constructor(
    val terms: Map<Monomial, Double>,
) {
    val variables: Set<String>
        get() = terms.keys
            .flatMap { monomial -> monomial.powers.keys }
            .toSet()

    val degree: Int
        get() = terms.keys.maxOfOrNull(Monomial::degree) ?: 0

    val isZero: Boolean
        get() = terms.isEmpty()

    fun degreeAtMost(value: Int): Boolean =
        terms.keys.all { monomial -> monomial.degree <= value }

    fun constant(): Double = terms[Monomial(emptyMap())] ?: 0.0

    fun coefficient(variable: String, power: Int = 1): Double {
        var result = 0.0
        terms.forEach { (monomial, coefficient) ->
            if (monomial.powers == mapOf(variable to power)) result += coefficient
        }
        return result
    }

    fun constantOrNull(): Double? {
        if (terms.keys.any { monomial -> monomial.degree > 0 }) return null
        return constant()
    }

    operator fun plus(other: Polynomial): Polynomial {
        val merged = terms.toMutableMap()
        other.terms.forEach { (monomial, coefficient) ->
            merged[monomial] = (merged[monomial] ?: 0.0) + coefficient
        }
        return of(merged)
    }

    operator fun minus(other: Polynomial): Polynomial = this + (other * -1.0)

    operator fun times(other: Polynomial): Polynomial {
        val result = mutableMapOf<Monomial, Double>()
        terms.forEach { (leftMonomial, leftCoefficient) ->
            other.terms.forEach { (rightMonomial, rightCoefficient) ->
                val monomial = leftMonomial.multiply(rightMonomial)
                result[monomial] = (result[monomial] ?: 0.0) +
                    leftCoefficient * rightCoefficient
            }
        }
        return of(result)
    }

    operator fun times(value: Double): Polynomial =
        of(terms.mapValues { (_, coefficient) -> coefficient * value })

    fun power(exponent: Int): Polynomial {
        var result = constant(1.0)
        repeat(exponent) {
            result *= this
        }
        return result
    }

    fun divide(value: Double): Polynomial =
        of(terms.mapValues { (_, coefficient) -> coefficient / value })

    companion object {
        val ZERO: Polynomial = Polynomial(emptyMap())

        fun of(terms: Map<Monomial, Double>): Polynomial =
            Polynomial(
                terms.filterValues { coefficient -> abs(coefficient) > EPSILON },
            )

        fun constant(value: Double): Polynomial =
            of(mapOf(Monomial(emptyMap()) to value))

        fun variable(name: String): Polynomial =
            of(mapOf(Monomial(mapOf(name to 1)) to 1.0))
    }
}

private class AlgebraExpressionParser(
    input: String,
) {
    private val tokens = tokenize(input)
    private var index = 0

    fun parse(): Polynomial {
        val result = parseAdditive()
        if (current() !is AlgebraToken.End) throw AlgebraParseException()
        return result
    }

    private fun parseAdditive(): Polynomial {
        var result = parseMultiplicative()
        while (true) {
            result = when {
                consumeSymbol('+') -> result + parseMultiplicative()
                consumeSymbol('-') -> result - parseMultiplicative()
                else -> return result
            }
        }
    }

    private fun parseMultiplicative(): Polynomial {
        var result = parseUnary()
        while (true) {
            result = when {
                consumeSymbol('*') -> result * parseUnary()
                consumeSymbol('/') -> {
                    val divisor = parseUnary().constantOrNull() ?: throw AlgebraParseException()
                    if (abs(divisor) < EPSILON) throw AlgebraParseException()
                    result.divide(divisor)
                }
                startsPrimary(current()) -> result * parseUnary()
                else -> return result
            }
        }
    }

    private fun parseUnary(): Polynomial = when {
        consumeSymbol('+') -> parseUnary()
        consumeSymbol('-') -> Polynomial.ZERO - parseUnary()
        else -> parsePower()
    }

    private fun parsePower(): Polynomial {
        val base = parsePrimary()
        if (!consumeSymbol('^')) return base
        val exponent = parseUnary().constantOrNull() ?: throw AlgebraParseException()
        val integerExponent = exponent.toInt()
        if (abs(exponent - integerExponent) > EPSILON ||
            integerExponent < 0 ||
            integerExponent > MAX_POWER
        ) {
            throw AlgebraParseException()
        }
        return base.power(integerExponent)
    }

    private fun parsePrimary(): Polynomial = when (val token = current()) {
        is AlgebraToken.Number -> {
            advance()
            Polynomial.constant(token.value)
        }
        is AlgebraToken.Variable -> {
            advance()
            Polynomial.variable(token.name)
        }
        is AlgebraToken.Symbol -> {
            if (token.symbol != '(') throw AlgebraParseException()
            advance()
            val result = parseAdditive()
            if (!consumeSymbol(')')) throw AlgebraParseException()
            result
        }
        AlgebraToken.End -> throw AlgebraParseException()
    }

    private fun current(): AlgebraToken = tokens[index]

    private fun advance() {
        if (index < tokens.lastIndex) index++
    }

    private fun consumeSymbol(symbol: Char): Boolean {
        val token = current()
        if (token !is AlgebraToken.Symbol || token.symbol != symbol) return false
        advance()
        return true
    }

    private fun startsPrimary(token: AlgebraToken): Boolean = when (token) {
        is AlgebraToken.Number, is AlgebraToken.Variable -> true
        is AlgebraToken.Symbol -> token.symbol == '('
        AlgebraToken.End -> false
    }
}

private sealed interface AlgebraToken {
    data class Number(val text: String, val value: Double) : AlgebraToken
    data class Variable(val name: String) : AlgebraToken
    data class Symbol(val symbol: Char) : AlgebraToken
    data object End : AlgebraToken
}

private fun tokenize(input: String): List<AlgebraToken> {
    val tokens = mutableListOf<AlgebraToken>()
    var index = 0
    while (index < input.length) {
        val character = input[index]
        when {
            character.isWhitespace() -> index++
            character.isDigit() || character == '.' -> {
                val start = index
                while (index < input.length &&
                    (input[index].isDigit() || input[index] == '.')
                ) {
                    index++
                }
                val text = input.substring(start, index)
                val value = text.toDoubleOrNull() ?: throw AlgebraParseException()
                tokens += AlgebraToken.Number(text, value)
            }
            character.isLetter() -> {
                tokens += AlgebraToken.Variable(character.lowercaseChar().toString())
                index++
            }
            character in "+-*/^()" -> {
                tokens += AlgebraToken.Symbol(character)
                index++
            }
            character == '−' -> {
                tokens += AlgebraToken.Symbol('-')
                index++
            }
            character == '×' -> {
                tokens += AlgebraToken.Symbol('*')
                index++
            }
            character == '÷' -> {
                tokens += AlgebraToken.Symbol('/')
                index++
            }
            else -> throw AlgebraParseException()
        }
    }
    tokens += AlgebraToken.End
    return tokens
}

private fun formatEquation(polynomial: Polynomial): String =
    "${formatPolynomial(polynomial)} = 0"

private fun formatPolynomial(polynomial: Polynomial): String {
    if (polynomial.isZero) return "0"
    val sorted = polynomial.terms.entries.sortedWith(
        compareByDescending<Map.Entry<Monomial, Double>> { entry -> entry.key.degree }
            .thenBy { entry -> entry.key.powers.keys.sorted().joinToString("") },
    )
    val builder = StringBuilder()
    sorted.forEachIndexed { index, entry ->
        val value = entry.value
        val negative = value < 0
        val absolute = abs(value)
        val variablePart = formatMonomial(entry.key)
        val coefficientPart = if (variablePart.isNotEmpty() && abs(absolute - 1) < EPSILON) {
            ""
        } else {
            formatNumber(absolute)
        }
        val term = coefficientPart + variablePart
        when {
            index == 0 -> builder.append(if (negative) "−$term" else term)
            negative -> builder.append(" − ").append(term)
            else -> builder.append(" + ").append(term)
        }
    }
    return builder.toString()
}

private fun formatMonomial(monomial: Monomial): String =
    monomial.powers.entries
        .sortedBy { entry -> entry.key }
        .joinToString("") { (variable, power) ->
            if (power == 1) variable else "$variable^$power"
        }

private fun formatVariableTerm(coefficient: Double, variable: String): String = when {
    abs(coefficient - 1) < EPSILON -> variable
    abs(coefficient + 1) < EPSILON -> "−$variable"
    else -> "${formatNumber(coefficient)}$variable"
}

private fun formatFactor(value: Double): String =
    if (value < 0) "(${formatNumber(value)})" else formatNumber(value)

private fun formatNumber(value: Double): String {
    if (abs(value) < EPSILON) return "0"
    return BigDecimal.valueOf(value)
        .setScale(6, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
        .replace('-', '−')
}

private class AlgebraParseException : RuntimeException()

private const val EPSILON = 1e-9
private const val MAX_POWER = 4
