package com.atiurin.atp.farmserver

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.lang.RuntimeException

object DevicePool {
    private val devices = mutableListOf<FarmPoolDevice>()

    fun all() = devices.toList()

    fun remove(deviceId: String){
        synchronized(devices){
            GlobalScope.async {
                devices.find { it.device.id == deviceId }?.let {
                    it.device.container?.stop()
                }
                devices.removeIf {
                    it.device.id == deviceId
                }
            }
        }
    }

    fun join(device: FarmDevice){
        synchronized(devices){
            val existDevice = devices.find { it.device.id == device.id }
            if (existDevice != null){
                existDevice.device = device
            } else devices.add(FarmPoolDevice(device))
        }
    }

    fun acquire(amount: Int = 1, api: Int, userAgent: String): List<FarmDevice> {
        synchronized(devices){
            val availableDevices = devices.filter { it.device.deviceInfo.api == api
                    && !it.isBusy && !it.isBlocked
            }
            if (availableDevices.size < amount)
                throw RuntimeException("Not enough devices with api $api. Available ${availableDevices.size}, required $amount")
            val acquiredDevices = mutableListOf<FarmPoolDevice>()
            for (i in 1 .. amount) {
                acquiredDevices.add(availableDevices.random())
            }
            acquiredDevices.forEach {
                it.userAgent = userAgent
                it.isBusy = true
                it.busyTimestamp = System.currentTimeMillis()
            }
            return acquiredDevices.map { it.device }
        }
    }

    fun release(deviceId: String){
        synchronized(devices){
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    userAgent = null
                    isBusy = false
                    busyTimestamp = 0L
                }
            }
        }
    }

    fun release(deviceIds: List<String>) = deviceIds.forEach { release(it) }

    fun block(deviceId: String, desc: String){
        synchronized(devices){
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    isBlocked = true
                    blockDesc = desc
                }
            }
        }
    }


    fun unblock(deviceId: String){
        synchronized(devices){
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    isBlocked = false
                }
            }
        }
    }
}

