package com.atiurin.atp.farmclient

import com.atiurin.atp.farmclient.exceptions.FarmServerException
import com.atiurin.atp.farmclient.okhttpclient.UnsafeOkHttpClientProvider
import com.atiurin.atp.farmclient.services.DeviceService
import com.atiurin.atp.farmcore.models.Device
import com.atiurin.atp.farmcore.responses.BaseResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FarmClient(private val config: FarmClientConfig) {
    private val httpClient = UnsafeOkHttpClientProvider().provide(config)
    private var deviceService: DeviceService
    private val acquiredDevices = mutableListOf<Device>()

    init {
        val retrofit = Retrofit.Builder()
            .client(httpClient)
            .baseUrl(config.farmUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        this.deviceService = retrofit.create(DeviceService::class.java)
    }

    fun acquire(
        amount: Int = 1,
        groupId: String,
    ): List<Device> {
        val body = execute(deviceService.acquire(amount, groupId, config.userAgent)).body()
        return if (body?.devices == null || body.devices.isEmpty()) {
            throw FarmServerException("Couldn't acquire devices from farm, reason: ${body?.message}")
        } else {
            println(body)
            body.devices.apply {
                acquiredDevices.addAll(this)
            }
            acquiredDevices
        }
    }

    fun state(deviceIds: List<String>): List<Device> {
        val body = execute(deviceService.state(deviceIds)).body()
        return if (body?.devices == null || body.devices.isEmpty()) {
            throw FarmServerException("Couldn't get devices state from farm, reason: ${body?.message}")
        } else {
            println(body)
            body.devices
        }
    }

    fun release(deviceIds: List<String>) {
        execute(deviceService.release(deviceIds))
        deviceIds.forEach { deviceId ->
            acquiredDevices.removeIf { it.id == deviceId }
        }
    }

    fun releaseAllCaptured() {
        println("Release all captured devices: ${acquiredDevices.joinToString(",") { it.id }}")
        release(acquiredDevices.map { it.id })
    }

    fun remove(deviceId: String) {
        execute(deviceService.remove(deviceId))
    }

    private fun <T : BaseResponse> execute(call: Call<T>): Response<T> {
        val response = call.execute()
//        println(response)
        if (response.code() == 500) {
            throw FarmServerException("Internal server error: " + response.message())
        }
        val body = response.body()
        if (response.isSuccessful && body != null && body.success) return response
        throw FarmServerException(
            """
                |Unsuccessful response from ${call.request()} 
                |Response code: ${response.code()}                            
                |Response message: $body
            """.trimMargin()
        )
    }
}

