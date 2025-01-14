package com.atiurin.atp.farmcore.entity

import com.atiurin.atp.farmcore.api.model.ApiPoolDevice

data class PoolDevice (
    val device: Device,
    val userAgent: String? = null,
    var status: DeviceStatus = DeviceStatus.FREE,
    var lastPingTimestampSec: Long = 0L,
    val statusTimestampSec: Long = 0L,
    var desc : String? = null
)

fun PoolDevice.toApiPoolDevice() = ApiPoolDevice(
    device = this.device.toApiDevice(),
    userAgent = this.userAgent,
    status = this.status.name,
    lastPingTimestampSec = this.lastPingTimestampSec,
    statusTimestampSec = this.statusTimestampSec,
    desc = this.desc
)

fun List<PoolDevice>.toApiPoolDevices(): List<ApiPoolDevice> = map { it.toApiPoolDevice() }
fun List<PoolDevice>.toDevices(): List<Device> = map { it.device }