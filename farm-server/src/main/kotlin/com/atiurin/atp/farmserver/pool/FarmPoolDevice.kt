package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.models.PoolDevice
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.device.toDevice

data class FarmPoolDevice(
    var device: FarmDevice,
    var userAgent: String? = null,
    var isBusy: Boolean = false,
    var busyTimestamp: Long = 0L,
    var lastPingTimestamp: Long = 0L,
    var isBlocked: Boolean = false,
    var blockDesc : String? = null
)

fun FarmPoolDevice.toPoolDevice() = PoolDevice(
    device = this.device.toDevice(),
    userAgent = this.userAgent,
    isBusy = this.isBusy,
    isBlocked = this.isBlocked,
    busyTimestamp = this.busyTimestamp,
    lastPingTimestamp = this.lastPingTimestamp,
    blockDesc = this.blockDesc
)

