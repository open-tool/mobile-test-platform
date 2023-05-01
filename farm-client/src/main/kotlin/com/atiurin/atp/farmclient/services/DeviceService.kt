package com.atiurin.atp.farmclient.services

import com.atiurin.atp.farmcore.responses.BaseResponse
import com.atiurin.atp.farmcore.responses.GetDevicesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DeviceService {
    @GET("/device/acquire")
    fun acquire(
        @Query("amount") amount: Int,
        @Query("api") api: Int,
        @Query("userAgent") userAgent: String
    ): Call<GetDevicesResponse>

    @POST("/device/release")
    fun release(
        @Query("deviceIds") deviceIds: List<String>
    ): Call<BaseResponse>

    @POST("/device/remove")
    fun remove(
        @Query("deviceId") deviceId: String
    ): Call<BaseResponse>

    @GET("/device/create")
    fun create(
        @Query("api") api: Int,
        @Query("name") name: String
    ): Call<GetDevicesResponse>
}