package com.atiurin.atp.farmserver.test

import com.atiurin.atp.farmserver.util.NetUtil
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NetUtilTest {
    @Test
    fun portArrangeTest(){
        val port = NetUtil.getFreePort()
        Assert.assertTrue(port > 0)
    }

    @Test
    fun getPortRangeTest(){
        val port  = NetUtil.getFreePortInRange(10000, 11000)
        println("port: $port")
        
        Assert.assertTrue(port in 10000..10999)
    }
}