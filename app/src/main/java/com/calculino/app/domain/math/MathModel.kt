package com.calculino.app.domain.math

enum class AngleMode {
    Radians,
    Degrees,
}

sealed interface NumericValue {
    val isExact: Boolean

    data class Exact(val rational: Rational) : NumericValue {
        override val isExact: Boolean = true
    }

    data class Approximate(val value: Double) : NumericValue {
        override val isExact: Boolean = false
    }
}

sealed interface MathError {
    data object EmptyExpression : MathError
    data class UnexpectedToken(val token: String, val position: Int) : MathError
    data class UnexpectedCharacter(val character: String, val position: Int) : MathError
    data class MalformedNumber(val text: String, val position: Int) : MathError
    data class UnknownIdentifier(val name: String, val position: Int) : MathError
    data object DivisionByZero : MathError
    data class DomainError(val operation: String) : MathError
    data object Overflow : MathError
    data object ResultTooLarge : MathError
    data class InvalidFactorial(val value: String) : MathError
}

data class FormattedResult(
    val main: String,
    val approximation: String?,
    val isExact: Boolean,
)

sealed interface MathEvaluationResult {
    data class Success(
        val value: NumericValue,
        val display: FormattedResult,
    ) : MathEvaluationResult

    data class Failure(val error: MathError) : MathEvaluationResult
}

class MathException(val error: MathError) : RuntimeException()
