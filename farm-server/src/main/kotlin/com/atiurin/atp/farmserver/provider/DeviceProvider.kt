package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice

interface DeviceProvider {
    fun createDevice(deviceInfo: DeviceInfo): FarmDevice
    fun deleteDevice(device: FarmDevice)
    fun isDeviceAlive(device: FarmDevice): Boolean
    fun getDevices(): List<FarmDevice>
}