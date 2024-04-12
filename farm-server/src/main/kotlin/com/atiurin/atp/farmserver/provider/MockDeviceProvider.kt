package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmcore.models.getPortInRange
import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.images.AndroidImage
import java.util.UUID

class MockDeviceProvider : DeviceProvider {
    private val containerMap: MutableMap<String, FarmDevice> = mutableMapOf()

    override fun createDevice(deviceInfo: DeviceInfo): FarmDevice {
        val config = ConfigProvider.get()
        runCatching {
            AndroidImage.get(deviceInfo.groupId)
        }.onFailure {
            AndroidImage.update(deviceInfo.groupId, "mock_image_${deviceInfo.groupId}")
        }
        val dockerImg = AndroidImage.get(deviceInfo.groupId)
        val device = FarmDevice(
            id = UUID.randomUUID().toString(),
            deviceInfo = DeviceInfo("mock_device_${deviceInfo.groupId}", deviceInfo.groupId),
            containerInfo = ContainerInfo(
                "mock_ip_${deviceInfo.groupId}",
                config.getPortInRange(),
                config.getPortInRange(),
                dockerImg
            ),
        )
        containerMap[device.id] = device
        return device
    }

    override fun deleteDevice(device: FarmDevice){
        containerMap.remove(device.id)
    }

    override fun isDeviceAlive(device: FarmDevice): Boolean = containerMap.any { it.key == device.id }

    override fun getDevices(): List<FarmDevice> = containerMap.values.toList()
}
