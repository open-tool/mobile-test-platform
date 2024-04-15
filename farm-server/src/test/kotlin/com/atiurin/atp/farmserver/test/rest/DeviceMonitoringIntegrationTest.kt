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
class DeviceMonitoringIntegrationTest : BaseRestControllerTest() {
    @Autowired
    lateinit var configRestController: ConfigRestController

    @Autowired
    lateinit var deviceRestController: DeviceRestController

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
    }

    @Test
    fun `update keep alive devices amount decreases number of alive devices`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
        val currentAmount = currentConfig.keepAliveDevicesMap[group] ?: 0
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
            timeoutMs = 5000L,
            delay = 1000,
            desc = { actualAmount ->
                "Assert devices created and result amount is expected to be = $expectedAmount, actual = $actualAmount"
            }
        )
    }

    @Test
    fun `monitor removes only free devices if necessary`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
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
    }
}