package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.config.getPortInRange
import com.atiurin.atp.farmserver.device.AndroidContainer
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.images.AndroidImage
import com.atiurin.atp.farmserver.logging.log
import com.github.dockerjava.api.model.Device
import org.testcontainers.utility.DockerImageName
import java.util.UUID

class TestContainersDeviceProvider : DeviceProvider {
    override fun createDevice(deviceInfo: DeviceInfo): FarmDevice {
        log.info { "Start device creation $deviceInfo" }
        val image = AndroidImage.get(deviceInfo.groupId)
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
        return FarmDevice(
            UUID.randomUUID().toString(), deviceInfo,
            ContainerInfo(
                adbPort = adbPort,
                ip = hostName,
                gRpcPort = gRpcPort,
                dockerImage = image
            ),
            container
        )
    }

    override fun deleteDevice(device: FarmDevice) {
        device.container?.stop()
    }

    override fun isDeviceAlive(device: FarmDevice): Boolean = device.container?.isRunning ?: false

    private fun startContainer(container: AndroidContainer<Nothing>) {
        container.apply {
            log.info { "Start container" }
            withPrivilegedMode(true)
            container.exposeAdbPort(ConfigProvider.get().getPortInRange())
            container.exposeGrpcPort(ConfigProvider.get().getPortInRange())
            start()
        }
    }
}