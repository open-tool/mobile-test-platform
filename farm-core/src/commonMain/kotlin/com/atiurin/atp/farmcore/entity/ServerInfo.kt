package com.atiurin.atp.farmcore.entity

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(
    val ip: String,
    val port: Int,
    val aliveTimestamp: Long,
    val isAlive: Boolean
)
