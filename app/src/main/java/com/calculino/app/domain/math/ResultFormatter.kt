package com.calculino.app.domain.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

private const val SIGNIFICANT_DIGITS = 12
private const val SCIENTIFIC_UPPER_BOUND = 1.0e12
private const val SCIENTIFIC_LOWER_BOUND = 1.0e-6

internal object ResultFormatter {

    fun format(value: NumericValue): FormattedResult = when (value) {
        is NumericValue.Exact -> formatExact(value.rational)
        is NumericValue.Approximate -> FormattedResult(
            main = unicodeMinus(formatApproximate(value.value)),
            approximation = null,
            isExact = false,
        )
    }

    private fun formatExact(value: Rational): FormattedResult {
        if (value.isInteger()) {
            return FormattedResult(
                main = unicodeMinus(value.numerator.toString()),
                approximation = null,
                isExact = true,
            )
        }
        val terminatingDecimal = value.toBigDecimalExact()
        if (terminatingDecimal != null) {
            return FormattedResult(
                main = unicodeMinus(terminatingDecimal.toPlainString()),
                approximation = null,
                isExact = true,
            )
        }
        val doubleValue = value.toDouble()
        val approximation = if (doubleValue.isFinite()) {
            unicodeMinus(formatApproximate(doubleValue))
        } else {
            null
        }
        return FormattedResult(
            main = unicodeMinus("${value.numerator}/${value.denominator}"),
            approximation = approximation,
            isExact = true,
        )
    }

    private fun formatApproximate(value: Double): String {
        if (!value.isFinite()) throw MathException(MathError.Overflow)
        if (value == 0.0) return "0"
        val magnitude = abs(value)
        val rounded = BigDecimal.valueOf(value).round(MathContext(SIGNIFICANT_DIGITS, RoundingMode.HALF_UP))
        return if (magnitude >= SCIENTIFIC_UPPER_BOUND || magnitude < SCIENTIFIC_LOWER_BOUND) {
            formatScientific(rounded)
        } else {
            rounded.stripTrailingZeros().toPlainString()
        }
    }

    private fun formatScientific(value: BigDecimal): String {
        if (value.signum() == 0) return "0"
        val normalized = value.stripTrailingZeros()
        val exponent = normalized.precision() - normalized.scale() - 1
        val mantissa = normalized.abs().movePointLeft(exponent).stripTrailingZeros().toPlainString()
        val sign = if (normalized.signum() < 0) "−" else ""
        return "$sign$mantissa×10^$exponent"
    }

    private fun unicodeMinus(text: String): String = text.replace('-', '−')
}

private const val MAX_EXPRESSION_LENGTH = 512

class MathEngine {

    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.Radians,
    ): MathEvaluationResult {
        if (expression.length > MAX_EXPRESSION_LENGTH) {
            return MathEvaluationResult.Failure(MathError.ResultTooLarge)
        }
        return try {
            val parsed = parseExpression(expression)
            val value = Evaluator(angleMode).evaluate(parsed)
            MathEvaluationResult.Success(value = value, display = ResultFormatter.format(value))
        } catch (exception: MathException) {
            MathEvaluationResult.Failure(exception.error)
        } catch (_: ArithmeticException) {
            MathEvaluationResult.Failure(MathError.Overflow)
        }
    }
}
