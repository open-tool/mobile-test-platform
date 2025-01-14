package com.atiurin.atp.farmcliclient.adb

import com.atiurin.atp.farmcliclient.log
import com.atiurin.atp.farmcore.entity.Device
import com.farm.cli.executor.Cli

@Suppress("ThrowableNotThrown")
class AdbServerImpl(override val port: Int) : AdbServer {

    private val portCmdPart = if (port > 0) "-P $port" else ""

    override fun start() {
        Cli.execute("adb $portCmdPart start-server")
    }

    override fun kill() {
        Cli.execute("adb $portCmdPart kill-server")
    }

    override fun connect(devices: List<Device>) {
        log.info { "devices to connect: $devices" }
        devices.forEach {
            Cli.execute("adb $portCmdPart connect ${it.ip}:${it.adbConnectPort}")
            Cli.execute("adb $portCmdPart -s ${it.ip}:${it.adbConnectPort} wait-for-device")
        }
    }

    override suspend fun connect(device: Device, timeoutMs: Long) : Result<Device> {
        log.info { "Connect device: $device" }
        val connectResult = Cli.execute("adb $portCmdPart connect ${device.ip}:${device.adbConnectPort}", timeoutMs = timeoutMs)
        val result = if (connectResult.success){
            val waitForDeviceResult = Cli.execute("adb $portCmdPart -s ${device.ip}:${device.adbConnectPort} wait-for-device", timeoutMs = timeoutMs)
            if (waitForDeviceResult.success){
                log.info { "Device ${device.id} with ip ${device.ip}:${device.adbConnectPort} connected successfully" }
                Result.success(device)
            } else {
                Result.failure(RuntimeException("Wait for device failed: ${waitForDeviceResult.message}"))
            }
        }else{
            Result.failure(RuntimeException("Connect failed: ${connectResult.message}"))
        }
        return result
    }

    override fun disconnect(devices: List<Device>) {
        log.info { "devices to disconnect: $devices" }
        devices.forEach {
            Cli.execute("adb $portCmdPart disconnect ${it.ip}:${it.adbConnectPort}")
        }
    }

    override fun printDevices() {
        Cli.execute("adb $portCmdPart devices")
    }
}