package com.calculino.app.domain.math

import java.math.BigInteger
import kotlin.math.sqrt

data class StatisticsResult(
    val count: Int,
    val mean: Double,
    val median: Double,
    val mode: List<Double>,
    val variance: Double,
    val standardDeviation: Double,
    val minimum: Double,
    val maximum: Double,
)

sealed interface StatisticsOutcome {
    data class Success(val result: StatisticsResult) : StatisticsOutcome
    data object InvalidData : StatisticsOutcome
}

object StatisticsCalculator {
    fun parseDataset(input: String): List<Double>? {
        val normalized = input.trim()
        if (normalized.isEmpty()) return null
        val separators = if (';' in normalized || '\n' in normalized) {
            charArrayOf(';', '\n', ' ', '\t')
        } else {
            charArrayOf(',', ' ', '\t')
        }
        val values = normalized
            .split(*separators)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { token -> token.replace(',', '.').toDoubleOrNull() ?: return null }
        if (values.isEmpty() || values.any { !it.isFinite() }) return null
        return values
    }

    fun calculate(input: String): StatisticsOutcome {
        val values = parseDataset(input) ?: return StatisticsOutcome.InvalidData
        return StatisticsOutcome.Success(calculate(values))
    }

    fun calculate(values: List<Double>): StatisticsResult {
        require(values.isNotEmpty()) { "dataset must not be empty" }
        val sorted = values.sorted()
        val count = values.size
        val mean = values.sum() / count
        val median = if (count % 2 == 0) {
            (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
        } else {
            sorted[count / 2]
        }
        val frequencies = values.groupingBy { it }.eachCount()
        val highestFrequency = frequencies.values.max()
        val mode = if (highestFrequency <= 1) {
            emptyList()
        } else {
            frequencies.filterValues { frequency -> frequency == highestFrequency }.keys.sorted()
        }
        val variance = values.sumOf { value ->
            val difference = value - mean
            difference * difference
        } / count
        return StatisticsResult(
            count = count,
            mean = mean,
            median = median,
            mode = mode,
            variance = variance,
            standardDeviation = sqrt(variance),
            minimum = sorted.first(),
            maximum = sorted.last(),
        )
    }

    fun combinations(n: Int, r: Int): BigInteger? {
        if (!isValidCombinatorics(n, r)) return null
        return binomial(n, r)
    }

    fun permutations(n: Int, r: Int): BigInteger? {
        if (!isValidCombinatorics(n, r)) return null
        var result = BigInteger.ONE
        repeat(r) { index ->
            result = result.multiply(BigInteger.valueOf((n - index).toLong()))
        }
        return result
    }

    private fun isValidCombinatorics(n: Int, r: Int): Boolean =
        n in 0..MAX_COMBINATORICS_INPUT && r in 0..n

    private fun binomial(n: Int, r: Int): BigInteger {
        val k = minOf(r, n - r)
        var result = BigInteger.ONE
        repeat(k) { index ->
            result = result
                .multiply(BigInteger.valueOf((n - index).toLong()))
                .divide(BigInteger.valueOf((index + 1).toLong()))
        }
        return result
    }
}

private const val MAX_COMBINATORICS_INPUT = 10_000
