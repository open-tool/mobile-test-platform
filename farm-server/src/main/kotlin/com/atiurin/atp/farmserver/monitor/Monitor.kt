package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.pool.DevicePoolProvider
import com.atiurin.atp.farmserver.pool.FarmPoolDevice
import kotlinx.coroutines.*
import java.time.Instant

suspend fun monitorDevicePool() {
    log.info { "Launch monitorDevicePool" }
    while (true) {
        val keepAliveDevices = ConfigProvider.get().keepAliveDevicesMap
        val devicePool = DevicePoolProvider.devicePool
        val launchedDevices: List<FarmPoolDevice> = devicePool.all()
        keepAliveDevices.entries.forEach {
            val groupId = it.key
            val amount = it.value
            val aliveDevicesAmount =
                launchedDevices.count { farmPoolDevice -> farmPoolDevice.device.deviceInfo.groupId == groupId }
            if (aliveDevicesAmount < amount) {
                devicePool.create(amount - aliveDevicesAmount, groupId)
            }
        }
        delay(2000)
    }
}

suspend fun monitorBusyDevices() {
    log.info { "Launch monitorBusyDevices" }
    while (true) {
        val timeout = ConfigProvider.get().deviceBusyTimeoutSec
        val devicePool = DevicePoolProvider.devicePool
        devicePool.all().filter { it.isBusy }.forEach { poolDevice ->
            val timeoutTime =
                Instant.ofEpochMilli(poolDevice.busyTimestamp).plusSeconds(timeout)
            val now = Instant.now()
            if (now.isAfter(timeoutTime)) {
                log.info { "Release device ${poolDevice.device.id}. timeout = $timeout, timeoutTime = ${timeoutTime.toEpochMilli()}, now = ${now.toEpochMilli()}, busyTimestamp = ${poolDevice.busyTimestamp}" }
                devicePool.release(poolDevice.device.id)
            }
        }
        delay(2000)
    }

}

object Monitor {
    fun startMonitors() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { monitorDevicePool() }
        scope.launch { monitorBusyDevices() }
    }
}
