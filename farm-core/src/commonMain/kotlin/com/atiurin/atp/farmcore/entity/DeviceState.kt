package com.atiurin.atp.farmcore.entity


enum class DeviceState {
    NEED_CREATE,
    CREATING,
    READY,
    NEED_REMOVE,
    REMOVING;
}
fun DeviceState.lowercaseName() = this.name.lowercase()

fun DeviceState.isAlive(): Boolean {
    return this != DeviceState.NEED_REMOVE && this != DeviceState.REMOVING
}

fun DeviceState.isPreparing(): Boolean {
    return this == DeviceState.NEED_CREATE || this == DeviceState.CREATING
}