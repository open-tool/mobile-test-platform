package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.models.DeviceState
import com.atiurin.atp.farmcore.models.DeviceStatus
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.DeviceRepository
import com.atiurin.atp.farmserver.logging.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Base class to create device pool
 * customize behaviour for different device types
 */
@Component
abstract class CachedDevicePool : AbstractDevicePool() {
    abstract val deviceRepository: DeviceRepository

    @Autowired
    lateinit var farmConfig: FarmConfig

    private val devices = mutableListOf<FarmPoolDevice>()

    override fun all(): List<FarmPoolDevice> {
        return devices.toList()
    }

    override fun count(predicate: (FarmPoolDevice) -> Boolean) =
        synchronized(devices) {
            return@synchronized devices.count { predicate(it) }
        }

    override fun remove(deviceId: String) {
        log.info { "Remove device $deviceId" }
        synchronized(devices) {
            val device = devices.find { it.device.id == deviceId }
            devices.removeIf {
                it.device.id == deviceId
            }
            device?.let {
                GlobalScope.async {
                    deviceRepository.deleteDevice(deviceId)
                }
            }
        }
    }

    override fun create(amount: Int, deviceInfo: DeviceInfo, status: DeviceStatus): MutableList<FarmPoolDevice> {
        log.info { "Create $amount new devices $deviceInfo" }
        val newDevices = mutableListOf<FarmPoolDevice>()
        synchronized(devices) {
            repeat(amount) {
                val farmPoolDevice = initDevice(deviceInfo)
                farmPoolDevice.status = status
                newDevices.add(farmPoolDevice)
                devices.add(farmPoolDevice)
                GlobalScope.async {
                    val device = deviceRepository.createDevice(farmPoolDevice.device)
                    devices.find { poolDevice ->
                        poolDevice.device.id == device.id
                    }?.device = device
                }
            }
        }
        return newDevices
    }

    override fun createNeededDevices(): List<FarmPoolDevice> = emptyList()

    override fun acquire(amount: Int, groupId: String, userAgent: String): List<FarmPoolDevice> {
        synchronized(devices) {
            val availableDevices = getAvailableDevicesAndBlock(groupId, amount)
            val deviceToBeAcquired = if (availableDevices.size < amount) {
                val startingDevices = tryToRunLocalDevices(amount, availableDevices.size, groupId)
                availableDevices + startingDevices
            } else availableDevices
            if (deviceToBeAcquired.isEmpty()) {
                throw IllegalStateException("Couldn't run devices cause server runs maximum available devices. Retry later")
            }
            deviceToBeAcquired.forEach {
                it.userAgent = userAgent
                it.status = DeviceStatus.BUSY
                it.busyTimestampSec = Instant.now().epochSecond
            }
            log.info { "Acquired devices by userAgent: $userAgent: $deviceToBeAcquired" }
            return deviceToBeAcquired
        }
    }

    /**
     * Return amount of started devices
     */
    private fun tryToRunLocalDevices(
        requestedAmount: Int,
        availableAmount: Int,
        groupId: String
    ): List<FarmPoolDevice> {
        if (devices.size >= farmConfig.get().maxDevicesAmount) {
            return emptyList()
        }
        val availableToRun = farmConfig.get().maxDevicesAmount - devices.size
        val requiredToRun = requestedAmount - availableAmount
        val runAmount = if (requiredToRun > availableToRun) availableToRun else requiredToRun
        log.info { "tryToRunLocalDevices: availableToRun = $availableToRun, requiredToRun = $requiredToRun, runAmount = $runAmount" }
        return create(runAmount, DeviceInfo("AutoLaunched $groupId", groupId))
    }

    private fun getAvailableDevicesAndBlock(
        groupId: String,
        limitAmount: Int
    ): List<FarmPoolDevice> {
        synchronized(devices) {
            val availableDevices = devices.filter {
                it.device.deviceInfo.groupId == groupId
                        && it.status == DeviceStatus.FREE
                        && it.device.state == DeviceState.READY
            }.take(limitAmount)
            availableDevices.forEach { it.status = DeviceStatus.BUSY }
            return availableDevices
        }
    }

    override fun release(deviceId: String) {
        log.info { "Release device $deviceId" }
        synchronized(devices) {
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    userAgent = null
                    status = DeviceStatus.FREE
                    busyTimestampSec = 0L
                }
            }
        }
    }

    override fun release(deviceIds: List<String>) = deviceIds.forEach { release(it) }

    override fun releaseAll(groupId: String) {
        synchronized(devices) {
            devices.filter { it.device.deviceInfo.groupId == groupId }.forEach { poolDevice ->
                release(poolDevice.device.id)
            }
        }
    }

    override fun removeAll(groupId: String) {
        synchronized(devices) {
            devices.filter { it.device.deviceInfo.groupId == groupId }.forEach { poolDevice ->
                remove(poolDevice.device.id)
            }
        }
    }

    override fun removeDeviceInState(state: DeviceState) {
        synchronized(devices) {
            devices.filter { it.device.state == state }.forEach { poolDevice ->
                remove(poolDevice.device.id)
            }
        }
    }

    override fun block(deviceId: String, desc: String) {
        synchronized(devices) {
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    status = DeviceStatus.BLOCKED
                    this.desc = desc
                }
            }
        }
    }

    override fun unblock(deviceId: String) {
        synchronized(devices) {
            devices.find { it.device.id == deviceId }?.let { poolDevice ->
                poolDevice.apply {
                    status = DeviceStatus.FREE
                }
            }
        }
    }
}

