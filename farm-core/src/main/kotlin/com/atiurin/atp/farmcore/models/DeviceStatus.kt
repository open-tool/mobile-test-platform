package com.atiurin.atp.farmcore.models

enum class DeviceStatus(val intValue: Int) {
    FREE(0),
    BUSY(1),
    BLOCKED(2);

    companion object {
        private val map = DeviceStatus.entries.associateBy(DeviceStatus::intValue)

        fun fromInt(intValue: Int) = map[intValue] ?: FREE
    }

}
