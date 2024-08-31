package com.atiurin.atp.farmcore.api.response

import com.atiurin.atp.farmcore.api.model.ApiPoolDevice
import kotlinx.serialization.Serializable

@Serializable
data class GetPoolDevicesResponse (val poolDevices: List<ApiPoolDevice> = emptyList()) : BaseResponse(){
    override fun toString(): String {
        return "${super.toString()}, poolDevices: $poolDevices"
    }
}