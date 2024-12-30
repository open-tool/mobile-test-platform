package com.atiurin.atp.farmcore.entity

enum class DeviceStatus {
    FREE,
    BUSY,
    BLOCKED;
}

fun DeviceStatus.lowercaseName() = this.name.lowercase()