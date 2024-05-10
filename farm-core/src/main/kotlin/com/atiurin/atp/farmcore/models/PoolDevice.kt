package com.atiurin.atp.farmcore.models

data class PoolDevice (
    val device: Device,
    val userAgent: String? = null,
    val isBusy: Boolean = false,
    val isBlocked: Boolean = false,
    val busyTimestamp: Long = 0L,
    var lastPingTimestamp: Long = 0L,
    var lastAliveTimestamp: Long = 0L,
    val blockDesc: String? = null
)