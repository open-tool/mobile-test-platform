package com.atiurin.atp.farmserver.servers.repository

import com.atiurin.atp.farmserver.servers.ServerInfo
import com.atiurin.atp.farmserver.util.nowSec
import org.springframework.stereotype.Component

@Component
class MockServersRepository : ServerRepository {
    private val servers = mutableMapOf<String, ServerInfo>()

    override fun register(ip: String, port: Int) {
        servers["$ip:$port"] = ServerInfo(ip, port, nowSec())
    }

    override fun unregister(ip: String, port: Int) {
        servers.remove("$ip:$port")
    }

    override fun updateAliveTimestamp(ip: String, port: Int) {
        servers["$ip:$port"]?.let {
            servers["$ip:$port"] = it.copy(aliveTimestamp = nowSec()) }
        }

    override fun getAliveServers(): List<ServerInfo> {
        val now = nowSec()
        val thirtySecondsAgo = now - 30_000 // 30 seconds in milliseconds

        return servers.values.toList().filter {
            it.aliveTimestamp >= thirtySecondsAgo
        }
    }
}
