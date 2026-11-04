package com.calculino.app.domain.engineering

import java.math.BigInteger

data class BaseValues(
    val decimal: String,
    val binary: String,
    val octal: String,
    val hexadecimal: String,
)

fun convertBases(input: String, base: Int): BaseValues? {
    val value = parseNonNegative(input, base) ?: return null
    return BaseValues(
        decimal = value.toString(10),
        binary = value.toString(2),
        octal = value.toString(8),
        hexadecimal = value.toString(16).uppercase(),
    )
}

val supportedBases = listOf(2, 8, 10, 16)

internal fun parseNonNegative(input: String, base: Int): BigInteger? {
    if (base !in supportedBases) return null
    val normalized = input.trim().replace(" ", "").replace("_", "")
    if (normalized.isEmpty()) return null
    val value = normalized.toBigIntegerOrNull(base) ?: return null
    return value.takeIf { it.signum() >= 0 }
}
