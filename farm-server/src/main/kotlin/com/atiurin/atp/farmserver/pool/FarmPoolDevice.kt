package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.models.DeviceStatus
import com.atiurin.atp.farmcore.models.PoolDevice
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.device.toDevice

data class FarmPoolDevice(
    var device: FarmDevice,
    var userAgent: String? = null,
    var busyTimestamp: Long = 0L,
    var lastPingTimestamp: Long = 0L,
    var status: DeviceStatus = DeviceStatus.FREE,
    var desc : String? = null
)


fun FarmPoolDevice.toPoolDevice() = PoolDevice(
    device = this.device.toDevice(),
    userAgent = this.userAgent,
    status = this.status,
    state = this.device.state,
    busyTimestamp = this.busyTimestamp,
    lastPingTimestamp = this.lastPingTimestamp,
    desc = this.desc
)

