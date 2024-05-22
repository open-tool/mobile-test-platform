package com.atiurin.atp.farmcliclient.services

import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmclient.FarmClient
import com.atiurin.atp.farmcore.models.Device
import com.atiurin.atp.farmcore.models.DeviceState
import com.atiurin.atp.farmcore.models.isPreparing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FarmDeviceConnectionService(
    private val farmClient: FarmClient,
    private val adbServer: AdbServer
) : DeviceConnectionService {
    private val devices = mutableListOf<Device>()

    override fun connect(amount: Int, groupId: String) {
        devices.addAll(farmClient.acquire(amount, groupId))
        if (devices.isEmpty()) throw RuntimeException("No devices available")
        adbServer.connect(devices.filter { it.state == DeviceState.READY })
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { connectReadyDevices() }
    }

    override fun disconnect() {
        farmClient.releaseAllCaptured()
        adbServer.disconnect(devices)
    }

    private suspend fun connectReadyDevices() {
        val deviceConnectionScope = CoroutineScope(Dispatchers.Default)
        val notReadyDevice = devices.filter { it.state.isPreparing() }.map { it.id }
        if (notReadyDevice.isEmpty()) return
        val readyToConnectDevices = farmClient.info(notReadyDevice)
            .filter { it.state == DeviceState.READY }
        readyToConnectDevices.forEach { updatedDevice ->
            devices.removeIf { it.id == updatedDevice.id }
            devices.add(updatedDevice)
            deviceConnectionScope.launch {
                adbServer.connect(updatedDevice)
            }
        }
        if (devices.count { it.state.isPreparing() } > 0) {
            delay(3000)
            connectReadyDevices()
        }
    }
}