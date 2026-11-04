package com.calculino.app.domain.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsCalculatorTest {
    @Test
    fun calculatesDescriptiveStatistics() {
        val result = StatisticsCalculator.calculate("2, 4, 4, 4, 5, 5, 7, 9")
        assertTrue(result is StatisticsOutcome.Success)
        val statistics = (result as StatisticsOutcome.Success).result
        assertEquals(8, statistics.count)
        assertEquals(5.0, statistics.mean, 1e-9)
        assertEquals(4.5, statistics.median, 1e-9)
        assertEquals(listOf(4.0), statistics.mode)
        assertEquals(4.0, statistics.variance, 1e-9)
        assertEquals(2.0, statistics.standardDeviation, 1e-9)
    }

    @Test
    fun handlesEmptyAndInvalidData() {
        assertTrue(StatisticsCalculator.calculate("") is StatisticsOutcome.InvalidData)
        assertTrue(StatisticsCalculator.calculate("1,abc,3") is StatisticsOutcome.InvalidData)
        assertNull(StatisticsCalculator.parseDataset(" "))
    }

    @Test
    fun parsesDecimalCommasWithSemicolons() {
        val values = StatisticsCalculator.parseDataset("2,5; 3,5; 4")
        assertEquals(listOf(2.5, 3.5, 4.0), values)
    }

    @Test
    fun calculatesCombinatorics() {
        assertEquals("10", StatisticsCalculator.combinations(5, 2)?.toString())
        assertEquals("60", StatisticsCalculator.permutations(5, 3)?.toString())
        assertNull(StatisticsCalculator.combinations(2, 3))
        assertNull(StatisticsCalculator.permutations(-1, 1))
    }
}
