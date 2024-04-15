package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.config.FarmConfigImpl
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.pool.DevicePool
import jakarta.annotation.PostConstruct
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
    private val devicePool: DevicePool
) : MonitorInterface {
    init {
        startMonitors()
    }

    final override fun startMonitors() {
        val scope = CoroutineScope(Dispatchers.Default)
        log.info { "Start monitors" }
        scope.launch { monitorDevicePool() }
        scope.launch { monitorBusyDevices() }
    }

    suspend fun monitorDevicePool() {
        log.info { "Launch monitorDevicePool" }
        while (true) {
            val keepAliveDevices = farmConfig.get().keepAliveDevicesMap
            runCatching {
                keepAliveDevices.entries.forEach {
                    val groupId = it.key
                    val amount = it.value
                    val aliveDevicesAmount =
                        devicePool.count { farmPoolDevice -> farmPoolDevice.device.deviceInfo.groupId == groupId }
                    if (aliveDevicesAmount < amount) {
                        devicePool.create(amount - aliveDevicesAmount, groupId)
                    }
                    if (aliveDevicesAmount > amount) { // need to kill extra devices
                        val busyDevicesAmount = devicePool.count { farmPoolDevice ->
                            farmPoolDevice.device.deviceInfo.groupId == groupId && farmPoolDevice.isBusy
                        }
                        val needToRelease = if (busyDevicesAmount < amount) {
                            aliveDevicesAmount - amount
                        } else {
                            aliveDevicesAmount - busyDevicesAmount
                        }
                        if (needToRelease > 0) {
                            val devicesToRelease = devicePool.acquire(
                                amount = needToRelease,
                                groupId = groupId,
                                userAgent = "DevicePoolReconfiguration"
                            )
                            devicePool.release(deviceIds = devicesToRelease.map { device -> device.id })
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
                devicePool.all().filter { it.isBusy }.forEach { poolDevice ->
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
}
