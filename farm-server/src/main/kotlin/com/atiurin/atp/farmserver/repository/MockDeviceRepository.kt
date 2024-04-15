package com.atiurin.atp.farmserver.repository

import com.atiurin.atp.farmcore.models.getPortInRange
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.images.AndroidImagesConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class MockDeviceRepository @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val androidImages: AndroidImagesConfiguration
) : DeviceRepository {
    private val containerMap: MutableMap<String, FarmDevice> = mutableMapOf()

    override fun createDevice(deviceInfo: DeviceInfo): FarmDevice {
        val config = farmConfig.get()
        runCatching {
            androidImages.get(deviceInfo.groupId)
        }.onFailure {
            androidImages.update(deviceInfo.groupId, "mock_image_${deviceInfo.groupId}")
        }
        val dockerImg = androidImages.get(deviceInfo.groupId)
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
