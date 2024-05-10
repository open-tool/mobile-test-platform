package com.atiurin.atp.farmserver.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Devices : IntIdTable() {
    val deviceId: Column<String> = varchar("deviceId", 50)
    val name: Column<String> = varchar("name", 50)
    val groupId: Column<String> = varchar("groupId", 50)
    val ip: Column<String> = varchar("ip", 50)
    val adbPort: Column<Int> = integer("adbPort")
    val grpcPort: Column<Int?> = integer("grpcPort").nullable()
    val dockerImage: Column<String> = varchar("dockerImage", length = 200)
    val userAgent: Column<String?> = varchar("userAgent", length = 1000).nullable()
    val isBusy: Column<Boolean> = bool("isBusy").default(false)
    val isBlocked: Column<Boolean> = bool("isBlocked").default(false)
    val blockDesc: Column<String?> = varchar("blockDesc", length = 1000).nullable()
    val busyTimestamp: Column<Long> = long("busyTimestamp").default(0L)
    val lastPingTimestamp: Column<Long> = long("lastPingTimestamp").default(0L)
    val lastAliveTimestamp: Column<Long> = long("lastAliveTimestamp").default(0L)
}