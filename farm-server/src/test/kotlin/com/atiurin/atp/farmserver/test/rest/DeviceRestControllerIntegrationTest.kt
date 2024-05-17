package com.atiurin.atp.farmserver.test.rest

import com.atiurin.atp.farmcore.models.DeviceStatus
import com.atiurin.atp.farmserver.test.util.AssertUtils.awaitTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [BaseRestControllerTest.FarmTestConfiguration::class],
)
class DeviceRestControllerIntegrationTest : BaseRestControllerTest() {
    @Test
    fun `create additional devices once acquire request`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.last()
        val availableNow = deviceRestController.list().poolDevices.count {
            it.device.groupId == group && it.status == DeviceStatus.FREE
        }
        val expectedAmount = availableNow + 3
        val acquiredDevices = deviceRestController.acquire(expectedAmount, group, "Test")
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.count {
                    it.device.groupId == group && it.status == DeviceStatus.BUSY
                }
            },
            assertionBlock = { actualAmount ->
                actualAmount == expectedAmount
            },
            desc = { actualAmount ->
                "Expected busy devices = $expectedAmount, actual = $actualAmount"
            }
        )
    }

    @Test
    fun `released devices are removed`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
        val initialAmount = deviceRestController.list().poolDevices.count {
            it.device.groupId == group
        }
        val devices = deviceRestController.acquire(3, group, "Test").devices
        deviceRestController.release(devices.map { it.id })
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.filter { it.device.groupId == group }
            },
            assertionBlock = { groupDevices ->
                groupDevices.count { groupDevice -> devices.any { device -> device.id == groupDevice.device.id } } == 0
            },
            desc = { groupDevices ->
                "Assert released devices were deleted $devices, but they exist = $groupDevices"
            }
        )
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.count {
                    it.device.groupId == group
                }
            },
            assertionBlock = { actualAmount ->
                actualAmount == initialAmount
            },
            desc = { actualAmount ->
                "Assert devices amount is restored, expected = $initialAmount, actual = $actualAmount"
            }
        )
    }

}