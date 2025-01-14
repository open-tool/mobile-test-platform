package com.atiurin.atp.farmserver.test.rest.monitoring

import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.test.rest.BaseRestControllerTest
import com.atiurin.atp.farmserver.test.util.AssertUtils.awaitTrue
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [BaseRestControllerTest.FarmTestConfiguration::class],
)
@DirtiesContext
class DeviceMonitoringIntegrationTest : BaseRestControllerTest() {
    @Test
    fun `update keep alive devices amount increases number of alive devices`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
        val expectedAmount = (currentConfig.keepAliveDevicesMap[group] ?: 0) + 5
        configRestController.updateGroupAmount(
            groupId = group, amount = expectedAmount
        )
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.count { it.device.groupId == group }
            },
            assertionBlock = { amount ->
                amount == expectedAmount
            },
            timeoutMs = 5000L,
            delay = 1000,
            desc = { actualAmount ->
                "Assert devices created and result amount is expected to be = $expectedAmount, actual = $actualAmount"
            }
        )
        log.info { "TROLOLO" }
    }

    @Test
    fun `update keep alive devices amount decreases number of alive devices`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
        val currentAmount = currentConfig.keepAliveDevicesMap[group] ?: 0
        val ids = deviceRestController.list().poolDevices.map { it.device.id }
        deviceRestController.release(ids)
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.count { it.device.groupId == group }
            },
            assertionBlock = { amount ->
                amount == currentAmount
            },
            timeoutMs = 5000L,
            delay = 500,
            desc = { actualAmount ->
                "Assert devices currently alive amount = $currentAmount, actual = $actualAmount"
            }
        )
        val expectedAmount = (currentConfig.keepAliveDevicesMap[group] ?: 0) - 3
        configRestController.updateGroupAmount(
            groupId = group, amount = expectedAmount
        )
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.count { it.device.groupId == group }
            },
            assertionBlock = { amount ->
                amount == expectedAmount
            },
            timeoutMs = 10000L,
            delay = 1000,
            desc = { actualAmount ->
                "Assert devices created and result amount is expected to be = $expectedAmount, actual = $actualAmount"
            }
        )
    }


}