package com.atiurin.atp.farmcliclient.services

import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.log
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.isPreparing
import com.atiurin.atp.farmcore.entity.toDevices
import com.atiurin.atp.kmpclient.FarmClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.LinkedBlockingQueue

class FarmDeviceConnectionService(
    private val farmClient: FarmClient,
    private val adbServer: AdbServer,
    private val connectedDeviceQueue: LinkedBlockingQueue<Device>,
    private val deviceConnectionTimeoutMs: Long,
) : DeviceConnectionService {
    private val devices = mutableListOf<Device>()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun connect(amount: Int, groupId: String) {
        runBlocking {
            val devicesAcquired = farmClient.acquire(amount, groupId)
            log.info { "Acquired devices: $devicesAcquired" }
            devices.addAll(devicesAcquired)
            if (devices.isEmpty()) throw RuntimeException("No devices available")
        }
        devices.filter { it.state == DeviceState.READY }.forEach { device ->
            connectDevice(device, deviceConnectionTimeoutMs)
        }
        scope.launch { connectReadyDevices() }
    }

    private fun connectDevice(device: Device, timeoutMs: Long) {
        scope.launch {
            adbServer.connect(device, timeoutMs = timeoutMs).onSuccess { device ->
                log.info { "Send device to connected channel: $device" }
                connectedDeviceQueue.add(device)
            }.onFailure {
                log.error { "Failed to connect device: $device" }
            }
        }
    }

    override fun disconnect() {

        CoroutineScope(Dispatchers.IO).launch {
            farmClient.releaseAllCaptured()
        }
        adbServer.disconnect(devices)
    }

    private suspend fun connectReadyDevices() {
        val notReadyDevices = devices.filter { it.state.isPreparing() }.map { it.id }
        if (notReadyDevices.isEmpty()) return
        val readyToConnectDevices = farmClient.info(notReadyDevices).toDevices()
            .filter { it.state == DeviceState.READY }
        readyToConnectDevices.forEach { updatedDevice ->
            devices.removeIf { it.id == updatedDevice.id }
            devices.add(updatedDevice)
            connectDevice(updatedDevice, deviceConnectionTimeoutMs)
        }
        if (devices.count { it.state.isPreparing() } > 0) {
            delay(3000)
            connectReadyDevices()
        }
    }
}