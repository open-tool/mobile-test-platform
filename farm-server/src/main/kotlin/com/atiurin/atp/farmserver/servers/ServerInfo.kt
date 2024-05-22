package com.atiurin.atp.farmserver.servers

data class ServerInfo(
    val ip: String,
    val port: Int,
    val aliveTimestamp: Long
)
