package com.atiurin.atp.farmcliclient.services

import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.isPreparing
import com.atiurin.atp.farmcore.entity.toDevices
import com.atiurin.atp.kmpclient.FarmClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FarmDeviceConnectionService(
    private val farmClient: FarmClient,
    private val adbServer: AdbServer,
    private val channel: Channel<Device>,
    private val deviceConnectionTimeoutMs: Long
) : DeviceConnectionService {
    private val devices = mutableListOf<Device>()

    override fun connect(amount: Int, groupId: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        runBlocking {
            devices.addAll(farmClient.acquire(amount, groupId))
            if (devices.isEmpty()) throw RuntimeException("No devices available")
        }
        devices.filter { it.state == DeviceState.READY }.forEach { device ->
            scope.launch {
                adbServer.connect(device, timeoutMs = deviceConnectionTimeoutMs).onSuccess {
                    channel.send(device)
                }
            }
        }

        scope.launch { connectReadyDevices() }
    }

    override fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            farmClient.releaseAllCaptured()
        }
        adbServer.disconnect(devices)
    }

    private suspend fun connectReadyDevices() {
        val deviceConnectionScope = CoroutineScope(Dispatchers.IO)
        val notReadyDevices = devices.filter { it.state.isPreparing() }.map { it.id }
        if (notReadyDevices.isEmpty()) return
        val readyToConnectDevices = farmClient.info(notReadyDevices).toDevices()
            .filter { it.state == DeviceState.READY }
        readyToConnectDevices.forEach { updatedDevice ->
            devices.removeIf { it.id == updatedDevice.id }
            devices.add(updatedDevice)
            deviceConnectionScope.launch {
                adbServer.connect(updatedDevice).onSuccess {
                    channel.send(updatedDevice)
                }
            }
        }
        if (devices.count { it.state.isPreparing() } > 0) {
            delay(3000)
            connectReadyDevices()
        }
    }
}