package com.atiurin.atp.farmcore.entity


data class Config(
    val farmMode: FarmMode = FarmMode.LOCAL,
    var maxDevicesAmount: Int = 0,
    var maxDeviceCreationBatchSize: Int = 10,
    var keepAliveDevicesMap: MutableMap<String, Int> = mutableMapOf(),
    var busyDeviceTimeoutSec: Long = 30 * 60,
    var creatingDeviceTimeoutSec: Long = 5 * 60,
    var isMock: Boolean = false,
    var startPort: Int = 0,
    var endPort: Int = 65534,
    var devicePoolMonitorDelay: Long = 5_000L,
    var serverMonitorDelay: Long = 5_000L,
    var busyDevicesMonitorDelay: Long = 5_000L,
    var creatingDevicesMonitorDelay: Long = 5_000L,
    val deviceNeedToDeleteMonitorDelay: Long = 5_000L,
    val deviceNeedToCreateMonitorDelay: Long = 5_000L,
    val brokenDevicesMonitorDelay: Long = 30_000L,
    var androidContainerAdbPath: String = "/android/sdk/platform-tools",
)
