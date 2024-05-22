package com.atiurin.atp.farmcore.models

import java.util.Locale

enum class DeviceStatus {
    FREE,
    BUSY,
    BLOCKED;
}
fun DeviceStatus.lowercaseName() = this.name.lowercase(Locale.getDefault())