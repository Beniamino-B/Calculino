package com.calculino.app.domain.practice

import com.calculino.app.domain.math.Rational

object PracticeAnswerParser {

    private const val MAX_LENGTH = 18

    fun parse(raw: String): Rational? {
        val text = raw.trim()
            .replace('−', '-')
            .replace(',', '.')
            .filterNot { it.isWhitespace() }
        if (text.isEmpty() || text.length > MAX_LENGTH) return null
        if (text.any { !it.isDigit() && it != '.' && it != ',' && it != '-' && it != '/' }) return null
        if (text.count { it == '/' } > 1) return null
        return try {
            val parts = text.split('/')
            if (parts.size == 1) {
                Rational.parseDecimal(parts[0])
            } else {
                val numerator = Rational.parseDecimal(parts[0]) ?: return null
                val denominator = Rational.parseDecimal(parts[1]) ?: return null
                if (denominator.isZero()) null else numerator / denominator
            }
        } catch (_: ArithmeticException) {
            null
        }
    }
}
