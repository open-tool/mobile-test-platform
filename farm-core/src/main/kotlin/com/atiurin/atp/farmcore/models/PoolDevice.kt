package com.atiurin.atp.farmcore.models

data class PoolDevice (
    val device: Device,
    val userAgent: String? = null,
    var status: DeviceStatus = DeviceStatus.FREE,
    var state: DeviceState = DeviceState.CREATING,
    var lastPingTimestamp: Long = 0L,
    val busyTimestamp: Long = 0L,
    var desc : String? = null
)