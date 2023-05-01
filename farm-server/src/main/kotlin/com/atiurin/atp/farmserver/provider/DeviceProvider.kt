package com.atiurin.atp.farmserver.provider

import com.atiurin.atp.farmserver.DeviceInfo
import com.atiurin.atp.farmserver.FarmDevice

interface DeviceProvider {
    fun createDevice(info: DeviceInfo): FarmDevice
    fun deleteDevice(device: FarmDevice)
    fun isDeviceAlive(device: FarmDevice): Boolean
}