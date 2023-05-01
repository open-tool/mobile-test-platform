package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmserver.ContainerInfo
import com.atiurin.atp.farmserver.DeviceInfo
import com.atiurin.atp.farmserver.FarmDevice
import java.util.*

class MockDeviceProvider : DeviceProvider {
    override fun createDevice(info: DeviceInfo): FarmDevice {
        return FarmDevice(
            id = UUID.randomUUID().toString(),
            deviceInfo = DeviceInfo("mock_device", 30),
            containerInfo = ContainerInfo("mock_ip", 5555, 6666, "mock_image"),
        )
    }

    override fun deleteDevice(device: FarmDevice)  = Unit

    override fun isDeviceAlive(device: FarmDevice): Boolean = true
}
