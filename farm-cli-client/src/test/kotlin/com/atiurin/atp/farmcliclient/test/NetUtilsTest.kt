package com.atiurin.atp.farmcliclient.test

import com.atiurin.atp.farmcliclient.test.AssertionUtil.assertException
import com.atiurin.atp.farmserver.util.NetUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.ServerSocket

class NetUtilsTest {
    @Test
    fun getFreePortInRange_rangeSpecified(){
        val busyPort = 8090
        val maxPort = 9000
        val socket = ServerSocket(busyPort).use {
            val port = NetUtil.getFreePortInRange(busyPort, maxPort)
            println("port: $port")
            Assertions.assertNotEquals(busyPort, port)
            Assertions.assertTrue(port > busyPort)
            Assertions.assertTrue(port < maxPort)
        }
    }

    @Test
    fun getFreePortInRange_allPortsInRangeAreBusy_throwException(){
        val busyPort = 8190
        ServerSocket(busyPort).use {
            assertException {
                NetUtil.getFreePortInRange(busyPort, busyPort)
            }
        }
    }

    @Test
    fun getFreePortInRangeTest_zeroPortProvided(){
        val port = NetUtil.getFreePortInRange(0)
        Assertions.assertTrue(port > 0)
    }
}