package com.atiurin.atp.farmserver.test.di

import com.atiurin.atp.farmcore.entity.Config
import com.atiurin.atp.farmcore.entity.FarmMode
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.config.InitialConfig
import com.atiurin.atp.farmserver.config.toConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
internal class FarmTestConfiguration {
    companion object {
        val initialConfig = InitialConfig(
            maxDevicesAmount = 50,
            keepAliveDevicesMap = mutableMapOf("30" to 0), //, "31" to 7),
            deviceBusyTimeoutSec = 1000,
            isMock = true,
            imagesMap = mutableMapOf("30" to "init30image", "31" to "init31image"),
            startPort = 10000,
            endPort = 11000,
        )

        val defaultConfig: Config
            get() = initialConfig.toConfig(FarmMode.MULTIPLE).copy(
                devicePoolMonitorDelay = 1000,
                busyDevicesMonitorDelay = 1000,
                serverMonitorDelay = 1000,
                deviceNeedToDeleteMonitorDelay = 2000,
                deviceNeedToCreateMonitorDelay = 1000,
                creatingDeviceTimeoutSec = 1000,
                creatingDevicesMonitorDelay = 1000
            )
    }

    @Bean
    fun farmConfig(): FarmConfig = object : FarmConfig {
        var config = defaultConfig

        override fun set(block: Config.() -> Unit) {
            config.block()
        }

        override fun set(config: Config) {
            this.config = config
        }

        override fun get(): Config = config
    }
}