package com.atiurin.atp.farmserver.test.rest

import com.atiurin.atp.farmserver.test.di.FarmTestConfiguration
import com.atiurin.atp.farmserver.test.di.FarmTestConfiguration.Companion.initialConfig
import com.atiurin.atp.farmserver.test.rest.base.BaseRestControllerTest
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [FarmTestConfiguration::class],
)
class InitialConfigIntegrationTest : BaseRestControllerTest() {
    @Test
    fun `get current farm config`() {
        val config = configRestController.getCurrentConfig().config
        println(config)
        SoftAssertions().apply {
            assertThat(config.isMock).isTrue
            assertThat(config.busyDeviceTimeoutSec).isEqualTo(initialConfig.deviceBusyTimeoutSec)
            assertThat(config.keepAliveDevicesMap.isNotEmpty()).isTrue
            assertThat(config.maxDevicesAmount).isEqualTo(initialConfig.maxDevicesAmount)
            initialConfig.keepAliveDevicesMap.forEach { (groupId, amount) ->
                assertThat(config.keepAliveDevicesMap[groupId]).isEqualTo(amount)
            }
            assertThat(config.startPort).isEqualTo(initialConfig.startPort)
            assertThat(config.endPort).isEqualTo(initialConfig.endPort)
        }.assertAll()
    }
}