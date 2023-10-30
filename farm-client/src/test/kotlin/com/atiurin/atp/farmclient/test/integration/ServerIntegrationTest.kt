package com.atiurin.atp.farmclient.test.integration

import com.atiurin.atp.farmclient.FarmClient
import com.atiurin.atp.farmclient.FarmClientConfig
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.*

class ServerIntegrationTest {
    val client = FarmClient(FarmClientConfig(
        farmUrl = "http://localhost:8080",
        userAgent = "test"
    ))

    @Test
    fun acquireAndReleaseTest(){
        val devices = client.acquire(3, "30")
        client.release(deviceIds = devices.map { it.id })
    }

    @Test
    fun timestamp(){
        val t = System.currentTimeMillis()
        val date = Date(1698387811182)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        val formattedDate = sdf.format(date)

        println(formattedDate)
    }
}