package com.calculino.app.domain.math

import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.PI

data class ComplexNumber(
    val real: Double,
    val imaginary: Double,
) {
    val magnitude: Double get() = hypot(real, imaginary)
    val phaseRadians: Double get() = atan2(imaginary, real)
    val phaseDegrees: Double get() = phaseRadians * 180.0 / PI

    operator fun plus(other: ComplexNumber): ComplexNumber =
        ComplexNumber(real + other.real, imaginary + other.imaginary)

    operator fun times(other: ComplexNumber): ComplexNumber = ComplexNumber(
        real = real * other.real - imaginary * other.imaginary,
        imaginary = real * other.imaginary + imaginary * other.real,
    )

    fun format(): String {
        val realText = formatComponent(real)
        val imaginaryText = formatComponent(imaginary)
        return when {
            imaginary.isNearlyZero() -> realText
            real.isNearlyZero() -> "${imaginaryText}i"
            imaginary < 0 -> "$realText - ${formatComponent(-imaginary)}i"
            else -> "$realText + ${imaginaryText}i"
        }
    }

    companion object {
        fun parse(input: String): ComplexNumber? {
            val normalized = input
                .replace(" ", "")
                .replace("−", "-")
                .replace("j", "i")
                .lowercase()
            if (normalized.isEmpty()) return null
            if (!normalized.contains('i')) {
                val real = normalized.toDoubleOrNull() ?: return null
                return ComplexNumber(real, 0.0)
            }
            if (!normalized.endsWith("i")) return null
            val body = normalized.dropLast(1)
            if (body.isEmpty() || body == "+") return ComplexNumber(0.0, 1.0)
            if (body == "-") return ComplexNumber(0.0, -1.0)
            val splitIndex = (body.lastIndex downTo 1).firstOrNull { index ->
                body[index] == '+' || body[index] == '-'
            } ?: -1
            if (splitIndex <= 0) {
                val imaginary = parseImaginary(body) ?: return null
                return ComplexNumber(0.0, imaginary)
            }
            val real = body.substring(0, splitIndex).toDoubleOrNull() ?: return null
            val imaginary = parseImaginary(body.substring(splitIndex)) ?: return null
            return ComplexNumber(real, imaginary)
        }

        private fun parseImaginary(text: String): Double? = when (text) {
            "+" -> 1.0
            "-" -> -1.0
            else -> text.toDoubleOrNull()
        }
    }
}

private fun formatComponent(value: Double): String {
    if (value.isNearlyZero()) return "0"
    val rounded = kotlin.math.round(value * 1_000_000.0) / 1_000_000.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}

private fun Double.isNearlyZero(): Boolean = kotlin.math.abs(this) < 1e-9
