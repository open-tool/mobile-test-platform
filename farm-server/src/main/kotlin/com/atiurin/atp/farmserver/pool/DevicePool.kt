package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.DeviceInfo
import com.atiurin.atp.farmserver.FarmDevice
import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.provider.DeviceProvider
import com.atiurin.atp.farmserver.provider.DeviceProviderContainer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.lang.RuntimeException

/**
 * Base class to create device pool
 * customize behaviour for different device types
 */
abstract class DevicePool {
    val deviceProvider = DeviceProviderContainer.deviceProvider

    private val devices = mutableListOf<FarmPoolDevice>()

    fun all() = devices.toList()

    fun remove(deviceId: String) {
        synchronized(devices) {
            GlobalScope.async {
                devices.find { it.device.id == deviceId }?.let {
                    deviceProvider.deleteDevice(it.device)
                }
                devices.removeIf {
                    it.device.id == deviceId
                }
            }
        }
    }

    fun join(device: FarmDevice) {
        synchronized(devices) {
            val existDevice = devices.find { it.device.id == device.id }
            if (existDevice != null) {
                existDevice.device = device
            } else devices.add(FarmPoolDevice(device))
        }
    }

    fun create(amount: Int = 1, api: Int){
        synchronized(devices) {
            repeat(amount) {
                devices.add(
                    FarmPoolDevice(
                        deviceProvider.createDevice(
                            DeviceInfo("AutoLaunched api $api", api)
                        )
                    )
                )
            }
        }
    }

    fun acquire(amount: Int = 1, api: Int, userAgent: String): List<FarmDevice> {
        synchronized(devices) {
            val availableDevices = getAvailableDevices(api)
            val deviceToBeAcquiredAmount = if (availableDevices.size < amount) {
                tryToCreateRequiredDevices(amount, availableDevices.size, api)
            } else amount

            val acquiredDevices = mutableListOf<FarmPoolDevice>()
            for (i in 1..deviceToBeAcquiredAmount) {
                if (getAvailableDevices(api).isNotEmpty()){
                    acquiredDevices.add(getAvailableDevices(api).random())
                }
            }
            acquiredDevices.forEach {
                it.userAgent = userAgent
                it.isBusy = true
                it.busyTimestamp = System.currentTimeMillis()
            }
            return acquiredDevices.map { it.device }
        }
    }

    /**
     * Return amount of started devices
     */
    private fun tryToCreateRequiredDevices(amount: Int, availableAmount: Int, api: Int): Int {
        if (devices.size >= ConfigProvider.get().maxDevicesAmount) {
            throw RuntimeException("Couldn't provide device now, farm runs to much devices (${devices.size}). Try again later")
        }
        val availableToRun = ConfigProvider.get().maxDevicesAmount - devices.size
        val requiredToRun = amount - availableAmount
        val runAmount = if (requiredToRun > availableToRun) availableToRun else requiredToRun
        println("availableToRun = $availableToRun, requiredToRun = $requiredToRun, runAmount = $runAmount")
        create(runAmount, api)
        return runAmount
    }

    private fun getAvailableDevices(api: Int) = devices.filter {
        it.device.deviceInfo.api == api && !it.isBusy && !it.isBlocked
    }

    open fun release(deviceId: String) {
        synchronized(devices) {
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

    fun block(deviceId: String, desc: String) {
        synchronized(devices) {
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    isBlocked = true
                    blockDesc = desc
                }
            }
        }
    }


    fun unblock(deviceId: String) {
        synchronized(devices) {
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    isBlocked = false
                }
            }
        }
    }
}

