package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.provider.DeviceProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
 * Base class to create device pool
 * customize behaviour for different device types
 */
abstract class DevicePool {
    abstract val deviceProvider: DeviceProvider

    private val devices = mutableListOf<FarmPoolDevice>()

    fun all(): List<FarmPoolDevice> {
        return devices.toList()
    }

    fun count(predicate: (FarmPoolDevice) -> Boolean) =
        synchronized(devices) {
            return@synchronized devices.count { predicate(it) }
        }

    fun remove(deviceId: String) {
        log.info { "Remove device $deviceId" }
        synchronized(devices) {
            val device = devices.find { it.device.id == deviceId }
            devices.removeIf {
                it.device.id == deviceId
            }
            device?.let {
                GlobalScope.async {
                    deviceProvider.deleteDevice(it.device)
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

    fun create(amount: Int = 1, groupId: String) {
        log.info { "Create $amount new devices for group $groupId" }
        synchronized(devices) {
            repeat(amount) {
                val newDevice = deviceProvider.createDevice(
                    DeviceInfo("AutoLaunched group '$groupId'", groupId)
                )
                devices.add(FarmPoolDevice(newDevice))
            }
        }
    }

    fun acquire(amount: Int = 1, groupId: String, userAgent: String): List<FarmDevice> {
        synchronized(devices) {
            val availableDevices = getAvailableDevices(groupId)
            val deviceToBeAcquiredAmount = if (availableDevices.size < amount) {
                tryToCreateRequiredDevices(amount, availableDevices.size, groupId)
                getAvailableDevices(groupId).size
            } else amount

            val acquiredDevices = mutableListOf<FarmPoolDevice>()
            for (i in 1..deviceToBeAcquiredAmount) {
                if (getAvailableDevices(groupId).isNotEmpty()) {
                    val device = getAvailableDevices(groupId).random()
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
    private fun tryToCreateRequiredDevices(
        requestedAmount: Int,
        availableAmount: Int,
        groupId: String
    ): Int {
        if (devices.size >= ConfigProvider.get().maxDevicesAmount) {
            throw RuntimeException("Couldn't provide device now, farm runs to much devices (${devices.size}). Try again later")
        }
        val availableToRun = ConfigProvider.get().maxDevicesAmount - devices.size
        val requiredToRun = requestedAmount - availableAmount
        val runAmount = if (requiredToRun > availableToRun) availableToRun else requiredToRun
        log.info { "tryToCreateRequiredDevices: availableToRun = $availableToRun, requiredToRun = $requiredToRun, runAmount = $runAmount" }

        create(runAmount, groupId)
        return runAmount
    }

    private fun getAvailableDevices(groupId: String) = devices.filter {
        it.device.deviceInfo.groupId == groupId && !it.isBusy && !it.isBlocked
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

    open fun release(deviceIds: List<String>) = deviceIds.forEach { release(it) }

    open fun releaseAll(groupId: String) {
        synchronized(devices) {
            devices.filter { it.device.deviceInfo.groupId == groupId }.forEach { poolDevice ->
                remove(poolDevice.device.id)
            }
        }
    }

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

