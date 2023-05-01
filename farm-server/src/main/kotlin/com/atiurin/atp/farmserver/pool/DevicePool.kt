package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.DeviceInfo
import com.atiurin.atp.farmserver.FarmDevice
import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.logging.log
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
    abstract val deviceProvider : DeviceProvider

    private val devices = mutableListOf<FarmPoolDevice>()

    fun all() = devices.toList()

    fun remove(deviceId: String) {
        log.info { "Remove device $deviceId" }
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

    fun create(amount: Int = 1, api: Int) {
        log.info { "Create $amount new devices with api $api" }
        synchronized(devices) {
            repeat(amount) {
                val newDevice = deviceProvider.createDevice(
                    DeviceInfo("AutoLaunched api $api", api)
                )
                devices.add(FarmPoolDevice(newDevice))
            }
        }
    }

    fun acquire(amount: Int = 1, api: Int, userAgent: String): List<FarmDevice> {
        synchronized(devices) {
            val availableDevices = getAvailableDevices(api)
            val deviceToBeAcquiredAmount = if (availableDevices.size < amount) {
                tryToCreateRequiredDevices(amount, availableDevices.size, api)
                getAvailableDevices(api).size
            } else amount

            val acquiredDevices = mutableListOf<FarmPoolDevice>()
            for (i in 1..deviceToBeAcquiredAmount) {
                if (getAvailableDevices(api).isNotEmpty()) {
                    val device = getAvailableDevices(api).random()
                    device.apply {
                        this.userAgent = userAgent
                        this.isBusy = true
                        this.busyTimestamp = System.currentTimeMillis()
                    }
                    acquiredDevices.add(device)
                }
            }
            val acquired = acquiredDevices.map { it.device }
            log.info { "Acquired devices by userAgent: $userAgent: $acquired" }
            return acquired
        }
    }

    /**
     * Return amount of started devices
     */
    private fun tryToCreateRequiredDevices(requestedAmount: Int, availableAmount: Int, api: Int): Int {
        if (devices.size >= ConfigProvider.get().maxDevicesAmount) {
            throw RuntimeException("Couldn't provide device now, farm runs to much devices (${devices.size}). Try again later")
        }
        val availableToRun = ConfigProvider.get().maxDevicesAmount - devices.size
        val requiredToRun = requestedAmount - availableAmount
        val runAmount = if (requiredToRun > availableToRun) availableToRun else requiredToRun
        log.info { "tryToCreateRequiredDevices: availableToRun = $availableToRun, requiredToRun = $requiredToRun, runAmount = $runAmount" }

        create(runAmount, api)
        return runAmount
    }

    private fun getAvailableDevices(api: Int) = devices.filter {
        it.device.deviceInfo.api == api && !it.isBusy && !it.isBlocked
    }

    open fun release(deviceId: String) {
        log.info { "Release device $deviceId" }
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

