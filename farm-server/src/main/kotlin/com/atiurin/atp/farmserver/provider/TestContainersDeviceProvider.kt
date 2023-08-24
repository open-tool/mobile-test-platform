package com.atiurin.atp.farmserver.provider

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
        log.info { "Start device creation $deviceInfo"}
        val image = AndroidImage.get(deviceInfo.groupId)
        val container = AndroidContainer<Nothing>(DockerImageName.parse(image)).apply {
            withCreateContainerCmdModifier { cmd ->
                cmd.hostConfig?.withDevices(Device("rwm", "/dev/kvm", "/dev/kvm"))
            }
        }

        startContainer(container)
        val adbPort = container.getHostAdbPort()
        val gRpcPort = container.getHostGrpcPort()
        log.info { "ip: ${container.host}, adbPort: $adbPort, gRpcPort: $gRpcPort" }
        return FarmDevice(
            UUID.randomUUID().toString(), deviceInfo,
            ContainerInfo(
                adbPort = adbPort,
                ip = container.host,
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
//            Wait.forHealthcheck()

//            val adb = "/android/sdk/platform-tools/adb"
//            var emulStarted = false
//            while (!emulStarted) {
//                val outLines = container.execInContainer(adb, "devices").stdout.reader().readLines()
//                outLines.forEach { line ->
//                    if (line.contains("emulator-") and line.contains("device")) emulStarted = true
//                }
//            }
//            println("Emulator started")
//            var packageServiceStarted = false
//            while (!packageServiceStarted) {
//                val outLines = container.execInContainer(
//                    adb,
//                    "shell",
//                    "service",
//                    "check",
//                    "package"
//                ).stdout.reader().readLines()
//                outLines.forEach { line ->
//                    if (line == "Service package: found") packageServiceStarted = true
//                }
//            }
//            println("Service package: found")
//            println("Emulator ${container.containerId} started")
        }
    }
}