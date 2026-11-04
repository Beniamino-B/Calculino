package com.calculino.app.domain.engineering

data class SubnetInfo(
    val prefixLength: Int,
    val network: String,
    val broadcast: String,
    val firstHost: String,
    val lastHost: String,
    val usableHosts: Long,
    val subnetMask: String,
    val wildcardMask: String,
)

sealed interface SubnetResult {
    data class Success(val info: SubnetInfo) : SubnetResult
    data object InvalidIp : SubnetResult
    data object InvalidMask : SubnetResult
}

fun calculateSubnet(ipInput: String, maskInput: String): SubnetResult {
    val ipText = ipInput.substringBefore('/').trim()
    val ip = parseIpv4(ipText) ?: return SubnetResult.InvalidIp
    val cidr = ipInput.substringAfter('/', "").trim()
    val prefix = when {
        cidr.isNotEmpty() -> cidr.toIntOrNull()?.takeIf { it in 0..32 }
        else -> parseMask(maskInput)
    } ?: return SubnetResult.InvalidMask

    val mask = prefixMask(prefix)
    val network = ip and mask
    val broadcast = network or mask.inv().and(UINT32_MASK)
    val total = 1L shl (32 - prefix)
    val usableHosts = when (prefix) {
        32 -> 1L
        31 -> 2L
        else -> total - 2
    }
    val firstHost = if (prefix >= 31) network else network + 1
    val lastHost = if (prefix >= 31) broadcast else broadcast - 1

    return SubnetResult.Success(
        SubnetInfo(
            prefixLength = prefix,
            network = formatIpv4(network),
            broadcast = formatIpv4(broadcast),
            firstHost = formatIpv4(firstHost),
            lastHost = formatIpv4(lastHost),
            usableHosts = usableHosts,
            subnetMask = formatIpv4(mask),
            wildcardMask = formatIpv4(mask.inv().and(UINT32_MASK)),
        ),
    )
}

private fun parseMask(input: String): Int? {
    val normalized = input.trim().removePrefix("/")
    normalized.toIntOrNull()?.let { prefix ->
        return prefix.takeIf { it in 0..32 }
    }
    val mask = parseIpv4(normalized) ?: return null
    val bits = mask.toString(2).padStart(32, '0')
    if (!bits.matches(Regex("1*0*"))) return null
    return bits.count { bit -> bit == '1' }
}

private fun parseIpv4(input: String): Long? {
    val parts = input.split('.')
    if (parts.size != 4) return null
    var value = 0L
    for (part in parts) {
        val octet = part.toIntOrNull()?.takeIf { it in 0..255 } ?: return null
        value = (value shl 8) or octet.toLong()
    }
    return value
}

private fun prefixMask(prefix: Int): Long {
    if (prefix == 0) return 0L
    return UINT32_MASK shl (32 - prefix) and UINT32_MASK
}

private fun formatIpv4(value: Long): String {
    return listOf(
        value shr 24,
        value shr 16,
        value shr 8,
        value,
    ).joinToString(".") { octet -> (octet and 0xFF).toString() }
}

private const val UINT32_MASK = 0xFFFFFFFFL
