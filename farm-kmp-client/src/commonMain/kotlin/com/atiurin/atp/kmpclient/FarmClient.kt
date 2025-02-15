package com.atiurin.atp.kmpclient

import com.atiurin.atp.farmcore.api.model.toDevices
import com.atiurin.atp.farmcore.api.model.toPoolDevices
import com.atiurin.atp.farmcore.api.response.BaseResponse
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.entity.PoolDevice
import com.atiurin.atp.farmcore.entity.ServerInfo
import com.atiurin.atp.kmpclient.service.DeviceService
import com.atiurin.atp.kmpclient.service.ServerService
import java.net.ConnectException

class FarmClient(
    private val config: FarmClientConfig,
    private val doOnFailure: (String) -> Unit,
) {
    private var httpClient = HttpClient(config)
    private var deviceService: DeviceService = DeviceService(httpClient)
    private var serverService: ServerService = ServerService(httpClient)
    private val acquiredDevices = mutableListOf<Device>()

    fun getUserAgent() = config.userAgent

    suspend fun acquire(
        amount: Int = 1,
        groupId: String,
    ): List<Device> {
        return sendRequest(
            defaultValue = emptyList(),
            requester = { deviceService.acquire(amount, groupId, config.userAgent) },
            onSuccess = { resp ->
                val devices = resp.devices.toDevices()
                acquiredDevices.addAll(devices)
                devices
            }
        )
    }

    suspend fun list(): List<PoolDevice> {
        return sendRequest(
            defaultValue = emptyList(),
            requester = { deviceService.getList() },
            onSuccess = { resp ->
                resp.poolDevices.toPoolDevices()
            }
        )
    }

    suspend fun info(deviceIds: List<String>): List<PoolDevice> {
        return sendRequest(
            defaultValue = emptyList(),
            requester = { deviceService.getInfo(deviceIds) },
            onSuccess = { resp ->
                resp.poolDevices.toPoolDevices()
            }
        )
    }

    suspend fun release(deviceIds: List<String>) {
        return sendRequest(
            defaultValue = Unit,
            requester = { deviceService.release(deviceIds) },
            onSuccess = {
                deviceIds.forEach { deviceId ->
                    acquiredDevices.removeIf { it.id == deviceId }
                }
            }
        )
    }

    suspend fun releaseAllCaptured() {
        println("Release all captured devices: ${acquiredDevices.joinToString(",") { it.id }}")
        release(acquiredDevices.map { it.id })
    }

    suspend fun remove(deviceId: String) {
        return sendRequest(
            defaultValue = Unit,
            requester = { deviceService.remove(deviceId) },
            onSuccess = { }
        )
    }

    suspend fun block(deviceId: String, desc: String) {
        return sendRequest(
            defaultValue = Unit,
            requester = { deviceService.block(deviceId, desc) },
            onSuccess = {}
        )
    }

    suspend fun unblock(deviceId: String) {
        return sendRequest(
            defaultValue = Unit,
            requester = { deviceService.unblock(deviceId) },
            onSuccess = {}
        )
    }

    suspend fun serverList(): List<ServerInfo> {
        return sendRequest(
            defaultValue = emptyList(),
            requester = { serverService.getList() },
            onSuccess = { resp ->
                resp.servers
            }
        )
    }

    private suspend fun <T : BaseResponse, R> sendRequest(
        defaultValue: R,
        requester: suspend () -> Result<T>,
        onSuccess: (T) -> R,
    ): R {
        var result: R = defaultValue
        requester().onFailure { ex ->
            doOnFailure(ex.message ?: ex.toString())
            if (ex is ConnectException) {
                httpClient = HttpClient(config)
                result = sendRequest(defaultValue, requester, onSuccess)
            }
        }.onSuccess { resp ->
            if (!resp.success) {
                doOnFailure(resp.message)
            } else {
                result = onSuccess(resp)
            }
        }
        return result
    }
}

