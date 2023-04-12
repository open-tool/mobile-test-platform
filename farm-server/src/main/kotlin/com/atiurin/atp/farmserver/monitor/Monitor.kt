package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.pool.DevicePoolProvider
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

suspend fun monitorDevicePool() {
    while (true){
        val keepAliveDeviceAmount = ConfigProvider.get().keepAliveDevicesAmount
        val devicePool = DevicePoolProvider.devicePool
        val launchedDevicesAmount = devicePool.all().size
        if (launchedDevicesAmount < keepAliveDeviceAmount) {
            devicePool.create(
                keepAliveDeviceAmount - launchedDevicesAmount,
                ConfigProvider.get().defaultApi
            )
        }
    }
}

object Monitor {
    fun startMonitors(){
        val monitorThreadContext = newSingleThreadContext("MonitorThread")
        runBlocking {
            withContext(monitorThreadContext){
                monitorDevicePool()
            }
        }
    }
}