package com.atiurin.atp.farmserver.test.rest

import com.atiurin.atp.farmcore.models.Config
import com.atiurin.atp.farmcore.models.FarmMode
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.config.InitialConfig
import com.atiurin.atp.farmserver.config.toConfig
import com.atiurin.atp.farmserver.db.Devices
import com.atiurin.atp.farmserver.db.Servers
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.rest.ConfigRestController
import com.atiurin.atp.farmserver.rest.DeviceRestController
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext

open class BaseRestControllerTest {
    companion object {
        val initialConfig = InitialConfig(
            maxDevicesAmount = 50,
            keepAliveDevicesMap = mutableMapOf("30" to 10),//, "31" to 7),
            deviceBusyTimeoutSec = 1000,
            isMock = true,
            imagesMap = mutableMapOf("30" to "init30image", "31" to "init31image"),
            startPort = 10000,
            endPort = 11000,
            farmMode = FarmMode.Multiple
        )
    }

    @BeforeEach
    fun cleanUp() {
        if (initialConfig.farmMode == FarmMode.Multiple){
            log.info { "Clean up database" }
            deviceRestController.list().poolDevices.forEach {
                deviceRestController.remove(it.device.id)
            }

            transaction {
                SchemaUtils.dropSchema()
                SchemaUtils.create(Devices, Servers)
            }
        }
    }

    @LocalServerPort
    var appPort: Int = 0

    fun endpoint(url: String) = "http://localhost:$appPort/$url"
    var client = TestRestTemplate()

    @Autowired
    lateinit var configRestController: ConfigRestController

    @Autowired
    lateinit var deviceRestController: DeviceRestController

    @TestConfiguration
    internal class FarmTestConfiguration {
        @Bean
        fun farmConfig(): FarmConfig = object : FarmConfig {
            val config: Config = initialConfig.toConfig().copy(
                devicePoolMonitorDelay = 1000,
                busyDevicesMonitorDelay = 1000,
                serverMonitorDelay = 1000,
                deviceNeedToDeleteMonitorDelay = 1000,
                deviceNeedToCreateMonitorDelay = 1000
            )

            override fun set(block: Config.() -> Unit) {
                config.block()
            }

            override fun get(): Config = config
        }
    }
}