package com.atiurin.atp.farmcliclient.adb

import com.atiurin.atp.farmcore.entity.Device

interface AdbServer {
    val port: Int
    fun start()
    fun kill()
    fun connect(devices: List<Device>)
    suspend fun connect(device: Device, timeoutSec: Long = 5 * 60) : Result<Device>
    fun disconnect(devices: List<Device>)
    fun printDevices()
}