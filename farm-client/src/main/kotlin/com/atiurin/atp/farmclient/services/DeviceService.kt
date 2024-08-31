package com.atiurin.atp.farmclient.services

import com.atiurin.atp.farmcore.api.response.BaseResponse
import com.atiurin.atp.farmcore.api.response.GetDevicesResponse
import com.atiurin.atp.farmcore.api.response.GetPoolDevicesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DeviceService {
    @GET("/device/acquire")
    fun acquire(
        @Query("amount") amount: Int,
        @Query("groupId") groupId: String,
        @Query("userAgent") userAgent: String
    ): Call<GetDevicesResponse>

    @GET("/device/info")
    fun info(
        @Query("deviceIds") deviceIds: List<String>
    ): Call<GetPoolDevicesResponse>

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
        @Query("groupId") groupId: String,
        @Query("name") name: String
    ): Call<GetDevicesResponse>
}