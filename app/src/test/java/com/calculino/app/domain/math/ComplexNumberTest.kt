package com.calculino.app.domain.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ComplexNumberTest {
    @Test
    fun parsesRectangularForms() {
        val number = ComplexNumber.parse("3+4i")
        assertNotNull(number)
        assertEquals(3.0, number!!.real, 1e-9)
        assertEquals(4.0, number.imaginary, 1e-9)
        assertEquals(5.0, number.magnitude, 1e-9)
        assertEquals(53.13010235, number.phaseDegrees, 1e-6)
    }

    @Test
    fun parsesNegativeAndPureImaginaryForms() {
        val negative = ComplexNumber.parse("-3-4i")
        assertEquals(-3.0, negative!!.real, 1e-9)
        assertEquals(-4.0, negative.imaginary, 1e-9)
        val imaginary = ComplexNumber.parse("-i")
        assertEquals(-1.0, imaginary!!.imaginary, 1e-9)
        assertNull(ComplexNumber.parse("3+4"))
    }

    @Test
    fun addsAndMultiplies() {
        val first = ComplexNumber.parse("3+4i")!!
        val second = ComplexNumber.parse("1-2i")!!
        assertEquals("4 + 2i", (first + second).format())
        assertEquals("11 - 2i", (first * second).format())
    }
}
