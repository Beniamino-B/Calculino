package com.calculino.app.domain.engineering

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BaseBitwiseTest {
    @Test
    fun convertsSupportedBases() {
        val values = convertBases("FF", 16)
        assertNotNull(values)
        assertEquals("255", values?.decimal)
        assertEquals("11111111", values?.binary)
        assertEquals("377", values?.octal)
        assertEquals("FF", values?.hexadecimal)
    }

    @Test
    fun rejectsNegativeAndInvalidDigits() {
        assertNull(convertBases("-10", 10))
        assertNull(convertBases("2", 2))
        assertNull(convertBases("", 10))
    }

    @Test
    fun appliesBitwiseOperations() {
        val and = applyBitwise("12", "10", 10, BitwiseOperation.And)
        assertEquals("8", and?.bases?.decimal)
        val or = applyBitwise("12", "10", 10, BitwiseOperation.Or)
        assertEquals("14", or?.bases?.decimal)
        val xor = applyBitwise("12", "10", 10, BitwiseOperation.Xor)
        assertEquals("6", xor?.bases?.decimal)
        val not = applyBitwise("0", "", 10, BitwiseOperation.Not)
        assertEquals(BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE).toString(), not?.value?.toString())
        val shiftLeft = applyBitwise("1", "4", 10, BitwiseOperation.ShiftLeft)
        assertEquals("16", shiftLeft?.bases?.decimal)
        val shiftRight = applyBitwise("16", "4", 10, BitwiseOperation.ShiftRight)
        assertEquals("1", shiftRight?.bases?.decimal)
    }

    @Test
    fun rejectsInvalidShift() {
        assertNull(applyBitwise("1", "64", 10, BitwiseOperation.ShiftLeft))
        assertNull(applyBitwise("1", "64", 10, BitwiseOperation.ShiftRight))
        assertNull(applyBitwise("1", "4294967297", 10, BitwiseOperation.ShiftLeft))
    }
}
