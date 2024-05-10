package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.device.FarmDevice

interface DevicePool {
    fun all(): List<FarmPoolDevice>
    fun count(predicate: (FarmPoolDevice) -> Boolean): Int
    fun remove(deviceId: String)
    fun join(device: FarmDevice)
    fun create(amount: Int = 1, groupId: String)
    fun acquire(amount: Int = 1, groupId: String, userAgent: String): List<FarmDevice>
    fun release(deviceId: String)
    fun release(deviceIds: List<String>)
    fun releaseAll(groupId: String)
    fun block(deviceId: String, desc: String)
    fun unblock(deviceId: String)
}