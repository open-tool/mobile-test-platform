package com.atiurin.atp.farmserver.device

interface DeviceRepository {
    fun createDevice(farmDevice: FarmDevice): FarmDevice
    fun deleteDevice(deviceId: String)
    fun isDeviceAlive(deviceId: String): Boolean
    fun getDevices(): List<FarmDevice>
}