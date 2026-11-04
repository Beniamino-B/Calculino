package com.calculino.app.domain.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

class Rational private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger,
) {
    operator fun plus(other: Rational): Rational =
        of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Rational): Rational =
        of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Rational): Rational =
        of(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Rational): Rational {
        if (other.numerator.signum() == 0) throw ArithmeticException("division by zero")
        return of(numerator * other.denominator, denominator * other.numerator)
    }

    fun isInteger(): Boolean = denominator == BigInteger.ONE

    fun isZero(): Boolean = numerator.signum() == 0

    fun abs(): Rational = if (numerator.signum() < 0) Rational(numerator.negate(), denominator) else this

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    fun toBigDecimalExact(): BigDecimal? {
        val negative = numerator.signum() < 0
        var remainder = denominator
        var twos = 0
        var fives = 0
        while (remainder.mod(TWO).signum() == 0) {
            remainder = remainder.divide(TWO)
            twos++
        }
        while (remainder.mod(FIVE).signum() == 0) {
            remainder = remainder.divide(FIVE)
            fives++
        }
        if (remainder != BigInteger.ONE) return null
        val scale = maxOf(twos, fives)
        val scaled = numerator.abs().multiply(BigInteger.TEN.pow(scale)).divide(denominator)
        val value = BigDecimal(scaled, scale).stripTrailingZeros()
        return if (negative) value.negate() else value
    }

    fun toDecimalApproximation(digits: Int): BigDecimal =
        BigDecimal(numerator).divide(BigDecimal(denominator), MathContext(digits, RoundingMode.HALF_UP))

    fun pow(exponent: Int): Rational {
        if (exponent == 0) return ONE
        return if (exponent > 0) {
            of(numerator.pow(exponent), denominator.pow(exponent))
        } else {
            of(denominator.pow(-exponent), numerator.pow(-exponent))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rational) return false
        return numerator == other.numerator && denominator == other.denominator
    }

    override fun hashCode(): Int = 31 * numerator.hashCode() + denominator.hashCode()

    override fun toString(): String =
        if (denominator == BigInteger.ONE) numerator.toString() else "$numerator/$denominator"

    companion object {
        val ZERO = Rational(BigInteger.ZERO, BigInteger.ONE)
        val ONE = Rational(BigInteger.ONE, BigInteger.ONE)
        private val TWO = BigInteger.valueOf(2)
        private val FIVE = BigInteger.valueOf(5)

        fun of(value: Long): Rational = of(BigInteger.valueOf(value))

        fun of(value: BigInteger): Rational = Rational(value, BigInteger.ONE)

        fun of(numerator: BigInteger, denominator: BigInteger): Rational {
            if (denominator.signum() == 0) throw ArithmeticException("division by zero")
            var n = numerator
            var d = denominator
            if (d.signum() < 0) {
                n = n.negate()
                d = d.negate()
            }
            val divisor = n.gcd(d)
            if (divisor.signum() == 0) return ZERO
            return Rational(n.divide(divisor), d.divide(divisor))
        }

        fun parseDecimal(text: String): Rational? {
            val normalized = when {
                text.startsWith(".") -> "0$text"
                text.endsWith(".") -> "${text}0"
                else -> text
            }
            val decimal = try {
                BigDecimal(normalized)
            } catch (_: NumberFormatException) {
                return null
            }
            val unscaled = decimal.unscaledValue()
            val scale = decimal.scale()
            return if (scale >= 0) {
                of(unscaled, BigInteger.TEN.pow(scale))
            } else {
                of(unscaled.multiply(BigInteger.TEN.pow(-scale)), BigInteger.ONE)
            }
        }
    }
}
