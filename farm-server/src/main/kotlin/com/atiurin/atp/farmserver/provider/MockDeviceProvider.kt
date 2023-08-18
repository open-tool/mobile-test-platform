package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmserver.ContainerInfo
import com.atiurin.atp.farmserver.DeviceInfo
import com.atiurin.atp.farmserver.FarmDevice
import java.lang.Math.random
import java.util.*

class MockDeviceProvider : DeviceProvider {
    override fun createDevice(info: DeviceInfo): FarmDevice {
        return FarmDevice(
            id = UUID.randomUUID().toString(),
            deviceInfo = DeviceInfo("mock_device_${info.groupId}", info.groupId),
            containerInfo = ContainerInfo("mock_ip_${info.groupId}", NetUtil.getFreePort(), NetUtil.getFreePort(), "mock_image_${info.groupId}"),
        )
    }

    override fun deleteDevice(device: FarmDevice)  = Unit

    override fun isDeviceAlive(device: FarmDevice): Boolean = true
}
