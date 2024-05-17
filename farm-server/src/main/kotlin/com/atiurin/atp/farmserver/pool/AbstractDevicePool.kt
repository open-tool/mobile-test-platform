package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.models.DeviceState
import com.atiurin.atp.farmcore.models.DeviceStatus
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import java.util.UUID

abstract class AbstractDevicePool: DevicePool {

    fun initDevice(groupId: String) = initDevice(DeviceInfo("AutoLaunched group '$groupId'", groupId))

    fun initDevice(deviceInfo: DeviceInfo) = FarmPoolDevice(
        device = FarmDevice(
            id = UUID.randomUUID().toString(),
            deviceInfo = deviceInfo,
            state = DeviceState.CREATING,
            containerInfo = ContainerInfo("", 0, 0, "")
        ),
        status = DeviceStatus.FREE,
        desc = "Creating new device"
    )
}