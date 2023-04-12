package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmserver.ContainerInfo
import com.atiurin.atp.farmserver.DeviceInfo
import com.atiurin.atp.farmserver.FarmDevice
import com.atiurin.atp.farmserver.images.AndroidImage
import com.github.dockerjava.api.model.Device
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.util.*

class TestContainersDeviceProvider : DeviceProvider {
    override fun createDevice(info: DeviceInfo): FarmDevice {
        val image = AndroidImage.get(info.api)
        val container = GenericContainer<Nothing>(DockerImageName.parse(image)).apply {
            withCreateContainerCmdModifier { cmd ->
                cmd.hostConfig?.withDevices(Device("rwm", "/dev/kvm", "/dev/kvm"))
            }
        }

        startContainer(container)
        val adbPort = container.getMappedPort(5555)
        val gRpcPort = container.getMappedPort(8554)
        println("ip: ${container.host}, adbPort: $adbPort, gRpcPort: $gRpcPort")
        return FarmDevice(
            UUID.randomUUID().toString(), info,
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

    private fun startContainer(container: GenericContainer<Nothing>) {
        container.apply {
            println("Start container")
            withPrivilegedMode(true)
            withExposedPorts(5555)
            withExposedPorts(8554)
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