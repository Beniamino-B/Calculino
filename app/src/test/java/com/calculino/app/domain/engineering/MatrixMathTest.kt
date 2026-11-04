package com.calculino.app.domain.engineering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatrixMathTest {
    @Test
    fun calculatesInverseWithGaussianElimination() {
        val matrix = NumericMatrix(2, 2, listOf(4.0, 7.0, 2.0, 6.0))
        val inverse = inverseMatrix(matrix)
        val product = multiplyMatrices(matrix, inverse!!)
        assertEquals(1.0, product!!.values[0], 1e-9)
        assertEquals(0.0, product.values[1], 1e-9)
        assertEquals(0.0, product.values[2], 1e-9)
        assertEquals(1.0, product.values[3], 1e-9)
    }

    @Test
    fun rejectsSingularMatrix() {
        val matrix = NumericMatrix(2, 2, listOf(1.0, 2.0, 2.0, 4.0))
        assertNull(inverseMatrix(matrix))
    }

    @Test
    fun multipliesRectangularMatrices() {
        val first = NumericMatrix(2, 3, listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0))
        val second = NumericMatrix(3, 2, listOf(7.0, 8.0, 9.0, 10.0, 11.0, 12.0))
        val product = multiplyMatrices(first, second)!!
        assertEquals(listOf(58.0, 64.0, 139.0, 154.0), product.values)
        assertNull(multiplyMatrices(first, first))
    }
}
