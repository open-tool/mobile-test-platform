package com.atiurin.atp.farmserver.test.rest

import com.atiurin.atp.farmcore.models.Config
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.config.InitialConfig
import com.atiurin.atp.farmserver.config.toConfig
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext

open class BaseRestControllerTest {
    companion object {
        val initialConfig = InitialConfig(
            maxDevicesAmount = 50,
            keepAliveDevicesMap = mutableMapOf("30" to 10, "31" to 7),
            deviceBusyTimeoutSec = 1000,
            isMock = true,
            imagesMap = mutableMapOf("30" to "init30image", "31" to "init31image"),
            startPort = 10000,
            endPort = 11000
        )
    }
    @LocalServerPort
    var appPort: Int = 0

    fun endpoint(url: String) = "http://localhost:$appPort/$url"

    var client = TestRestTemplate()

    @BeforeEach
    @DirtiesContext
    fun setup() {}

    @TestConfiguration
    internal class FarmTestConfiguration {
        @Bean
        fun farmConfig(): FarmConfig = object : FarmConfig {
            val config: Config = initialConfig.toConfig().apply {
                devicePoolMonitorDelay = 1000
                busyDevicesMonitorDelay = 1000
            }

            override fun set(block: Config.() -> Unit) {
                config.block()
            }

            override fun get(): Config = config
        }
    }
}