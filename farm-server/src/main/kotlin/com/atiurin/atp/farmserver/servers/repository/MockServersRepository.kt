package com.atiurin.atp.farmserver.servers.repository

import com.atiurin.atp.farmserver.servers.ServerInfo
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MockServersRepository : ServerRepository {
    private val servers = mutableMapOf<String, ServerInfo>()

    override fun register(ip: String, port: Int) {
        servers["$ip:$port"] = ServerInfo(ip, port, Instant.now().toEpochMilli())
    }

    override fun unregister(ip: String, port: Int) {
        servers.remove("$ip:$port")
    }

    override fun updateAliveTimestamp(ip: String, port: Int) {
        servers["$ip:$port"]?.let {
            servers["$ip:$port"] = it.copy(aliveTimestamp = Instant.now().toEpochMilli()) }
        }

    override fun getAliveServers(): List<ServerInfo> {
        val now = Instant.now().toEpochMilli()
        val thirtySecondsAgo = now - 30_000 // 30 seconds in milliseconds

        return servers.values.toList().filter {
            it.aliveTimestamp >= thirtySecondsAgo
        }
    }
}
