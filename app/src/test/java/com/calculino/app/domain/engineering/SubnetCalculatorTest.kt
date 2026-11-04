package com.calculino.app.domain.engineering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubnetCalculatorTest {
    @Test
    fun calculatesCidrNetwork() {
        val result = calculateSubnet("192.168.1.10/24", "")
        assertTrue(result is SubnetResult.Success)
        val info = (result as SubnetResult.Success).info
        assertEquals(24, info.prefixLength)
        assertEquals("192.168.1.0", info.network)
        assertEquals("192.168.1.255", info.broadcast)
        assertEquals("192.168.1.1", info.firstHost)
        assertEquals("192.168.1.254", info.lastHost)
        assertEquals(254L, info.usableHosts)
        assertEquals("255.255.255.0", info.subnetMask)
        assertEquals("0.0.0.255", info.wildcardMask)
    }

    @Test
    fun supportsPointToPointAndSingleHost() {
        val pointToPoint = calculateSubnet("10.0.0.4/31", "") as SubnetResult.Success
        assertEquals(2L, pointToPoint.info.usableHosts)
        assertEquals("10.0.0.4", pointToPoint.info.firstHost)
        assertEquals("10.0.0.5", pointToPoint.info.lastHost)
        val single = calculateSubnet("10.0.0.4/32", "") as SubnetResult.Success
        assertEquals(1L, single.info.usableHosts)
    }

    @Test
    fun rejectsInvalidInput() {
        assertTrue(calculateSubnet("10.0.0.256/24", "") is SubnetResult.InvalidIp)
        assertTrue(calculateSubnet("10.0.0.1/33", "") is SubnetResult.InvalidMask)
        assertTrue(calculateSubnet("10.0.0.1", "255.0.255.0") is SubnetResult.InvalidMask)
    }
}
