package com.atiurin.atp.farmcliclient.adb

import com.atiurin.atp.farmcliclient.logger.log
import com.atiurin.atp.farmcore.entity.Device
import com.farm.cli.command.ConnectDeviceCommand
import com.farm.cli.command.WaitForDeviceCommand
import com.farm.cli.executor.CliCommandExecutor
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTimedValue

@Suppress("ThrowableNotThrown")
class AdbServerImpl(override val port: Int) : AdbServer {

    private val portCmdPart = if (port > 0) "-P $port" else ""

    override fun start() {
        CliCommandExecutor().execute("adb $portCmdPart start-server")
    }

    override fun kill() {
        CliCommandExecutor().execute("adb $portCmdPart kill-server")
    }

    override fun connect(devices: List<Device>) {
        log.info { "devices to connect: $devices" }
        devices.forEach {
            runBlocking {
                connect(it, timeoutSec = 60_000)
            }
        }
    }

    override suspend fun connect(device: Device, timeoutSec: Long) : Result<Device> {
        log.info { "Connect device: $device with timeout: $timeoutSec" }
        val timedResult = measureTimedValue {
            val connectResult = ConnectDeviceCommand(adbServerPort = port, device = device, timeoutMs = timeoutSec * 1000).execute()
            if (connectResult.success){
                waitForDevice(device, 5000)
            } else {
                Result.failure(RuntimeException("Connect failed: ${connectResult.message}"))
            }
        }
        val result = if (timedResult.value.isFailure && timedResult.duration.inWholeSeconds < timeoutSec){
            connect(device, timeoutSec - timedResult.duration.inWholeSeconds)
        } else {
            timedResult.value
        }
        return result
    }

    suspend fun waitForDevice(device: Device, timeoutSec: Long = 5): Result<Device> {
        val waitForDeviceResult = WaitForDeviceCommand(adbServerPort = port, device = device, timeoutSec = timeoutSec).execute()
        return if (waitForDeviceResult.success){
            log.info { "Device ${device.id} with ip ${device.ip}:${device.adbConnectPort} connected successfully" }
            Result.success(device)
        } else {
            Result.failure(RuntimeException("Wait for device failed: ${waitForDeviceResult.message}"))
        }
    }

    override fun disconnect(devices: List<Device>) {
        log.info { "devices to disconnect: $devices" }
        devices.forEach {
            CliCommandExecutor().execute("adb $portCmdPart disconnect ${it.ip}:${it.adbConnectPort}")
        }
    }

    override fun printDevices() {
        CliCommandExecutor().execute("adb $portCmdPart devices")
    }
}