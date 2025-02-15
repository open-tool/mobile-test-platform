package com.atiurin.atp.farmcore.api.response

import com.atiurin.atp.farmcore.entity.ServerInfo
import kotlinx.serialization.Serializable

@Serializable
data class GetServersResponse(val servers: List<ServerInfo> = emptyList()) : BaseResponse(){
    override fun toString(): String {
        return "${super.toString()}, servers: $servers"
    }
}
