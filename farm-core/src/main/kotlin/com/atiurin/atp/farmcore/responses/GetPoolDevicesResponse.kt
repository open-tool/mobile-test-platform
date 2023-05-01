package com.atiurin.atp.farmcore.responses

import com.atiurin.atp.farmcore.models.PoolDevice

data class GetPoolDevicesResponse (val poolDevices: List<PoolDevice> = emptyList()) : BaseResponse(){
    override fun toString(): String {
        return "${super.toString()}, poolDevices: $poolDevices"
    }
}