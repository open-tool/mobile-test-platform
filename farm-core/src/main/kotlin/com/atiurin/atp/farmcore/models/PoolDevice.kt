package com.atiurin.atp.farmcore.models

data class PoolDevice (
    val device: Device,
    val userAgent: String? = null,
    val isBusy: Boolean = false,
    val busyTimestamp: Long = 0L,
    val isBlocked: Boolean = false,
    val blockDesc: String? = null
)