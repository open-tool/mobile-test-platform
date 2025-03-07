package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmcore.entity.isPreparing
import com.atiurin.atp.farmcore.entity.lowercaseName
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.db.Devices
import com.atiurin.atp.farmserver.device.ContainerInfo
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.DeviceRepository
import com.atiurin.atp.farmserver.device.FarmDevice
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.servers.repository.LocalServerRepository
import com.atiurin.atp.farmserver.util.nowSec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.LinkedBlockingQueue

abstract class DBDevicePool : AbstractDevicePool() {
    abstract val deviceRepository: DeviceRepository

    @Autowired
    lateinit var localServerRepository: LocalServerRepository

    @Autowired
    lateinit var farmConfig: FarmConfig

    private val localDevices: Query
        get() = Devices.selectAll().where {
            Devices.ip eq localServerRepository.ip
        }

    private val totalServerDevices: Int
        get() = executeTransaction {
            localDevices.count().toInt()
        }

    override fun all(): List<FarmPoolDevice> = executeTransaction {
        Devices.selectAll().mapToFarmPoolDevice()
    }

    override fun count(predicate: (FarmPoolDevice) -> Boolean) = all().count { predicate(it) }

    override fun remove(deviceId: String) {
        log.info { "Remove device $deviceId" }
        executeTransaction {
            Devices.update({
                Devices.uid eq deviceId
            }) {
                it[state] = DeviceState.NEED_REMOVE.lowercaseName()
                it[stateTimestampSec] = nowSec()
                it[desc] = "Device is planned to be deleted"
            }
            val isLocalDevice = Devices.selectAll().where {
                Devices.uid eq deviceId and (Devices.ip eq localServerRepository.ip)
            }.count() > 0
            if (isLocalDevice) {
                Devices.deleteWhere {
                    uid eq deviceId
                }
                CoroutineScope(Dispatchers.Default).launch {
                    deviceRepository.deleteDevice(deviceId)
                }
                return@executeTransaction
            }
        }
    }

    override fun removeAll(groupId: String) {
        executeTransaction {
            Devices.selectAll().where {
                Devices.groupId eq groupId
            }.forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                Devices.update({ Devices.uid eq result[Devices.uid] }) {
                    it[status] = DeviceStatus.BLOCKED.lowercaseName()
                    it[statusTimestampSec] = nowSec()
                    it[stateTimestampSec] = nowSec()
                    it[state] = DeviceState.NEED_REMOVE.lowercaseName()
                    it[desc] = "Device is planned to be deleted as part of group $groupId deletion"
                }
            }
        }
    }

    override fun removeDeviceInStatus(amount: Int, groupId: String, status: DeviceStatus) {
        removeDevicesWhere(amount) {
            Devices.status eq status.lowercaseName() and
                    (Devices.ip eq localServerRepository.ip) and
                    (Devices.groupId eq groupId)
        }
    }

    override fun removeDeviceInState(amount: Int, state: DeviceState) {
        removeDevicesWhere(amount) {
            Devices.state eq state.lowercaseName() and (Devices.ip eq localServerRepository.ip)
        }
    }

    private fun removeDevicesWhere(amount: Int, predicate: SqlExpressionBuilder.() -> Op<Boolean>) {
        val deviceIdsToRemove = mutableListOf<String>()
        executeTransaction {
            val selectedDevices =
                Devices.selectAll().where(predicate).forUpdate(ForUpdateOption.ForUpdate)
            if (amount > 0) {
                selectedDevices.limit(amount)
            }
            selectedDevices.forEach { row ->
                deviceIdsToRemove.add(row[Devices.uid])
                Devices.update({ Devices.uid eq row[Devices.uid] }) {
                    it[state] = DeviceState.REMOVING.lowercaseName()
                    it[stateTimestampSec] = nowSec()
                }
            }
        }
        deviceIdsToRemove.forEach { deviceId ->
            remove(deviceId)
        }
    }

    private fun insertOrUpdateDeviceInDB(farmPoolDevice: FarmPoolDevice) {
        executeTransaction {
            Devices.upsert(Devices.uid) {
                it[uid] = farmPoolDevice.device.id
                it[name] = farmPoolDevice.device.deviceInfo.name
                it[groupId] = farmPoolDevice.device.deviceInfo.groupId
                it[ip] = farmPoolDevice.device.containerInfo.ip
                it[adbPort] = farmPoolDevice.device.containerInfo.adbPort
                it[grpcPort] = farmPoolDevice.device.containerInfo.gRpcPort
                it[dockerImage] = farmPoolDevice.device.containerInfo.dockerImage
                it[desc] = farmPoolDevice.desc
                it[userAgent] = farmPoolDevice.userAgent
                it[status] = farmPoolDevice.status.lowercaseName()
                it[statusTimestampSec] = farmPoolDevice.statusTimestampSec
                it[state] = farmPoolDevice.device.state.lowercaseName()
                it[stateTimestampSec] = farmPoolDevice.device.stateTimestampSec
                it[lastPingTimestampSec] = farmPoolDevice.lastPingTimestampSec
            }
        }

    }

    override fun create(
        amount: Int,
        deviceInfo: DeviceInfo,
        status: DeviceStatus,
        creatingDeviceQueue: LinkedBlockingQueue<FarmPoolDevice>,
    ): List<FarmPoolDevice> {
        log.info { "Create $amount new devices for group $deviceInfo.groupId" }
        val newDevices = mutableListOf<FarmPoolDevice>()
        runBlocking {
            repeat(amount) {
                val farmPoolDevice = initDevice(deviceInfo)
                insertOrUpdateDeviceInDB(farmPoolDevice)
                newDevices.add(farmPoolDevice)
                creatingDeviceQueue.add(farmPoolDevice)
            }
        }
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            while (creatingDeviceQueue.isNotEmpty()) {
                val farmPoolDevice = creatingDeviceQueue.poll()
                launch {
                    val device = deviceRepository.createDevice(farmPoolDevice.device)
                    insertOrUpdateDeviceInDB(
                        farmPoolDevice.copy(
                            device = device,
                            status = status,
                            statusTimestampSec = if (status != farmPoolDevice.status) nowSec() else farmPoolDevice.statusTimestampSec,
                            desc = ""
                        )
                    )
                }
            }
        }
        return newDevices
    }

    override fun createNeededDevices(): List<FarmPoolDevice> {
        val amount = farmConfig.get().maxDevicesAmount - totalServerDevices
        val devices = executeTransaction {
            Devices.selectAll().where {
                Devices.state eq DeviceState.NEED_CREATE.lowercaseName()
            }.limit(amount).forUpdate(ForUpdateOption.ForUpdate).mapToFarmPoolDevice().apply {
                forEach { creatingDevice ->
                    Devices.update({ Devices.uid eq creatingDevice.device.id }) {
                        it[state] = DeviceState.CREATING.lowercaseName()
                        it[stateTimestampSec] = nowSec()
                    }
                }
            }
        }
        devices.forEach { farmPoolDevice ->
            CoroutineScope(Dispatchers.Default).launch {
                val device = deviceRepository.createDevice(farmPoolDevice.device)
                insertOrUpdateDeviceInDB(
                    farmPoolDevice.copy(
                        device = device,
                        status = DeviceStatus.BUSY,
                        statusTimestampSec = nowSec(),
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
                    totalServerDevices, amount, availableDevices.size, groupId
                )
                availableDevices + startingLocalDevices
            } else availableDevices
            val idsForUpdate = deviceToBeAcquired.map { it.device.id }
            Devices.update({ Devices.uid inList idsForUpdate }) {
                it[Devices.userAgent] = userAgent
                it[status] = DeviceStatus.BUSY.lowercaseName()
                it[statusTimestampSec] = nowSec()
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
        groupId: String,
    ): List<FarmPoolDevice> {
        if (allDevicesOnLocalServerAmount >= farmConfig.get().maxDevicesAmount) {
            return emptyList()
        }
        val availableToRun = farmConfig.get().maxDevicesAmount - allDevicesOnLocalServerAmount
        val requiredToRun = requestedAmount - availableAmount
        val runAmount = if (requiredToRun > availableToRun) availableToRun else requiredToRun
        log.info { "tryToRunLocalDevices: availableToRun = $availableToRun, requiredToRun = $requiredToRun, runAmount = $runAmount" }
        return create(runAmount, DeviceInfo("AutoLaunched $groupId", groupId), DeviceStatus.BUSY)
    }

    private fun addNeedToCreateDevices(
        amount: Int, groupId: String, userAgent: String,
    ): List<FarmPoolDevice> {
        val newDevices = mutableListOf<FarmPoolDevice>()
        //if we have max devices amount on local server we can't create more
        //create it in DB and assume that other servers will create it by monitoring
        repeat(amount) {
            val farmPoolDevice = initDevice(groupId, "").apply {
                device.state = DeviceState.NEED_CREATE
                device.stateTimestampSec = nowSec()
                status = DeviceStatus.BUSY
                statusTimestampSec = nowSec()
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
        groupId: String, limitAmount: Int,
    ): List<FarmPoolDevice> {
        val aliveServerIps = localServerRepository.getAliveServers().map { it.ip }
        return Devices.selectAll().where {
            (Devices.groupId eq groupId)
                .and(Devices.status eq DeviceStatus.FREE.lowercaseName())
                .and(Devices.state eq DeviceState.READY.lowercaseName())
                .and(Devices.ip inList aliveServerIps)
        }.limit(limitAmount).forUpdate().mapToFarmPoolDevice()
    }

    override fun release(deviceId: String) {
        log.info { "Release device $deviceId" }
        executeTransaction {
            Devices.selectAll().where { Devices.uid eq deviceId }
                .forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                    Devices.update({ Devices.uid eq result[Devices.uid] }) {
                        it[userAgent] = null
                        it[status] = DeviceStatus.FREE.lowercaseName()
                        it[statusTimestampSec] = nowSec()
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
                        it[status] = DeviceStatus.FREE.lowercaseName()
                        it[statusTimestampSec] = nowSec()
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
                        it[status] = DeviceStatus.FREE.lowercaseName()
                        it[statusTimestampSec] = nowSec()
                    }
                }
        }
    }

    override fun block(deviceId: String, desc: String) {
        executeTransaction {
            Devices.selectAll().where { Devices.uid eq deviceId }
                .forUpdate(ForUpdateOption.ForUpdate).forEach { result ->
                    Devices.update({ Devices.uid eq result[Devices.uid] }) {
                        it[status] = DeviceStatus.BLOCKED.lowercaseName()
                        it[statusTimestampSec] = nowSec()
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
                        it[status] = DeviceStatus.FREE.lowercaseName()
                        it[statusTimestampSec] = nowSec()
                        it[desc] = ""
                    }
                }
        }
    }

    override fun isAlive(deviceId: String): FarmPoolDevice {
        val getDeviceInfo = {
            Devices.selectAll().where { Devices.uid eq deviceId }
                .forUpdate(ForUpdateOption.ForUpdate).mapToFarmPoolDevice().first()
        }
        val poolDevice = getDeviceInfo()
        //TODO: change to client request to another server
        val isLocalDevice = Devices.selectAll().where {
            Devices.uid eq deviceId and (Devices.ip eq localServerRepository.ip)
        }.count() > 0
        if (!isLocalDevice) return poolDevice
        if (poolDevice.device.state.isPreparing()) return poolDevice
        val isAlive = deviceRepository.isDeviceAlive(deviceId)
        return if (!isAlive) {
            //TODO: move to updateReturning statement
            Devices.update(
                where = { Devices.uid eq deviceId }
            ) {
                it[state] = DeviceState.BROKEN.lowercaseName()
                it[stateTimestampSec] = nowSec()
            }
            getDeviceInfo()
        } else {
            poolDevice
        }
    }

    private fun <R> executeTransaction(block: () -> R): R {
        return transaction {
            val result = block()
            commit()
            result
        }
    }

    private fun Query.mapToFarmPoolDevice(): List<FarmPoolDevice> {
        return this.map {
            val farmDevice = FarmDevice(
                id = it[Devices.uid],
                state = DeviceState.valueOf(it[Devices.state].uppercase()),
                stateTimestampSec = it[Devices.stateTimestampSec],
                deviceInfo = DeviceInfo(
                    name = it[Devices.name], groupId = it[Devices.groupId]
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
                status = DeviceStatus.valueOf(it[Devices.status].uppercase()),
                desc = it[Devices.desc],
                userAgent = it[Devices.userAgent],
                statusTimestampSec = it[Devices.statusTimestampSec],
                lastPingTimestampSec = it[Devices.lastPingTimestampSec]
            )
        }
    }

}

