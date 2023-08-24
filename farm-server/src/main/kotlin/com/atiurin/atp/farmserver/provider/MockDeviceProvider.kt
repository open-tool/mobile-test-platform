package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.config.getPortInRange
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import java.util.*

class MockDeviceProvider : DeviceProvider {
    override fun createDevice(info: DeviceInfo): FarmDevice {
        val config = ConfigProvider.get()
        return FarmDevice(
            id = UUID.randomUUID().toString(),
            deviceInfo = DeviceInfo("mock_device_${info.groupId}", info.groupId),
            containerInfo = ContainerInfo("mock_ip_${info.groupId}", config.getPortInRange(), config.getPortInRange(), "mock_image_${info.groupId}"),
        )
    }

    override fun deleteDevice(device: FarmDevice)  = Unit

    override fun isDeviceAlive(device: FarmDevice): Boolean = true
}
