package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.models.DeviceState
import com.atiurin.atp.farmcore.models.DeviceStatus
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.db.DataSource
import com.atiurin.atp.farmserver.db.Devices
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.DeviceRepository
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.servers.repository.LocalServerRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

abstract class DBDevicePool(val dataSource: DataSource) : AbstractDevicePool() {
    abstract val deviceRepository: DeviceRepository
    private val database: Database by lazy {
        dataSource.db ?: throw IllegalStateException("Database is not initialized")
    }

    @Autowired
    lateinit var localServerRepository: LocalServerRepository

    @Autowired
    lateinit var farmConfig: FarmConfig

    private val totalServerDevices: Int
        get() = Devices.selectAll().where {
            Devices.ip eq localServerRepository.ip
        }.count().toInt()

    override fun all(): List<FarmPoolDevice> {
        return Devices.selectAll().mapToFarmPoolDevice()
    }

    override fun count(predicate: (FarmPoolDevice) -> Boolean) =
        all().count { predicate(it) }


    override fun remove(deviceId: String) {
        log.info { "Remove device $deviceId" }
        val localDevice = deviceRepository.getDevices().find { it.id == deviceId }
        if (localDevice != null) {
            Devices.deleteWhere {
                uid eq deviceId
            }
            GlobalScope.async {
                deviceRepository.deleteDevice(deviceId)
            }
            return
        } else {
            Devices.update({
                Devices.uid eq deviceId
            }) {
                it[state] = DeviceState.NEED_REMOVE.intValue
                it[desc] = "Device is planned to be deleted"
            }
        }
    }

    override fun removeAll(groupId: String) {
        executeTransaction {
            Devices.selectAll().where {
                Devices.groupId eq groupId
            }.forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                Devices.update({ Devices.uid eq result[Devices.uid] }) {
                    it[status] = DeviceStatus.BLOCKED.intValue
                    it[state] = DeviceState.NEED_REMOVE.intValue
                    it[desc] = "Device is planned to be deleted as part of group $groupId deletion"
                }
            }
        }
    }


    override fun removeDeviceInState(state: DeviceState) {
        val deviceIdsToRemove = mutableListOf<String>()
        executeTransaction {
            Devices.selectAll().where {
                Devices.state eq state.intValue and (Devices.ip eq localServerRepository.ip)
            }.forUpdate(ForUpdateOption.ForUpdate).forEach { row ->
                deviceIdsToRemove.add(row[Devices.uid])
                Devices.update({ Devices.uid eq row[Devices.uid] }) {
                    it[Devices.state] = DeviceState.REMOVING.intValue
                }
            }
        }
        deviceIdsToRemove.forEach { deviceId ->
            remove(deviceId)
        }
    }

    fun insertOrUpdateDeviceInDB(farmPoolDevice: FarmPoolDevice) {
        Devices.upsert(Devices.uid) {
            it[uid] = farmPoolDevice.device.id
            it[name] = farmPoolDevice.device.deviceInfo.name
            it[groupId] = farmPoolDevice.device.deviceInfo.groupId
            it[ip] = farmPoolDevice.device.containerInfo.ip
            it[adbPort] = farmPoolDevice.device.containerInfo.adbPort
            it[grpcPort] = farmPoolDevice.device.containerInfo.gRpcPort
            it[dockerImage] = farmPoolDevice.device.containerInfo.dockerImage
            it[state] = farmPoolDevice.device.state.intValue
            it[status] = farmPoolDevice.status.intValue
            it[desc] = farmPoolDevice.desc
            it[userAgent] = farmPoolDevice.userAgent
            it[busyTimestamp] = farmPoolDevice.busyTimestamp
            it[lastPingTimestamp] = farmPoolDevice.lastPingTimestamp
        }
    }

    override fun create(amount: Int, deviceInfo: DeviceInfo): List<FarmPoolDevice> {
        log.info { "Create $amount new devices for group $deviceInfo.groupId" }
        val newDevices = mutableListOf<FarmPoolDevice>()
        repeat(amount) {
            val farmPoolDevice = initDevice(deviceInfo)
            insertOrUpdateDeviceInDB(farmPoolDevice)
            newDevices.add(farmPoolDevice)
            GlobalScope.async {
                val device = deviceRepository.createDevice(farmPoolDevice.device)
                insertOrUpdateDeviceInDB(
                    farmPoolDevice.copy(
                        device = device,
                        status = DeviceStatus.BUSY
                    )
                )
            }
        }
        return newDevices
    }

    override fun createNeededDevices(): List<FarmPoolDevice> {
        val amount = farmConfig.get().maxDevicesAmount - totalServerDevices
        val devices = executeTransaction {
            Devices.selectAll().where {
                Devices.state eq DeviceState.NEED_CREATE.intValue
            }.limit(amount).forUpdate(ForUpdateOption.ForUpdate).mapToFarmPoolDevice().apply {
                forEach { creatingDevice ->
                    Devices.update({Devices.uid eq creatingDevice.device.id }) {
                        it[state] = DeviceState.CREATING.intValue
                    }
                }
            }
        }
        devices.forEach { farmPoolDevice ->
            GlobalScope.async {
                val device = deviceRepository.createDevice(farmPoolDevice.device)
                insertOrUpdateDeviceInDB(
                    farmPoolDevice.copy(
                        device = device,
                        status = DeviceStatus.BUSY
                    )
                )
            }
        }
        return devices
    }

    override fun acquire(amount: Int, groupId: String, userAgent: String): List<FarmPoolDevice> {
        val devices = executeTransaction {
            val availableDevices = getAvailableDevicesAndBlock(groupId, amount)
            val deviceToBeAcquired = if (availableDevices.size < amount) {
                val startingLocalDevices = tryToRunLocalDevices(
                    totalServerDevices,
                    amount,
                    availableDevices.size,
                    groupId
                )
                availableDevices + startingLocalDevices
            } else availableDevices
            val idsForUpdate = deviceToBeAcquired.map { it.device.id }
            Devices.update({ Devices.uid inList idsForUpdate }) {
                it[Devices.userAgent] = userAgent
                it[status] = DeviceStatus.BUSY.intValue
                it[busyTimestamp] = Instant.now().toEpochMilli()
            }
            deviceToBeAcquired
        }
        val needToCreate = if (devices.size < amount) {
            addNeedToCreateDevices(amount - devices.size, groupId, userAgent)
        } else emptyList()
        log.info { "Acquired devices by userAgent: $userAgent: $devices,\n needToCreate: $needToCreate" }
        return devices + needToCreate
    }

    private fun tryToRunLocalDevices(
        allDevicesOnLocalServerAmount: Int,
        requestedAmount: Int,
        availableAmount: Int,
        groupId: String
    ): List<FarmPoolDevice> {
        if (allDevicesOnLocalServerAmount >= farmConfig.get().maxDevicesAmount) {
            return emptyList()
        }
        val availableToRun = farmConfig.get().maxDevicesAmount - allDevicesOnLocalServerAmount
        val requiredToRun = requestedAmount - availableAmount
        val runAmount = if (requiredToRun > availableToRun) availableToRun else requiredToRun
        log.info { "tryToRunLocalDevices: availableToRun = $availableToRun, requiredToRun = $requiredToRun, runAmount = $runAmount" }
        return create(runAmount, DeviceInfo("AutoLaunched $groupId",groupId))
    }

    private fun addNeedToCreateDevices(amount: Int, groupId: String, userAgent: String): List<FarmPoolDevice> {
        val newDevices = mutableListOf<FarmPoolDevice>()
        //if we have max devices amount on local server we can't create more
        //create it in DB and assume that other servers will create it by monitoring
        repeat(amount) {
            val farmPoolDevice = initDevice(groupId).apply {
                device.state = DeviceState.NEED_CREATE
                this.userAgent = userAgent
            }
            insertOrUpdateDeviceInDB(farmPoolDevice)
            newDevices.add(farmPoolDevice)
        }
        return newDevices
    }

    /**
     * Return list of available devices for specified group
     * should be called in transaction scope
     */
    private fun getAvailableDevicesAndBlock(
        groupId: String,
        limitAmount: Int
    ): List<FarmPoolDevice> {
        return Devices.selectAll().where {
            (Devices.groupId eq groupId)
                .and(Devices.status eq DeviceStatus.FREE.intValue)
                .and(Devices.state eq DeviceState.READY.intValue)
        }.limit(limitAmount).forUpdate().mapToFarmPoolDevice()
    }

    override fun release(deviceId: String) {
        log.info { "Release device $deviceId" }
        executeTransaction {
            Devices.selectAll().where { Devices.uid eq deviceId }
                .forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                    Devices.update({ Devices.uid eq result[Devices.uid] }) {
                        it[userAgent] = null
                        it[status] = DeviceStatus.FREE.intValue
                        it[busyTimestamp] = 0L
                    }
                }
        }
    }

    override fun release(deviceIds: List<String>) {
        executeTransaction {
            Devices.selectAll().where { Devices.uid inList deviceIds }
                .forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                    Devices.update({ Devices.uid eq result[Devices.uid] }) {
                        it[userAgent] = null
                        it[status] = DeviceStatus.FREE.intValue
                        it[busyTimestamp] = 0L
                    }
                }
        }
    }

    override fun releaseAll(groupId: String) {
        executeTransaction {
            Devices.selectAll().where { Devices.groupId eq groupId }
                .forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                    Devices.update({ Devices.uid eq result[Devices.uid] }) {
                        it[userAgent] = null
                        it[status] = DeviceStatus.FREE.intValue
                        it[busyTimestamp] = 0L
                    }
                }
        }
    }


    override fun block(deviceId: String, desc: String) {
        executeTransaction {
            Devices.selectAll().where { Devices.uid eq deviceId }
                .forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                    Devices.update({ Devices.uid eq result[Devices.uid] }) {
                        it[status] = DeviceStatus.BLOCKED.intValue
                        it[Devices.desc] = desc
                    }
                }
        }
    }

    override fun unblock(deviceId: String) {
        executeTransaction {
            Devices.selectAll().where { Devices.uid eq deviceId }
                .forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                    Devices.update({ Devices.uid eq result[Devices.uid] }) {
                        it[status] = DeviceStatus.FREE.intValue
                        it[desc] = ""
                    }
                }
        }
    }

    private fun <R> executeTransaction(block: () -> R): R {
        return transaction(database) {
            val result = block()
            commit()
            result
        }
    }

    private fun Query.mapToFarmPoolDevice(): List<FarmPoolDevice> {
        return this.map {
            val farmDevice = FarmDevice(
                id = it[Devices.uid],
                state = DeviceState.fromInt(it[Devices.state]),
                deviceInfo = DeviceInfo(
                    name = it[Devices.name],
                    groupId = it[Devices.groupId]
                ),
                containerInfo = ContainerInfo(
                    ip = it[Devices.ip],
                    adbPort = it[Devices.adbPort],
                    gRpcPort = it[Devices.grpcPort],
                    dockerImage = it[Devices.dockerImage]
                ),
                container = null
            )
            FarmPoolDevice(
                device = farmDevice,
                status = DeviceStatus.fromInt(it[Devices.status]),
                desc = it[Devices.desc],
                userAgent = it[Devices.userAgent],
                busyTimestamp = it[Devices.busyTimestamp],
                lastPingTimestamp = it[Devices.lastPingTimestamp]
            )
        }
    }

}

