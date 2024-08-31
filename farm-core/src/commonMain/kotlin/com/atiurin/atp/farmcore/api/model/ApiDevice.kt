package com.atiurin.atp.farmcore.api.model

import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.entity.DeviceState
import kotlinx.serialization.Serializable

@Serializable
data class ApiDevice (
    val id: String,
    val name: String,
    val groupId: String,
    val dockerImage: String,
    val ip: String,
    val adbConnectPort: Int,
    val state: String
)

fun ApiDevice.toDevice(): Device = Device(
    id = this.id,
    name = this.name,
    groupId = this.groupId,
    dockerImage = this.dockerImage,
    ip = this.ip,
    adbConnectPort = this.adbConnectPort,
    state = DeviceState.valueOf(this.state)
)

fun List<ApiDevice>.toDevices() : List<Device> = this.map { it.toDevice() }