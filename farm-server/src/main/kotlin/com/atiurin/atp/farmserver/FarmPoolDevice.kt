package com.atiurin.atp.farmserver

import com.atiurin.atp.farmcore.models.PoolDevice

data class FarmPoolDevice(
    var device: FarmDevice,
    var userAgent: String? = null,
    var isBusy: Boolean = false,
    var busyTimestamp: Long = 0L,
    var isBlocked: Boolean = false,
    var blockDesc : String? = null
)

fun FarmPoolDevice.toPoolDevice() = PoolDevice(
    this.device.toDevice(),
    this.userAgent,
    this.isBusy,
    this.busyTimestamp,
    this.isBlocked,
    this.blockDesc
)

