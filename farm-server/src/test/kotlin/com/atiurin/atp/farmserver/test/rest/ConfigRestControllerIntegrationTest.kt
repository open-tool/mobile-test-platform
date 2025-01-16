package com.atiurin.atp.farmserver.test.rest

import com.atiurin.atp.farmserver.test.di.FarmTestConfiguration
import com.atiurin.atp.farmserver.test.rest.base.BaseRestControllerTest
import org.assertj.core.api.Assertions
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
    fun `update busy device timeout`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val expectedTimeout = currentConfig.busyDeviceTimeoutSec + 300
        configRestController.updateBusyDeviceTimeout(expectedTimeout)
        val resultConfig = configRestController.getCurrentConfig().config
        Assertions.assertThat(resultConfig.busyDeviceTimeoutSec).isEqualTo(expectedTimeout)
    }

    @Test
    fun `update creating device timeout`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val expectedTimeout = currentConfig.creatingDeviceTimeoutSec + 300
        configRestController.updateCreatingDeviceTimeout(expectedTimeout)
        val resultConfig = configRestController.getCurrentConfig().config
        Assertions.assertThat(resultConfig.creatingDeviceTimeoutSec).isEqualTo(expectedTimeout)
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