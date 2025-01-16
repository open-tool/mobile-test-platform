package com.atiurin.atp.farmserver.test.rest.base

import com.atiurin.atp.farmcore.api.model.toPoolDevice
import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmserver.test.di.FarmTestConfiguration.Companion.defaultConfig
import com.atiurin.atp.farmserver.test.util.AssertUtils.awaitTrue
import org.junit.jupiter.api.BeforeEach

open class BaseRestControllerTestWithDevices : BaseRestControllerTest() {
    @BeforeEach
    fun waitDevicesToBeInited() {
        configRestController.updateGroupAmount("30", 10)
        defaultConfig.keepAliveDevicesMap.forEach { (groupId, amount) ->
            awaitTrue(
                valueProviderBlock = {
                    deviceRestController.list().poolDevices.filter {
                        val poolDevice = it.toPoolDevice()
                        poolDevice.device.groupId == groupId
                                && poolDevice.device.state == DeviceState.READY
                                && poolDevice.status == DeviceStatus.FREE
                    }
                },
                assertionBlock = { groupDevices ->
                    groupDevices.size == amount
                },
                desc = { groupDevices ->
                    "Wait group devices amount = $amount as specified in default config, but they exist = $groupDevices"
                }
            )
        }
    }
}