package com.atiurin.atp.farmcliclient.test

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.commands.AcquireCommand
import com.atiurin.atp.kmpclient.FarmClientConfig
import com.atiurin.atp.kmpclient.getFarmUrlFromString
import org.junit.jupiter.api.Test

class AcquireCommandTest {
    @Test
    fun acquireDeviceCommandTest(){
        val farmUrls = listOf(getFarmUrlFromString("http://localhost:8080/"))
        FarmClientProvider.init(
            FarmClientConfig(
                farmUrls = farmUrls,
                userAgent = "test"
            )
        )
        val cmd = AcquireCommand(3, "28", deviceConnectionTimeoutMs = 60_000)
        cmd.execute()
        println()
    }
}