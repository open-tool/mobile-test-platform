package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.models.Config
import com.atiurin.atp.farmcore.models.FarmMode

data class InitialConfig(
    val maxDevicesAmount: Int = 0,
    val keepAliveDevicesMap: Map<String, Int> = mapOf(),
    val deviceBusyTimeoutSec: Long = 30 * 60,
    val isMock: Boolean = false,
    val startPort: Int = 0,
    val endPort: Int = 65534,
    val imagesMap: Map<String, String> = mapOf(),
    val farmMode: FarmMode = FarmMode.Local
)

fun InitialConfig.toConfig() = Config(
    maxDevicesAmount = this.maxDevicesAmount,
    keepAliveDevicesMap = this.keepAliveDevicesMap.toMutableMap(),
    deviceBusyTimeoutSec = this.deviceBusyTimeoutSec,
    isMock = this.isMock,
    startPort = this.startPort,
    endPort = this.endPort,
    farmMode = this.farmMode
)

