package com.atiurin.atp.farmclient.test.integration

import com.atiurin.atp.farmclient.FarmClient
import com.atiurin.atp.farmclient.FarmClientConfig
import org.junit.jupiter.api.Test

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
}