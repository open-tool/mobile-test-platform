package com.atiurin.atp.farmserver.device

import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.entity.DeviceState
import org.testcontainers.containers.GenericContainer

data class FarmDevice(
    val id: String,
    var state: DeviceState,
    val deviceInfo: DeviceInfo,
    var containerInfo: ContainerInfo,
    var container: GenericContainer<Nothing>? = null
)

fun FarmDevice.toDevice() = Device(
    id = this.id,
    name = this.deviceInfo.name,
    groupId = this.deviceInfo.groupId,
    dockerImage = this.containerInfo.dockerImage,
    ip = this.containerInfo.ip,
    adbConnectPort = this.containerInfo.adbPort,
    state = this.state
)

data class DeviceInfo(
    val name: String,
    val groupId: String
)

data class ContainerInfo(
    val ip: String,
    val adbPort: Int,
    val gRpcPort: Int?,
    val dockerImage: String
)

