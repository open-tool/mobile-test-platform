package com.atiurin.atp.farmserver.test.rest.monitoring

import com.atiurin.atp.farmcore.api.model.toDevice
import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmserver.device.MockDeviceRepository
import com.atiurin.atp.farmserver.test.rest.BaseRestControllerTest
import com.atiurin.atp.farmserver.test.util.AssertUtils.awaitTrue
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [BaseRestControllerTest.FarmTestConfiguration::class],
)
class MonitorCreatingDeviceTest : BaseRestControllerTest() {
    @Test
    fun `monitor device stack in CREATING state`() {
        val currentConfig = configRestController.getCurrentConfig().config
        val group: String = currentConfig.keepAliveDevicesMap.keys.first()
        configRestController.updateGroupAmount(
            groupId = group, amount = 0
        )
        MockDeviceRepository.newDeviceState = DeviceState.CREATING
        configRestController.updateGroupAmount(
            groupId = group, amount = 2
        )
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.filter { it.device.groupId == group }
            },
            assertionBlock = { devices ->
                devices.count { it.device.toDevice().state == DeviceState.CREATING } == 2
            },
            timeoutMs = 5000L,
            delay = 1000,
            desc = { actualAmount ->
                "Assert devices with proper state"
            }
        )
        configRestController.updateCreatingDeviceTimeout(1)
        MockDeviceRepository.newDeviceState = DeviceState.READY
        awaitTrue(
            valueProviderBlock = {
                deviceRestController.list().poolDevices.filter { it.device.groupId == group }
            },
            assertionBlock = { devices ->
                devices.count { it.device.toDevice().state == DeviceState.READY } == 2
            },
            timeoutMs = 5000L,
            delay = 1000,
            desc = { actualAmount ->
                "Assert devices with proper state"
            }
        )
    }
}