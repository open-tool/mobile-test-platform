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
class MonitorRemovesFreeDevicesTest  : BaseRestControllerTest() {
    @Test
    @DirtiesContext
    fun `monitor removes only free devices if necessary`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.last()
        val totalDevices = currentConfig.keepAliveDevicesMap[group] ?: 0

        val busyAmount = totalDevices - 3
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.count { it.device.groupId == group }
            },
            assertionBlock = { amount ->
                amount == totalDevices
            },
            timeoutMs = 5000L,
            delay = 500,
            desc = { actualAmount ->
                "Assert devices currently alive amount = $totalDevices, actual = $actualAmount"
            }
        )
        deviceRestController.acquire(amount = busyAmount, groupId = group, userAgent = "test")
        val newAmount = (currentConfig.keepAliveDevicesMap[group] ?: 0) - 5
        configRestController.updateGroupAmount(
            groupId = group, amount = newAmount
        )
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.count { it.device.groupId == group }
            },
            assertionBlock = { amount ->
                amount == busyAmount
            },
            timeoutMs = 5000L,
            delay = 1000,
            desc = { actualAmount ->
                "Assert devices created and result amount is expected to be = $busyAmount, actual = $actualAmount"
            }
        )
        log.info { "Monitor amount: ${deviceRestController.list().poolDevices.count { it.device.groupId == group }}" }
    }
}