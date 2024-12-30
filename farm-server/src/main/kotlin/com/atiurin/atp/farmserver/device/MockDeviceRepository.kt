package com.atiurin.atp.farmserver.device

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.images.AndroidImagesConfiguration
import com.atiurin.atp.farmserver.util.NetUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MockDeviceRepository @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val androidImages: AndroidImagesConfiguration
) : DeviceRepository {
    private val containerMap: MutableMap<String, FarmDevice> = mutableMapOf()

    override fun createDevice(farmDevice: FarmDevice): FarmDevice {
        val config = farmConfig.get()
        val groupId = farmDevice.deviceInfo.groupId
        runCatching {
            androidImages.get(groupId)
        }.onFailure {
            androidImages.update(groupId, "mock_image_${groupId}")
        }
        val dockerImg = androidImages.get(groupId)
        farmDevice.containerInfo = ContainerInfo(
            NetUtil.localhostName,
            NetUtil.getFreePortInRange(farmConfig.get().startPort, farmConfig.get().endPort),
            NetUtil.getFreePortInRange(farmConfig.get().startPort, farmConfig.get().endPort),
            dockerImg
        )
        farmDevice.state = DeviceState.READY
        containerMap[farmDevice.id] = farmDevice
        return farmDevice
    }

    override fun deleteDevice(deviceId: String) {
        containerMap.remove(deviceId)
    }

    override fun isDeviceAlive(deviceId: String): Boolean = containerMap.any { it.key == deviceId }

    override fun getDevices(): List<FarmDevice> = containerMap.values.toList()
}
