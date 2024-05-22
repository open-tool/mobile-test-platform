package com.atiurin.atp.farmserver.servers.repository

import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmserver.servers.ServerInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class LocalServerRepository @Autowired constructor(
    @Qualifier("serversRepository") val serverRepository: ServerRepository,
    environment: Environment
) {
    val ip = NetUtil.localhostName

    val port = environment.getProperty("local.server.port")?.toInt()
        ?: throw IllegalStateException("Can't get server port")

    fun register() {
        serverRepository.register(ip, port)
    }

    fun unregister() = serverRepository.unregister(ip, port)
    fun updateAliveTimestamp() = serverRepository.updateAliveTimestamp(ip, port)
    fun getAliveServers(): List<ServerInfo> = serverRepository.getAliveServers()
}