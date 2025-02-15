package com.atiurin.atp.farmserver.test.rest.base

import com.atiurin.atp.farmcore.entity.FarmMode
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.db.Devices
import com.atiurin.atp.farmserver.db.Servers
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.rest.ConfigRestController
import com.atiurin.atp.farmserver.rest.DeviceRestController
import com.atiurin.atp.farmserver.rest.ServerRestController
import com.atiurin.atp.farmserver.test.di.FarmTestConfiguration.Companion.defaultConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort

open class BaseRestControllerTest {
    @Autowired
    lateinit var farmConfig: FarmConfig

    @BeforeEach
    fun cleanUp() {
        if (farmConfig.get().farmMode == FarmMode.MULTIPLE) {
            log.info { "Clean up database" }
            deviceRestController.list().poolDevices.forEach {
                deviceRestController.remove(it.device.id)
            }

            transaction {
                SchemaUtils.dropSchema()
                SchemaUtils.create(Devices, Servers)
            }
        }
        configRestController.setConfig(defaultConfig)
    }

    @LocalServerPort
    var appPort: Int = 0

    fun endpoint(url: String) = "http://localhost:$appPort/$url"
    var client = TestRestTemplate()

    @Autowired
    lateinit var configRestController: ConfigRestController

    @Autowired
    lateinit var deviceRestController: DeviceRestController

    @Autowired
    lateinit var serverRestController: ServerRestController
}