package com.atiurin.atp.farmcliclient.services

interface DeviceConnectionService {
    /**
     * Connects to the farm and acquires devices
     * Check device state and connect to it once it in [DeviceState.READY] state
     */
    fun connect(amount: Int = 1, groupId: String)
    fun disconnect()
}