package com.atiurin.atp.farmserver.servers.repository

import com.atiurin.atp.farmserver.db.Devices
import com.atiurin.atp.farmserver.db.Servers
import com.atiurin.atp.farmserver.servers.ServerInfo
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DBServerRepository : ServerRepository {
    override fun register(ip: String, port: Int) {
        transaction {
            Servers.upsert(Servers.ip, Servers.port){
                it[this.ip] = ip
                it[this.port] = port
                it[aliveTimestamp] = Instant.now().epochSecond
            }
        }
    }

    override fun unregister(ip: String, port: Int) {
        transaction {
            Devices.deleteWhere { Devices.ip eq ip }
            Servers.deleteWhere { Servers.ip eq ip and (Servers.port eq port) }
        }
    }

    override fun updateAliveTimestamp(ip: String, port: Int) {
        transaction {
            Servers.update({ Servers.ip eq ip and (Servers.port eq port) }) {
                it[aliveTimestamp] = Instant.now().epochSecond
            }
        }
    }

    override fun getAliveServers(): List<ServerInfo> {
        val now = Instant.now().epochSecond
        val thirtySecondsAgo = now - 30_000 // 30 seconds in milliseconds

        return transaction {
            Servers.selectAll()
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
}