package com.atiurin.atp.farmserver

import com.atiurin.atp.farmcore.models.Device
import org.springframework.data.relational.core.mapping.Table
import org.testcontainers.containers.GenericContainer

data class FarmDevice(
    val id: String,
    val deviceInfo: DeviceInfo,
    var containerInfo: ContainerInfo,
    var container: GenericContainer<Nothing>? = null
)

fun FarmDevice.toDevice() = Device(
    id = this.id,
    name = this.deviceInfo.name,
    apiLevel = this.deviceInfo.api,
    dockerImage = this.deviceInfo.dockerImage,
    ip = this.containerInfo.ip,
    adbConnectPort = this.containerInfo.adbPort,
    telnetPort = this.containerInfo.telnetPort,
    vncPort = this.containerInfo.vncPort,
    adbServerSocketPort = this.containerInfo.adbServerSocketPort
)

data class DeviceInfo(
    val name: String,
    val api: Int,
    val dockerImage: String
)

data class ContainerInfo(
    val ip: String,
    val adbPort: Int,
    val telnetPort: Int,
    val vncPort: Int,
    val adbServerSocketPort: Int
)

