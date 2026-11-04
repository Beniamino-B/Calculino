package com.calculino.app.domain.engineering

import java.math.BigInteger

enum class BitwiseOperation {
    And,
    Or,
    Xor,
    Not,
    ShiftLeft,
    ShiftRight,
}

data class BitwiseResult(
    val operation: BitwiseOperation,
    val value: BigInteger,
    val bases: BaseValues,
)

fun applyBitwise(
    leftInput: String,
    rightInput: String,
    base: Int,
    operation: BitwiseOperation,
): BitwiseResult? {
    val left = parseNonNegative(leftInput, base) ?: return null
    if (left.bitLength() > MAX_BITS) return null
    val right = if (operation == BitwiseOperation.Not) {
        BigInteger.ZERO
    } else {
        parseNonNegative(rightInput, base) ?: return null
    }
    if (right.bitLength() > MAX_BITS) return null
    val value = when (operation) {
        BitwiseOperation.And -> left.and(right)
        BitwiseOperation.Or -> left.or(right)
        BitwiseOperation.Xor -> left.xor(right)
        BitwiseOperation.Not -> MASK_64.xor(left)
        BitwiseOperation.ShiftLeft -> {
            if (right > MAX_SHIFT) return null
            left.shiftLeft(right.toInt())
        }
        BitwiseOperation.ShiftRight -> {
            if (right > MAX_SHIFT) return null
            left.shiftRight(right.toInt())
        }
    }
    if (value.signum() < 0 || value.bitLength() > MAX_BITS) return null
    return BitwiseResult(
        operation = operation,
        value = value,
        bases = BaseValues(
            decimal = value.toString(10),
            binary = value.toString(2),
            octal = value.toString(8),
            hexadecimal = value.toString(16).uppercase(),
        ),
    )
}

private const val MAX_BITS = 64
private val MASK_64 = BigInteger.ONE.shiftLeft(MAX_BITS).subtract(BigInteger.ONE)
private val MAX_SHIFT = BigInteger.valueOf(MAX_BITS - 1L)
