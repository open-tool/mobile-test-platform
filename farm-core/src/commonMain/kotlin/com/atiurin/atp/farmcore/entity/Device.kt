package com.atiurin.atp.farmcore.entity

import com.atiurin.atp.farmcore.api.model.ApiDevice

data class Device(
    val id: String,
    val name: String,
    val groupId: String,
    val dockerImage: String,
    val ip: String,
    val adbConnectPort: Int,
    val state: DeviceState,
    val stateTimestampSec: Long,
)

fun Device.toApiDevice() = ApiDevice(
    id = this.id,
    name = this.name,
    groupId = this.groupId,
    dockerImage = this.dockerImage,
    ip = this.ip,
    adbConnectPort = this.adbConnectPort,
    state = this.state.name,
    stateTimestampSec = this.stateTimestampSec
)

fun List<Device>.toApiDevices() : List<ApiDevice> = this.map { it.toApiDevice() }