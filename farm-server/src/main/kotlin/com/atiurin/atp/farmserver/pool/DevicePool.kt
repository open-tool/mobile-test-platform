package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmserver.device.DeviceInfo
import java.util.concurrent.LinkedBlockingQueue

interface DevicePool {
    fun all(): List<FarmPoolDevice>
    fun count(predicate: (FarmPoolDevice) -> Boolean): Int
    fun remove(deviceId: String)
    fun removeAll(groupId: String)
    fun removeDeviceInState(amount: Int = -1, state: DeviceState = DeviceState.NEED_REMOVE)
    fun removeDeviceInStatus(amount: Int = -1, groupId: String, status: DeviceStatus = DeviceStatus.BUSY)
    fun create(amount: Int = 1, deviceInfo: DeviceInfo, status: DeviceStatus = DeviceStatus.FREE, creatingDeviceQueue: LinkedBlockingQueue<FarmPoolDevice> = LinkedBlockingQueue()): List<FarmPoolDevice>
    fun createNeededDevices(): List<FarmPoolDevice>
    fun acquire(amount: Int = 1, groupId: String, userAgent: String): List<FarmPoolDevice>
    fun release(deviceId: String)
    fun release(deviceIds: List<String>)
    fun releaseAll(groupId: String)
    fun block(deviceId: String, desc: String)
    fun unblock(deviceId: String)
    fun isAlive(deviceId: String): FarmPoolDevice
}