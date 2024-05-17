package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmcore.models.DeviceState
import com.atiurin.atp.farmcore.models.DeviceStatus
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.pool.DevicePool
import com.atiurin.atp.farmserver.servers.repository.LocalServerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import javax.inject.Singleton

@Singleton
@Component
class Monitor @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val devicePool: DevicePool,
    private val localServerRepository: LocalServerRepository
) : MonitorInterface {
    init {
        startMonitors()
    }

    final override fun startMonitors() {
        val scope = CoroutineScope(Dispatchers.Default)
        log.info { "Start monitors" }
        scope.launch { monitorBusyDevices() }
        scope.launch { monitorServer() }
        scope.launch { monitorLocalServerDevicePool() }
        scope.launch { monitorDevicesNeedToCreate() }
        scope.launch { monitorLocalDeviceNeedToDelete() }
    }

    suspend fun monitorLocalServerDevicePool() {
        log.info { "Launch monitorDevicePool" }
        while (true) {
            val keepAliveDevices = farmConfig.get().keepAliveDevicesMap
            runCatching {
                keepAliveDevices.entries.forEach {
                    val groupId = it.key
                    val amount = it.value
                    val aliveDevicesAmount = devicePool.count { farmPoolDevice ->
                        farmPoolDevice.device.deviceInfo.groupId == groupId
                                && farmPoolDevice.device.containerInfo.ip == localServerRepository.ip
                    }
                    if (aliveDevicesAmount < amount) {
                        devicePool.create(
                            amount - aliveDevicesAmount,
                            DeviceInfo("AutoLaunched $groupId", groupId)
                        )
                    }
                    if (aliveDevicesAmount > amount) { // need to kill extra devices
                        val busyDevicesAmount = devicePool.count { farmPoolDevice ->
                            farmPoolDevice.device.deviceInfo.groupId == groupId
                                    && farmPoolDevice.status == DeviceStatus.BUSY
                                    && farmPoolDevice.device.containerInfo.ip == localServerRepository.ip
                        }
                        val needToRelease = if (busyDevicesAmount < amount) {
                            aliveDevicesAmount - amount
                        } else {
                            aliveDevicesAmount - busyDevicesAmount
                        }
                        if (needToRelease > 0) {
                            val poolDevicesToRelease = devicePool.acquire(
                                amount = needToRelease,
                                groupId = groupId,
                                userAgent = "DevicePoolReconfiguration"
                            )
                            devicePool.release(deviceIds = poolDevicesToRelease.map { poolDevice -> poolDevice.device.id })
                        }
                    }
                }
            }
            delay(farmConfig.get().devicePoolMonitorDelay)
        }
    }

    suspend fun monitorBusyDevices() {
        log.info { "Launch monitorBusyDevices" }
        while (true) {
            val timeout = farmConfig.get().deviceBusyTimeoutSec
            runCatching {
                devicePool.all().filter { it.status == DeviceStatus.BUSY }.forEach { poolDevice ->
                    val timeoutTime =
                        Instant.ofEpochMilli(poolDevice.busyTimestamp).plusSeconds(timeout)
                    val now = Instant.now()
                    if (now.isAfter(timeoutTime)) {
                        log.info { "Release device ${poolDevice.device.id}. timeout = $timeout, timeoutTime = ${timeoutTime.toEpochMilli()}, now = ${now.toEpochMilli()}, busyTimestamp = ${poolDevice.busyTimestamp}" }
                        devicePool.release(poolDevice.device.id)
                    }
                }
            }
            delay(farmConfig.get().busyDevicesMonitorDelay)
        }
    }

    suspend fun monitorServer() {
        while (true) {
            runCatching {
                localServerRepository.updateAliveTimestamp()
            }
            delay(farmConfig.get().serverMonitorDelay)
        }
    }

    suspend fun monitorLocalDeviceNeedToDelete() {
        while (true) {
            runCatching {
                devicePool.removeDeviceInState(DeviceState.NEED_REMOVE)
            }
            delay(farmConfig.get().deviceNeedToDeleteMonitorDelay)
        }
    }

    suspend fun monitorDevicesNeedToCreate() {
        while (true) {
            runCatching {
                devicePool.createNeededDevices()
            }
            delay(farmConfig.get().deviceNeedToCreateMonitorDelay)
        }
    }
}
