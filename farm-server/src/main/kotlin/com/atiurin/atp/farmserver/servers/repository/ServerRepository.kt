package com.atiurin.atp.farmserver.servers.repository

import com.atiurin.atp.farmserver.servers.ServerInfo

interface ServerRepository {
    fun register(ip: String, port: Int)
    fun unregister(ip: String, port: Int)
    fun updateAliveTimestamp(ip: String, port: Int)
    fun getAliveServers(): List<ServerInfo>
}