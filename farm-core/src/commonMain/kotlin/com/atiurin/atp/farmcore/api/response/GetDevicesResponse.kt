package com.atiurin.atp.farmcore.api.response

import com.atiurin.atp.farmcore.api.model.ApiDevice
import kotlinx.serialization.Serializable

@Serializable
data class GetDevicesResponse(val devices: List<ApiDevice> = emptyList()) : BaseResponse(){
    override fun toString(): String {
        return "${super.toString()}, devices: $devices"
    }
}