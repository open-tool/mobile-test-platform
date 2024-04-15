package com.atiurin.atp.farmserver.repository

import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice

interface DeviceRepository {
    fun createDevice(deviceInfo: DeviceInfo): FarmDevice
    fun deleteDevice(device: FarmDevice)
    fun isDeviceAlive(device: FarmDevice): Boolean
    fun getDevices(): List<FarmDevice>
}