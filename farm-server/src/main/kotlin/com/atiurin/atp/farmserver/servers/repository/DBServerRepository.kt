package com.atiurin.atp.farmserver.servers.repository

import com.atiurin.atp.farmserver.db.Servers
import com.atiurin.atp.farmserver.servers.ServerInfo
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DBServerRepository : ServerRepository {
    override fun register(ip: String, port: Int) {
        Servers.insert {
            it[this.ip] = ip
            it[this.port] = port
            it[aliveTimestamp] = Instant.now().toEpochMilli()
        }
    }

    override fun unregister(ip: String, port: Int) {
        Servers.deleteWhere { Servers.ip eq ip and (Servers.port eq port) }
    }

    override fun updateAliveTimestamp(ip: String, port: Int) {
        Servers.update({ Servers.ip eq ip and (Servers.port eq port) }) {
            it[aliveTimestamp] = Instant.now().toEpochMilli()
        }
    }

    override fun getAliveServers(): List<ServerInfo> {
        val now = Instant.now().toEpochMilli()
        val thirtySecondsAgo = now - 30_000 // 30 seconds in milliseconds

        return Servers.selectAll()
            .filter {
                it[Servers.aliveTimestamp] >= thirtySecondsAgo
            }.map {
                ServerInfo(
                    ip = it[Servers.ip],
                    port = it[Servers.port],
                    aliveTimestamp = it[Servers.aliveTimestamp]
                )
            }
    }
}