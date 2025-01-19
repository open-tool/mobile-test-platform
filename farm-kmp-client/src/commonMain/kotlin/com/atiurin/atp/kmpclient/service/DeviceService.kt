package com.atiurin.atp.kmpclient.service

import com.atiurin.atp.farmcore.api.response.BaseResponse
import com.atiurin.atp.farmcore.api.response.GetDevicesResponse
import com.atiurin.atp.farmcore.api.response.GetPoolDevicesResponse
import com.atiurin.atp.kmpclient.get
import com.atiurin.atp.kmpclient.post
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.http.path

class DeviceService(private val client: HttpClient) {
    suspend fun getList(): Result<GetPoolDevicesResponse> =
        client.get {
            url { path("device/list") }
        }

    suspend fun getInfo(deviceIds: List<String>): Result<GetPoolDevicesResponse> =
        client.get {
            url {
                path("device/info")
                deviceIds.forEach {
                    parameter("deviceIds", it)
                }
            }
        }

    suspend fun acquire(amount: Int, groupId: String, userAgent: String): Result<GetDevicesResponse> =
        client.get {
            url {
                path("device/acquire")
                parameter("amount", amount)
                parameter("groupId", groupId)
                parameter("userAgent", userAgent)
            }
        }

    suspend fun release(deviceIds: List<String>): Result<BaseResponse> =
        client.post {
            url { path("device/release") }
            deviceIds.forEach {
                parameter("deviceIds", it)
            }
        }

    suspend fun remove(deviceId: String): Result<BaseResponse> =
        client.post {
            url { path("device/remove") }
            parameter("deviceId", deviceId)
        }

    suspend fun block(deviceId: String, desc: String): Result<BaseResponse> =
        client.post {
            url { path("device/block") }
            parameter("deviceId", deviceId)
            parameter("desc", desc)
        }

    suspend fun unblock(deviceId: String): Result<BaseResponse> =
        client.post {
            url { path("device/unblock") }
            parameter("deviceId", deviceId)
        }

    suspend fun create(groupId: String, name: String): Result<GetDevicesResponse> =
        client.post {
            url {
                path("device/create")
                parameter("groupId", groupId)
                parameter("name", name)
            }
        }
}
