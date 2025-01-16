package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.db.DatasourceInitializedEvent
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.pool.DevicePool
import com.atiurin.atp.farmserver.pool.FarmPoolDevice
import com.atiurin.atp.farmserver.servers.repository.LocalServerRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Singleton

@Singleton
@Component
class Monitor @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val devicePool: DevicePool,
    private val localServerRepository: LocalServerRepository,
) : MonitorInterface, ApplicationListener<DatasourceInitializedEvent> {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        log.error { "Coroutine exception caught: ${exception.message}" }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    override fun onApplicationEvent(event: DatasourceInitializedEvent) {
        log.info { "Database initialized, starting monitors" }
        startMonitors()
    }

    final override fun startMonitors() {
        log.info { "Start monitors" }
        scope.launch { monitorBusyDevices() }
        scope.launch { monitorServer() }
        scope.launch { monitorLocalServerDevicePoolToCreateRequired() }
        scope.launch { monitorLocalServerDevicePoolToDeleteRequired() }
        scope.launch { monitorDevicesNeedToCreate() }
        scope.launch { monitorLocalDeviceNeedToDelete() }
        scope.launch { monitorCreatingDevices() }
    }

    suspend fun monitorLocalServerDevicePoolToCreateRequired() {
        val creatingDeviceQueue = LinkedBlockingQueue<FarmPoolDevice>()
        while (true) {
            runCatching {
                farmConfig.get().keepAliveDevicesMap.entries.forEach {
                    val groupId = it.key
                    val amount = it.value
                    val aliveDevicesAmount = devicePool.count { farmPoolDevice ->
                        farmPoolDevice.device.deviceInfo.groupId == groupId
                                && farmPoolDevice.device.containerInfo.ip == localServerRepository.ip
                    }
                    val needToCreateAmount = amount - aliveDevicesAmount
                    val maxAmountAvailableToCreate =
                        farmConfig.get().maxDeviceCreationBatchSize - creatingDeviceQueue.size

                    if (needToCreateAmount > 0 && maxAmountAvailableToCreate > 0) {
                        val devicesToCreate = if (needToCreateAmount > maxAmountAvailableToCreate) {
                            maxAmountAvailableToCreate
                        } else {
                            needToCreateAmount
                        }
                        log.info { "Group $groupId: alive devices = $aliveDevicesAmount, need to create = $needToCreateAmount, max available to create = $maxAmountAvailableToCreate, queue size = ${creatingDeviceQueue.size}" }
                        val devices = devicePool.create(
                            amount = devicesToCreate,
                            deviceInfo = DeviceInfo("AutoLaunched $groupId", groupId),
                            creatingDeviceQueue = creatingDeviceQueue
                        )
                    }
                }
            }.onFailure {
                log.error { "Error in monitorLocalServerDevicePool: ${it.message}" }
            }
            delay(farmConfig.get().devicePoolMonitorDelay)
        }
    }

    suspend fun monitorLocalServerDevicePoolToDeleteRequired() {
        log.info { "Launch monitorDevicePool" }
        val creatingDeviceQueue = LinkedBlockingQueue<FarmPoolDevice>()
        while (true) {
            runCatching {
                farmConfig.get().keepAliveDevicesMap.entries.forEach {
                    val groupId = it.key
                    val amount = it.value
                    val aliveDevicesAmount = devicePool.count { farmPoolDevice ->
                        farmPoolDevice.device.deviceInfo.groupId == groupId
                                && farmPoolDevice.device.containerInfo.ip == localServerRepository.ip
                    }
                    if (aliveDevicesAmount > amount) { // need to kill extra devices
                        val busyDevicesAmount = devicePool.count { farmPoolDevice ->
                            farmPoolDevice.device.deviceInfo.groupId == groupId
                                    && farmPoolDevice.status == DeviceStatus.BUSY
                                    && farmPoolDevice.device.containerInfo.ip == localServerRepository.ip
                        }
                        val needToRemove = if (busyDevicesAmount < amount) {
                            aliveDevicesAmount - amount
                        } else {
                            aliveDevicesAmount - busyDevicesAmount
                        }
                        if (needToRemove > 0) {
                            devicePool.removeDeviceInStatus(needToRemove, groupId, DeviceStatus.FREE)
                        }
                    }
                }
            }.onFailure {
                log.error { "Error in monitorLocalServerDevicePool: ${it.message}" }
            }
            delay(farmConfig.get().devicePoolMonitorDelay * 2)
        }
    }

    suspend fun monitorCreatingDevices() {
        log.info { "Launch monitorCreatingDevices" }
        while (true) {
            val timeout = farmConfig.get().creatingDeviceTimeoutSec
            runCatching {
                devicePool.all().filter { it.device.state == DeviceState.CREATING }.forEach { poolDevice ->
                    val timeoutTime =
                        Instant.ofEpochSecond(poolDevice.device.stateTimestampSec).plusSeconds(timeout)
                    val now = Instant.now()
                    if (now.isAfter(timeoutTime)) {
                        log.info { "Device ${poolDevice.device.id} stacked in creating state. Remove it. Timeout = $timeout, timeoutTime = ${timeoutTime.epochSecond}, now = ${now.epochSecond}" }
                        devicePool.remove(poolDevice.device.id)
                    }
                }
            }
        }
    }

    suspend fun monitorBusyDevices() {
        log.info { "Launch monitorBusyDevices" }
        while (true) {
            val timeout = farmConfig.get().busyDeviceTimeoutSec
            runCatching {
                devicePool.all().filter { it.status == DeviceStatus.BUSY }.forEach { poolDevice ->
                    val timeoutTime =
                        Instant.ofEpochSecond(poolDevice.statusTimestampSec).plusSeconds(timeout)
                    val now = Instant.now()
                    if (now.isAfter(timeoutTime)) {
                        log.info { "Release device ${poolDevice.device.id}. timeout = $timeout, timeoutTime = ${timeoutTime.epochSecond}, now = ${now.epochSecond}, busyTimestamp = ${poolDevice.statusTimestampSec}" }
                        devicePool.release(poolDevice.device.id)
                    }
                }
            }.onFailure {
                log.error { "Error in monitorBusyDevices: ${it.message}" }
            }
            delay(farmConfig.get().busyDevicesMonitorDelay)
        }
    }

    suspend fun monitorServer() {
        while (true) {
            runCatching {
                localServerRepository.updateAliveTimestamp()
            }.onFailure {
                log.error { "Error in monitorServer: ${it.message}" }
            }
            delay(farmConfig.get().serverMonitorDelay)
        }
    }

    suspend fun monitorLocalDeviceNeedToDelete() {
        while (true) {
            runCatching {
                devicePool.removeDeviceInState(state = DeviceState.NEED_REMOVE)
            }.onFailure {
                log.error { "Error in monitorLocalDeviceNeedToDelete: ${it.message}" }
            }
            delay(farmConfig.get().deviceNeedToDeleteMonitorDelay)
        }
    }

    suspend fun monitorDevicesNeedToCreate() {
        while (true) {
            runCatching {
                devicePool.createNeededDevices()
            }.onFailure {
                log.error { "Error in monitorDevicesNeedToCreate: ${it.message}" }
            }
            delay(farmConfig.get().deviceNeedToCreateMonitorDelay)
        }
    }
}
