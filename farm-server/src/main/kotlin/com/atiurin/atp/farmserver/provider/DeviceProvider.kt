package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice

interface DeviceProvider {
    fun createDevice(info: DeviceInfo): FarmDevice
    fun deleteDevice(device: FarmDevice)
    fun isDeviceAlive(device: FarmDevice): Boolean
}