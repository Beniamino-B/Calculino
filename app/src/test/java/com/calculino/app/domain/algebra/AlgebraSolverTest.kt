package com.calculino.app.domain.algebra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlgebraSolverTest {
    @Test
    fun respectsOperatorPrecedence() {
        val result = AlgebraSolver.solve("2x + 3 * 4 = 18")
        assertTrue(result is AlgebraResult.Success)
        assertEquals("x = 3", (result as AlgebraResult.Success).solution.headline)
    }

    @Test
    fun solvesQuadraticEquations() {
        val result = AlgebraSolver.solve("x^2 - 5x + 6 = 0")
        assertTrue(result is AlgebraResult.Success)
        assertEquals(
            "x₁ = 2, x₂ = 3",
            (result as AlgebraResult.Success).solution.headline,
        )
    }

    @Test
    fun solvesTwoVariableSystems() {
        val result = AlgebraSolver.solve("2x + y = 7\nx - y = 2")
        assertTrue(result is AlgebraResult.Success)
        assertEquals("x = 3, y = 1", (result as AlgebraResult.Success).solution.headline)
    }

    @Test
    fun invalidInputNeverThrows() {
        listOf("", "x^", "1/0", "2x +", "sin(x)=0", "x=1\ny=2\nz=3").forEach { input ->
            val result = AlgebraSolver.solve(input)
            assertTrue(result is AlgebraResult.Failure)
        }
    }
}
