package com.atiurin.atp.farmserver.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Servers : Table() {
    val ip: Column<String> = varchar("ip", 50)
    val port: Column<Int> = integer("port")
    val aliveTimestamp: Column<Long> = long("aliveTimestamp").default(0L)
}