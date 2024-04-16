package com.atiurin.atp.farmserver.test.rest

import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.rest.ConfigRestController
import com.atiurin.atp.farmserver.rest.DeviceRestController
import com.atiurin.atp.farmserver.test.util.AssertUtils.awaitTrue
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [BaseRestControllerTest.FarmTestConfiguration::class],
)
@DirtiesContext
class ConfigRestControllerIntegrationTest : BaseRestControllerTest() {
    @Test
    fun `update keep alive devices amount`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
        val expectedAmount = (currentConfig.keepAliveDevicesMap[group] ?: 0) + 5
        configRestController.updateGroupAmount(
            groupId = group, amount = expectedAmount
        )
        val resultConfig = configRestController.getCurrentConfig().config
        Assertions.assertThat(resultConfig.keepAliveDevicesMap[group]).isEqualTo(expectedAmount)
    }

    @Test
    fun `update keep alive devices amount more than allowed`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
        val expectedAmount = (currentConfig.keepAliveDevicesMap[group] ?: 0)
        val response = configRestController.updateGroupAmount(
            groupId = group, amount = currentConfig.maxDevicesAmount + 5
        )
        val resultConfig = configRestController.getCurrentConfig().config
        SoftAssertions().apply {
            assertThat(resultConfig.keepAliveDevicesMap[group]).isEqualTo(expectedAmount)
            response.success = false
        }.assertAll()
    }

    @Test
    fun `update device busy timeout`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val expectedTimeout = currentConfig.deviceBusyTimeoutSec + 300
        configRestController.updateDeviceBusyTimeout(expectedTimeout)
        val resultConfig = configRestController.getCurrentConfig().config
        Assertions.assertThat(resultConfig.deviceBusyTimeoutSec).isEqualTo(expectedTimeout)
    }

    @Test
    fun `update max device amount`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val expectedAmount = currentConfig.maxDevicesAmount + 10
        configRestController.updateMaxDevices(expectedAmount)
        val resultConfig = configRestController.getCurrentConfig().config
        Assertions.assertThat(resultConfig.maxDevicesAmount).isEqualTo(expectedAmount)
    }
}