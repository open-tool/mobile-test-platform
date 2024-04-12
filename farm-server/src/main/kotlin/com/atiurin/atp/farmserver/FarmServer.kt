package com.atiurin.atp.farmserver

import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.images.AndroidImage
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.monitor.Monitor
import com.atiurin.atp.farmserver.pool.DevicePoolProvider
import com.atiurin.atp.farmserver.pool.MockDevicePool
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.logging.LoggingApplicationListener
import org.springframework.boot.runApplication

@SpringBootApplication
class FarmServer : CliktCommand() {
    val maxAmount by option("-m", "--max_amount").int().required()
    val keepAliveDevices: Map<String, String> by option("-kad", "--keep_alive_devices").associate()
    val deviceBusyTimeoutInSec by option("-dbt", "--device_busy_timeout").long().required()
    val images: Map<String, String> by option("-i", "--img").associate()
    val mockDevice by option("-md", "--mock_device").flag()
    val startPortParam by option("-sp", "--start_port").int()
    val endPortParam by option("-ep", "--end_port").int()

    override fun run() {
        val app = runApplication<FarmServer>()
        app.addApplicationListener { LoggingApplicationListener() }
        log.info {
            """
            | Farm server is started with params: max_amount = $maxAmount, keep_alive_devices = $keepAliveDevices,
            | device_busy_timeout (in seconds) = $deviceBusyTimeoutInSec, images = $images, mock_device = $mockDevice,
            | start_port = $startPortParam, end_port = $endPortParam
            """.trimMargin()

        }
        val devicesMap = runCatching {
            keepAliveDevices.entries.map { e ->
                e.key to e.value.toInt()
            }
        } .onFailure {
            throw RuntimeException("Invalid keep_alive_devices value. It should be 'String[group_id]=Int[amount_of_devices]'.")
        }.getOrThrow().toMap()
        ConfigProvider.set {
            maxDevicesAmount = maxAmount
            keepAliveDevicesMap = devicesMap.toMutableMap()
            deviceBusyTimeoutSec = deviceBusyTimeoutInSec
            isMock = mockDevice
            startPortParam?.let { this.startPort = it }
            endPortParam?.let { this.endPort = it }
        }
        AndroidImage.set(images)
        if (ConfigProvider.get().isMock) {
            DevicePoolProvider.devicePool = MockDevicePool()
        }
        Monitor.startMonitors()
    }
}

fun main(args: Array<String>) = FarmServer().main(args)

