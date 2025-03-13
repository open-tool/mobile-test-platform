package com.atiurin.atp.farmserver.device

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.util.waitForWithDelay
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.images.AndroidImagesConfiguration
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.util.NetUtil
import com.atiurin.atp.farmserver.util.nowSec
import com.farm.cli.command.DockerExecAdbBootAnimationCompletedCommand
import com.farm.cli.command.DockerExecAdbBootCompletedCommand
import com.github.dockerjava.api.model.Device
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import org.testcontainers.utility.DockerImageName

@Lazy
@Repository
class TestContainersDeviceRepository @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val androidImages: AndroidImagesConfiguration,
) : DeviceRepository {
    private val containerMap: MutableMap<String, FarmDevice> = mutableMapOf()

    override suspend fun createDevice(farmDevice: FarmDevice): FarmDevice {
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
            ip = NetUtil.localhostName,
            gRpcPort = container.getHostGrpcPort(),
            dockerImage = image
        )
        farmDevice.containerInfo = containerInfo
        farmDevice.container = container
        containerMap[farmDevice.id] = farmDevice
        log.info { "Device ${farmDevice.id} is booting. Created container $containerInfo, " }
        val bootTimeout = farmConfig.get().creatingDeviceTimeoutSec
        val isDeviceCreated = waitForWithDelay(timeoutMs = bootTimeout * 1000, intervalMs = 1000){
            isDeviceAlive(farmDevice.id)
        }
        val state = if (isDeviceCreated){
            log.info { "Change device ${farmDevice.id} state to ${DeviceState.READY} as it's booted." }
            DeviceState.READY
        } else {
            log.info { "Change device ${farmDevice.id} state to ${DeviceState.BROKEN} as it's not booted during $bootTimeout sec. $farmDevice" }
            DeviceState.BROKEN
        }
        farmDevice.stateTimestampSec = nowSec()
        farmDevice.state = state
        return farmDevice
    }

    override suspend fun deleteDevice(deviceId: String) {
        val device = containerMap[deviceId]?.let { farmDevice ->
            farmDevice.container?.stop()
        }
        containerMap.remove(deviceId)
    }

    override fun isDeviceAlive(deviceId: String): Boolean {
        val container = containerMap[deviceId]?.container ?: return false
        val sysBootCompleted = runBlocking {
            DockerExecAdbBootCompletedCommand(
                containerId = container.containerId,
                adbContainerPath = "${farmConfig.get().androidContainerAdbPath}/adb"
            ).execute()
        }
        val bootAnimCompleted = runBlocking {
            DockerExecAdbBootAnimationCompletedCommand(
                containerId = container.containerId,
                adbContainerPath = "${farmConfig.get().androidContainerAdbPath}/adb"
            ).execute()
        }
        return sysBootCompleted.success && bootAnimCompleted.success
    }

    override fun getDevices(): List<FarmDevice> = containerMap.values.toList()

    private fun startContainer(container: AndroidContainer<Nothing>) {
        container.apply {
            log.info { "Start container $container" }
            withPrivilegedMode(true)
            container.exposeAdbPort(farmConfig.getPortInRange())
            container.exposeGrpcPort(farmConfig.getPortInRange())
            start()
        }
    }
}