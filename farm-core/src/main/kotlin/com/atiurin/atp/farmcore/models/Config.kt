package com.atiurin.atp.farmcore.models

import com.atiurin.atp.farmcore.util.NetUtil

data class Config(
    var maxDevicesAmount: Int = 0,
    var keepAliveDevicesMap: MutableMap<String, Int> = mutableMapOf(),
    var deviceBusyTimeoutSec: Long = 30 * 60,
    var isMock: Boolean = false,
    var startPort: Int = 0,
    var endPort: Int = 65534,
    var devicePoolMonitorDelay: Long = 5_000L,
    var serverMonitorDelay: Long = 5_000L,
    var busyDevicesMonitorDelay: Long = 5_000L,
    val farmMode: FarmMode = FarmMode.Local,
    val deviceNeedToDeleteMonitorDelay: Long = 5_000L,
    val deviceNeedToCreateMonitorDelay: Long = 5_000L
)

fun Config.getPortInRange() = NetUtil.getFreePortInRange(this.startPort, this.endPort)