package com.atiurin.atp.farmserver.repository

import com.atiurin.atp.farmcore.models.getPortInRange
import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmserver.config.FarmConfiguration
import com.atiurin.atp.farmserver.device.AndroidContainer
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.images.AndroidImagesConfiguration
import com.atiurin.atp.farmserver.logging.log
import com.github.dockerjava.api.model.Device
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@Repository
class TestContainersDeviceRepository @Autowired constructor(
    private val farmConfig: FarmConfiguration,
    private val androidImages: AndroidImagesConfiguration
) : DeviceRepository {
    private val containerMap: MutableMap<String, FarmDevice> = mutableMapOf()

    override fun createDevice(deviceInfo: DeviceInfo): FarmDevice {
        log.info { "Start device creation $deviceInfo" }
        val image = androidImages.get(deviceInfo.groupId)
        val container = AndroidContainer<Nothing>(DockerImageName.parse(image)).apply {
            withCreateContainerCmdModifier { cmd ->
                cmd.hostConfig?.withDevices(Device("rwm", "/dev/kvm", "/dev/kvm"))
            }
        }

        startContainer(container)
        val adbPort = container.getHostAdbPort()
        val gRpcPort = container.getHostGrpcPort()
        val hostName = NetUtil.getLocalhostName() ?: container.host
        log.info { "ip: $hostName, adbPort: $adbPort, gRpcPort: $gRpcPort" }
        val device = FarmDevice(
            UUID.randomUUID().toString(), deviceInfo,
            ContainerInfo(
                adbPort = adbPort,
                ip = hostName,
                gRpcPort = gRpcPort,
                dockerImage = image
            ),
            container
        )
        containerMap[container.containerId] = device
        return device
    }

    override fun deleteDevice(device: FarmDevice) {
        device.container?.let {
            it.stop()
            containerMap.remove(it.containerId)
        }
    }

    override fun isDeviceAlive(device: FarmDevice): Boolean = device.container?.isRunning ?: false

    override fun getDevices(): List<FarmDevice> = containerMap.values.toList()

    private fun startContainer(container: AndroidContainer<Nothing>) {
        container.apply {
            log.info { "Start container" }
            withPrivilegedMode(true)
            container.exposeAdbPort(farmConfig.get().getPortInRange())
            container.exposeGrpcPort(farmConfig.get().getPortInRange())
            start()
        }
    }
}