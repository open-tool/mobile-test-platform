package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.models.DeviceState
import com.atiurin.atp.farmserver.device.DeviceInfo

interface DevicePool {
    fun all(): List<FarmPoolDevice>
    fun count(predicate: (FarmPoolDevice) -> Boolean): Int
    fun remove(deviceId: String)
    fun removeAll(groupId: String)
    fun removeDeviceInState(state: DeviceState = DeviceState.NEED_REMOVE)
    fun create(amount: Int = 1, deviceInfo: DeviceInfo): List<FarmPoolDevice>
    fun createNeededDevices(): List<FarmPoolDevice>
    fun acquire(amount: Int = 1, groupId: String, userAgent: String): List<FarmPoolDevice>
    fun release(deviceId: String)
    fun release(deviceIds: List<String>)
    fun releaseAll(groupId: String)
    fun block(deviceId: String, desc: String)
    fun unblock(deviceId: String)
}