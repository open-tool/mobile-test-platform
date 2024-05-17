package com.atiurin.atp.farmserver.device

import com.atiurin.atp.farmcore.models.DeviceState
import com.atiurin.atp.farmcore.models.getPortInRange
import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.images.AndroidImagesConfiguration
import com.atiurin.atp.farmserver.logging.log
import com.github.dockerjava.api.model.Device
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@Lazy
@Repository
class TestContainersDeviceRepository @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val androidImages: AndroidImagesConfiguration
) : DeviceRepository {
    private val containerMap: MutableMap<String, FarmDevice> = mutableMapOf()

    override fun createDevice(farmDevice: FarmDevice): FarmDevice {
        log.info { "Start device creation $farmDevice" }
        val image = androidImages.get(farmDevice.deviceInfo.groupId)
        val container = AndroidContainer<Nothing>(DockerImageName.parse(image)).apply {
            withCreateContainerCmdModifier { cmd ->
                cmd.hostConfig?.withDevices(Device("rwm", "/dev/kvm", "/dev/kvm"))
            }
        }

        startContainer(container)
        val containerInfo = ContainerInfo(
            adbPort = container.getHostAdbPort(),
            ip = NetUtil.getLocalhostName() ?: container.host,
            gRpcPort = container.getHostGrpcPort(),
            dockerImage = image
        )
        log.info { "Created container $containerInfo" }
        farmDevice.containerInfo = containerInfo
        farmDevice.container = container
        farmDevice.state = DeviceState.READY
        containerMap[farmDevice.id] = farmDevice
        return farmDevice
    }

    override fun deleteDevice(deviceId: String) {
        val device = containerMap[deviceId]?.let { farmDevice ->
            farmDevice.container?.stop()
        }
        containerMap.remove(deviceId)
    }

    override fun isDeviceAlive(deviceId: String): Boolean =
        containerMap[deviceId]?.container?.isRunning ?: false

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