package com.atiurin.atp.farmcore.api.model

import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmcore.entity.PoolDevice
import kotlinx.serialization.Serializable

@Serializable
data class ApiPoolDevice(
    val device: ApiDevice,
    val userAgent: String? = null,
    val status: String,
    val lastPingTimestampSec: Long = 0L,
    val busyTimestampSec: Long = 0L,
    val desc: String? = null
)

fun ApiPoolDevice.toPoolDevice() = PoolDevice(
    device = this.device.toDevice(),
    userAgent = this.userAgent,
    status = DeviceStatus.valueOf(this.status),
    lastPingTimestampSec = this.lastPingTimestampSec,
    busyTimestampSec = this.busyTimestampSec,
    desc = this.desc
)

fun List<ApiPoolDevice>.toPoolDevices() : List<PoolDevice> = this.map { it.toPoolDevice() }