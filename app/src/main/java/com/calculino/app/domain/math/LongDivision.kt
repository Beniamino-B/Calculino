package com.calculino.app.domain.math

import java.math.BigInteger

data class QuotientDigit(
    val position: Int,
    val digit: Int,
)

data class LongDivisionStep(
    val position: Int,
    val currentValue: BigInteger,
    val quotientDigit: Int,
    val product: BigInteger,
    val remainder: BigInteger,
    val nextDigit: Int?,
    val nextPartial: BigInteger?,
)

data class LongDivisionProcedure(
    val dividend: BigInteger,
    val divisor: BigInteger,
    val quotient: BigInteger,
    val remainder: BigInteger,
    val dividendDigits: List<Int>,
    val quotientDigits: List<QuotientDigit>,
    val steps: List<LongDivisionStep>,
) {
    val isExact: Boolean get() = remainder.signum() == 0
}

object LongDivisionProcedureBuilder {
    private const val MAX_DIVIDEND_DIGITS = 12
    private const val MAX_DIVISOR_DIGITS = 6
    private val TEN = BigInteger.TEN

    fun build(
        expression: String,
        angleMode: AngleMode = AngleMode.Radians,
    ): LongDivisionProcedure? = runCatching {
        val parsed = parseExpression(expression)
        if (parsed !is Expression.Binary || parsed.operator != BinaryOperator.Divide) return null
        val dividend = evaluateInteger(parsed.left, angleMode) ?: return null
        val divisor = evaluateInteger(parsed.right, angleMode) ?: return null
        if (dividend.signum() < 0 || divisor.signum() <= 0) return null
        if (dividend.toString().length > MAX_DIVIDEND_DIGITS) return null
        if (divisor.toString().length > MAX_DIVISOR_DIGITS) return null
        buildDivision(dividend, divisor)
    }.getOrNull()

    private fun evaluateInteger(
        expression: Expression,
        angleMode: AngleMode,
    ): BigInteger? {
        val value = Evaluator(angleMode).evaluate(expression) as? NumericValue.Exact ?: return null
        if (!value.rational.isInteger()) return null
        return value.rational.numerator
    }

    private fun buildDivision(
        dividend: BigInteger,
        divisor: BigInteger,
    ): LongDivisionProcedure {
        val division = dividend.divideAndRemainder(divisor)
        val quotient = division[0]
        val finalRemainder = division[1]
        val digits = dividend.toString().map { it.digitToInt() }
        val quotientDigits = mutableListOf<QuotientDigit>()
        val steps = mutableListOf<LongDivisionStep>()
        var partial = BigInteger.ZERO
        var quotientStarted = false

        digits.forEachIndexed { position, digit ->
            partial = partial.multiply(TEN).add(BigInteger.valueOf(digit.toLong()))
            val stepQuotient = partial.divide(divisor)
            val stepProduct = stepQuotient.multiply(divisor)
            val stepRemainder = partial.subtract(stepProduct)
            val quotientDigit = stepQuotient.toInt()
            if (quotientDigit > 0) quotientStarted = true
            val nextDigit = digits.getOrNull(position + 1)
            val nextPartial = nextDigit?.let {
                stepRemainder.multiply(TEN).add(BigInteger.valueOf(it.toLong()))
            }
            if (quotientStarted) {
                quotientDigits += QuotientDigit(position = position, digit = quotientDigit)
                steps += LongDivisionStep(
                    position = position,
                    currentValue = partial,
                    quotientDigit = quotientDigit,
                    product = stepProduct,
                    remainder = stepRemainder,
                    nextDigit = nextDigit,
                    nextPartial = nextPartial,
                )
            }
            partial = stepRemainder
        }

        if (quotientDigits.isEmpty()) {
            quotientDigits += QuotientDigit(position = digits.lastIndex, digit = 0)
            steps += LongDivisionStep(
                position = digits.lastIndex,
                currentValue = dividend,
                quotientDigit = 0,
                product = BigInteger.ZERO,
                remainder = dividend,
                nextDigit = null,
                nextPartial = null,
            )
        }

        return LongDivisionProcedure(
            dividend = dividend,
            divisor = divisor,
            quotient = quotient,
            remainder = finalRemainder,
            dividendDigits = digits,
            quotientDigits = quotientDigits,
            steps = steps,
        )
    }
}
