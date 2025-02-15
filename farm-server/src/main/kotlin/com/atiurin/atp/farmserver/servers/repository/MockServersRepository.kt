package com.atiurin.atp.farmserver.servers.repository

import com.atiurin.atp.farmcore.entity.ServerInfo
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.util.nowSec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MockServersRepository @Autowired constructor(
    private val farmConfig: FarmConfig
) : ServerRepository {
    private val servers = mutableMapOf<String, ServerInfo>()

    override fun register(ip: String, port: Int) {
        servers["$ip:$port"] = ServerInfo(ip, port, nowSec(), true)
    }

    override fun unregister(ip: String, port: Int) {
        servers.remove("$ip:$port")
    }

    override fun updateAliveTimestamp(ip: String, port: Int) {
        servers["$ip:$port"]?.let {
            servers["$ip:$port"] = it.copy(aliveTimestamp = nowSec()) }
        }

    override fun getAliveServers(): List<ServerInfo> {
        return all().filter {
            it.aliveTimestamp >= (nowSec() - farmConfig.get().serverAliveTimeoutSec)
        }
    }

    override fun all(): List<ServerInfo> = servers.values.toList()
}
