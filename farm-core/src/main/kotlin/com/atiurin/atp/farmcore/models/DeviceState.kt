package com.atiurin.atp.farmcore.models

enum class DeviceState(val intValue: Int) {
    NEED_CREATE(0),
    CREATING(1),
    READY(2),
    NEED_REMOVE(3),
    REMOVING(4);

    companion object {
        private val map = DeviceState.entries.associateBy(DeviceState::intValue)

        fun fromInt(intValue: Int) = map[intValue] ?: NEED_CREATE
    }

}

fun DeviceState.isAlive(): Boolean {
    return this != DeviceState.NEED_REMOVE && this != DeviceState.REMOVING
}