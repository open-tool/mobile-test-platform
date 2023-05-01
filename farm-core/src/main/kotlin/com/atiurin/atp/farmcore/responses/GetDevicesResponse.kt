package com.atiurin.atp.farmcore.responses

import com.atiurin.atp.farmcore.models.Device

data class GetDevicesResponse(val devices: List<Device> = emptyList()) : BaseResponse(){
    override fun toString(): String {
        return "${super.toString()}, devices: $devices"
    }
}