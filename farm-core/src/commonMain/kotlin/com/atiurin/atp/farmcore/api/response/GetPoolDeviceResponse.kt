package com.atiurin.atp.farmcore.api.response

import com.atiurin.atp.farmcore.api.model.ApiPoolDevice
import kotlinx.serialization.Serializable

@Serializable
data class GetPoolDeviceResponse (val poolDevice: ApiPoolDevice) : BaseResponse(){
    override fun toString(): String {
        return "${super.toString()}, poolDevice: $poolDevice"
    }
}