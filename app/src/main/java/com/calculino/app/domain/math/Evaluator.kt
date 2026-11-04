package com.calculino.app.domain.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private const val MAX_EXACT_BITS = 8192
private const val MAX_EXPONENT = 10_000
private const val MAX_FACTORIAL = 1_000
private val HUNDRED = Rational.of(100)

internal class Evaluator(private val angleMode: AngleMode) {

    fun evaluate(expression: Expression): NumericValue = when (expression) {
        is Expression.NumberLiteral -> expression.value
        is Expression.Unary -> evaluateUnary(expression)
        is Expression.Binary -> evaluateBinary(expression)
        is Expression.FunctionCall -> evaluateFunction(expression.name, evaluate(expression.argument))
        is Expression.Postfix -> evaluatePostfix(expression)
    }

    private fun evaluateUnary(expression: Expression.Unary): NumericValue {
        val operand = evaluate(expression.operand)
        return when (expression.operator) {
            UnaryOperator.Plus -> operand
            UnaryOperator.Minus -> negate(operand)
        }
    }

    private fun evaluateBinary(expression: Expression.Binary): NumericValue {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)
        return when (expression.operator) {
            BinaryOperator.Add -> add(left, right)
            BinaryOperator.Subtract -> subtract(left, right)
            BinaryOperator.Multiply -> multiply(left, right)
            BinaryOperator.Divide -> divide(left, right)
            BinaryOperator.Power -> power(left, right)
        }
    }

    private fun evaluatePostfix(expression: Expression.Postfix): NumericValue {
        val operand = evaluate(expression.operand)
        return when (expression.operator) {
            PostfixOperator.Percent -> divide(operand, NumericValue.Exact(HUNDRED))
            PostfixOperator.Factorial -> factorial(operand)
        }
    }

    private fun negate(value: NumericValue): NumericValue = when (value) {
        is NumericValue.Exact -> exact(Rational.of(value.rational.numerator.negate(), value.rational.denominator))
        is NumericValue.Approximate -> approximate(-value.value)
    }

    private fun add(left: NumericValue, right: NumericValue): NumericValue =
        combine(left, right, Rational::plus, Double::plus)

    private fun subtract(left: NumericValue, right: NumericValue): NumericValue =
        combine(left, right, Rational::minus, Double::minus)

    private fun multiply(left: NumericValue, right: NumericValue): NumericValue =
        combine(left, right, Rational::times, Double::times)

    private fun combine(
        left: NumericValue,
        right: NumericValue,
        exactOperation: (Rational, Rational) -> Rational,
        approximateOperation: (Double, Double) -> Double,
    ): NumericValue {
        return if (left is NumericValue.Exact && right is NumericValue.Exact) {
            exact(exactOperation(left.rational, right.rational))
        } else {
            approximate(approximateOperation(left.toDoubleValue(), right.toDoubleValue()))
        }
    }

    private fun divide(left: NumericValue, right: NumericValue): NumericValue {
        if (right.isZeroValue()) throw MathException(MathError.DivisionByZero)
        return if (left is NumericValue.Exact && right is NumericValue.Exact) {
            exact(left.rational / right.rational)
        } else {
            approximate(left.toDoubleValue() / right.toDoubleValue())
        }
    }

    private fun power(base: NumericValue, exponent: NumericValue): NumericValue {
        val exponentRational = (exponent as? NumericValue.Exact)?.rational
        if (exponentRational != null && exponentRational.isInteger()) {
            val exponentValue = exponentRational.numerator
            if (exponentValue.bitLength() > 31) throw MathException(MathError.ResultTooLarge)
            val exponentInt = exponentValue.toInt()
            if (exponentInt > MAX_EXPONENT || exponentInt < -MAX_EXPONENT) {
                throw MathException(MathError.ResultTooLarge)
            }
            return if (base is NumericValue.Exact) {
                if (base.rational.isZero() && exponentInt < 0) {
                    throw MathException(MathError.DivisionByZero)
                }
                ensurePowerSize(base.rational, exponentInt)
                exact(base.rational.pow(exponentInt))
            } else {
                val approximateBase = base as NumericValue.Approximate
                if (approximateBase.value == 0.0 && exponentInt < 0) {
                    throw MathException(MathError.DivisionByZero)
                }
                approximate(approximateBase.value.pow(exponentInt.toDouble()))
            }
        }
        val baseValue = base.toDoubleValue()
        if (baseValue < 0.0) throw MathException(MathError.DomainError("^"))
        return approximate(baseValue.pow(exponent.toDoubleValue()))
    }

    private fun ensurePowerSize(base: Rational, exponent: Int) {
        if (base.numerator.signum() == 0) return
        if (base.numerator.abs() == base.denominator) return
        val bits = maxOf(base.numerator.abs().bitLength(), base.denominator.bitLength()).toLong()
        if (bits * abs(exponent).toLong() > MAX_EXACT_BITS) {
            throw MathException(MathError.ResultTooLarge)
        }
    }

    private fun factorial(value: NumericValue): NumericValue {
        val rational = (value as? NumericValue.Exact)?.rational
            ?: throw MathException(MathError.InvalidFactorial(value.displayValue()))
        if (!rational.isInteger() || rational.numerator.signum() < 0) {
            throw MathException(MathError.InvalidFactorial(rational.toString()))
        }
        if (rational.numerator > BigInteger.valueOf(MAX_FACTORIAL.toLong())) {
            throw MathException(MathError.ResultTooLarge)
        }
        val count = rational.numerator.toInt()
        var result = BigInteger.ONE
        for (number in 2..count) {
            result = result.multiply(BigInteger.valueOf(number.toLong()))
        }
        return exact(Rational.of(result))
    }

    private fun evaluateFunction(name: String, argument: NumericValue): NumericValue {
        return when (name) {
            "sin" -> trig(argument, NumericValue.Exact(Rational.ZERO)) { sin(it) }
            "cos" -> trig(argument, NumericValue.Exact(Rational.ONE)) { cos(it) }
            "tan" -> tangent(argument)
            "asin" -> inverseTrig(argument, Math::asin)
            "acos" -> inverseTrig(argument, Math::acos)
            "atan" -> inverseTrig(argument, Math::atan)
            "sinh" -> approximate(Math.sinh(argument.toDoubleValue()))
            "cosh" -> approximate(Math.cosh(argument.toDoubleValue()))
            "tanh" -> approximate(Math.tanh(argument.toDoubleValue()))
            "ln" -> logarithm(argument) { Math.log(it) }
            "log", "log10" -> logarithm(argument) { Math.log10(it) }
            "log2" -> logarithm(argument) { Math.log(it) / Math.log(2.0) }
            "exp" -> approximate(Math.exp(argument.toDoubleValue()))
            "sqrt" -> squareRoot(argument)
            "cbrt" -> cubeRoot(argument)
            "abs" -> absolute(argument)
            "floor" -> integerTransform(argument) { floorExact(it) }
            "ceil" -> integerTransform(argument) { ceilExact(it) }
            "round" -> integerTransform(argument) { roundExact(it) }
            else -> throw MathException(MathError.UnknownIdentifier(name, 0))
        }
    }

    private fun trig(
        value: NumericValue,
        zeroValue: NumericValue,
        operation: (Double) -> Double,
    ): NumericValue {
        if (value is NumericValue.Exact && value.rational.isZero()) {
            return zeroValue
        }
        val result = operation(toRadians(value.toDoubleValue()))
        if (abs(result) < 1e-12) return approximate(0.0)
        return approximate(result)
    }

    private fun tangent(value: NumericValue): NumericValue {
        if (value is NumericValue.Exact && value.rational.isZero()) {
            return NumericValue.Exact(Rational.ZERO)
        }
        val radians = toRadians(value.toDoubleValue())
        if (abs(cos(radians)) < 1e-12) throw MathException(MathError.DomainError("tan"))
        val result = Math.tan(radians)
        if (abs(result) < 1e-12) return approximate(0.0)
        return approximate(result)
    }

    private fun inverseTrig(value: NumericValue, operation: (Double) -> Double): NumericValue {
        val argument = value.toDoubleValue()
        if (argument < -1.0 || argument > 1.0) {
            throw MathException(MathError.DomainError("inverse trigonometry"))
        }
        val radians = operation(argument)
        return approximate(fromRadians(radians))
    }

    private fun logarithm(value: NumericValue, operation: (Double) -> Double): NumericValue {
        val argument = value.toDoubleValue()
        if (argument <= 0.0) throw MathException(MathError.DomainError("logarithm"))
        return approximate(operation(argument))
    }

    private fun squareRoot(value: NumericValue): NumericValue {
        if (value is NumericValue.Exact) {
            val rational = value.rational
            if (rational.numerator.signum() < 0) throw MathException(MathError.DomainError("sqrt"))
            val numeratorRoot = integerRootExact(rational.numerator, 2)
            val denominatorRoot = integerRootExact(rational.denominator, 2)
            if (numeratorRoot != null && denominatorRoot != null) {
                return exact(Rational.of(numeratorRoot, denominatorRoot))
            }
            return approximate(Math.sqrt(rational.toDouble()))
        }
        val approximateValue = value as NumericValue.Approximate
        if (approximateValue.value < 0.0) throw MathException(MathError.DomainError("sqrt"))
        return approximate(Math.sqrt(approximateValue.value))
    }

    private fun cubeRoot(value: NumericValue): NumericValue {
        if (value is NumericValue.Exact) {
            val rational = value.rational
            val negative = rational.numerator.signum() < 0
            val numeratorRoot = integerRootExact(rational.numerator.abs(), 3)
            val denominatorRoot = integerRootExact(rational.denominator, 3)
            if (numeratorRoot != null && denominatorRoot != null) {
                val root = Rational.of(numeratorRoot, denominatorRoot)
                return exact(if (negative) root.let { Rational.of(it.numerator.negate(), it.denominator) } else root)
            }
            return approximate(Math.cbrt(rational.toDouble()))
        }
        return approximate(Math.cbrt((value as NumericValue.Approximate).value))
    }

    private fun absolute(value: NumericValue): NumericValue = when (value) {
        is NumericValue.Exact -> exact(value.rational.abs())
        is NumericValue.Approximate -> approximate(abs(value.value))
    }

    private fun integerTransform(value: NumericValue, transform: (Rational) -> BigInteger): NumericValue {
        if (value is NumericValue.Exact) {
            return exact(Rational.of(transform(value.rational)))
        }
        val approximateValue = (value as NumericValue.Approximate).value
        return exact(Rational.of(transform(Rational.parseDecimal(approximateValue.toString()) ?: Rational.ZERO)))
    }

    private fun floorExact(value: Rational): BigInteger {
        val division = value.numerator.divideAndRemainder(value.denominator)
        return if (division[1].signum() < 0) division[0].subtract(BigInteger.ONE) else division[0]
    }

    private fun ceilExact(value: Rational): BigInteger {
        val division = value.numerator.divideAndRemainder(value.denominator)
        return if (division[1].signum() > 0) division[0].add(BigInteger.ONE) else division[0]
    }

    private fun roundExact(value: Rational): BigInteger =
        BigDecimal(value.numerator)
            .divide(BigDecimal(value.denominator), 0, RoundingMode.HALF_UP)
            .toBigIntegerExact()

    private fun integerRootExact(value: BigInteger, degree: Int): BigInteger? {
        if (value.signum() < 0) return null
        if (value.signum() == 0) return BigInteger.ZERO
        val floor = integerRootFloor(value, degree)
        return if (floor.pow(degree) == value) floor else null
    }

    private fun integerRootFloor(value: BigInteger, degree: Int): BigInteger {
        var low = BigInteger.ZERO
        var high = BigInteger.ONE.shiftLeft((value.bitLength() + degree - 1) / degree + 1)
        while (low.add(BigInteger.ONE) < high) {
            val middle = low.add(high).shiftRight(1)
            if (middle.pow(degree) <= value) low = middle else high = middle
        }
        return low
    }

    private fun toRadians(value: Double): Double =
        if (angleMode == AngleMode.Degrees) Math.toRadians(value) else value

    private fun fromRadians(value: Double): Double =
        if (angleMode == AngleMode.Degrees) Math.toDegrees(value) else value

    private fun exact(value: Rational): NumericValue.Exact {
        if (value.numerator.abs().bitLength() > MAX_EXACT_BITS || value.denominator.bitLength() > MAX_EXACT_BITS) {
            throw MathException(MathError.ResultTooLarge)
        }
        return NumericValue.Exact(value)
    }

    private fun approximate(value: Double): NumericValue.Approximate {
        if (value.isNaN()) throw MathException(MathError.DomainError("operation"))
        if (value.isInfinite()) throw MathException(MathError.Overflow)
        return NumericValue.Approximate(value)
    }

    private fun NumericValue.toDoubleValue(): Double = when (this) {
        is NumericValue.Exact -> rational.toDouble()
        is NumericValue.Approximate -> value
    }

    private fun NumericValue.isZeroValue(): Boolean = when (this) {
        is NumericValue.Exact -> rational.isZero()
        is NumericValue.Approximate -> value == 0.0
    }

    private fun NumericValue.displayValue(): String = when (this) {
        is NumericValue.Exact -> rational.toString()
        is NumericValue.Approximate -> value.toString()
    }
}
