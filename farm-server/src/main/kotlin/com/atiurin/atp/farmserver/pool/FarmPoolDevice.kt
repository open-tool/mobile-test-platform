package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmcore.entity.PoolDevice
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.device.toDevice

data class FarmPoolDevice(
    var device: FarmDevice,
    var userAgent: String? = null,
    var statusTimestampSec: Long = 0L,
    var lastPingTimestampSec: Long = 0L,
    var status: DeviceStatus = DeviceStatus.FREE,
    var desc : String? = null
)


fun FarmPoolDevice.toPoolDevice() = PoolDevice(
    device = this.device.toDevice(),
    userAgent = this.userAgent,
    status = this.status,
    statusTimestampSec = this.statusTimestampSec,
    lastPingTimestampSec = this.lastPingTimestampSec,
    desc = this.desc
)

fun List<FarmPoolDevice>.toDevices(): List<Device> = map { it.device.toDevice() }
fun List<FarmPoolDevice>.toPoolDevices(): List<PoolDevice> = map { it.toPoolDevice() }