package com.atiurin.atp.farmserver.device

interface DeviceRepository {
    suspend fun createDevice(farmDevice: FarmDevice): FarmDevice
    suspend fun deleteDevice(deviceId: String)
    fun isDeviceAlive(deviceId: String): Boolean
    fun getDevices(): List<FarmDevice>
}