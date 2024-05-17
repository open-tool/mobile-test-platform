package com.atiurin.atp.farmcore.models

import java.util.Locale

enum class DeviceState {
    NEED_CREATE,
    CREATING,
    READY,
    NEED_REMOVE,
    REMOVING;
}
fun DeviceState.lowercaseName() = this.name.lowercase(Locale.getDefault())

fun DeviceState.isAlive(): Boolean {
    return this != DeviceState.NEED_REMOVE && this != DeviceState.REMOVING
}