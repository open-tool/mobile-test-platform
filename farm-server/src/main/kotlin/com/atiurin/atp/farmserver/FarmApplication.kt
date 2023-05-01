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
class FarmApplication : CliktCommand() {
    val maxAmount by option("-m", "--max_amount").int().required()
    val keepAliveAmount by option("-ka", "--keep_alive_amount").int().required()
    val defaultApiValue by option("-da", "--default_api").int().required()
    val deviceBusyTimeoutInSec by option("-dbt", "--device_busy_timeout").long().required()
    val images: Map<String, String> by option("-i", "--img").associate()
    val mockDevice by option("-md", "--mock_device").flag()

    override fun run() {
        val app = runApplication<FarmApplication>()
        app.addApplicationListener { LoggingApplicationListener() }
        log.info {
            "Farm server is started with params: maxAmount = $maxAmount, keepAliveAmount = $keepAliveAmount, " +
                    "defaultApiValue = $defaultApiValue, deviceBusyTimeoutInSec = $deviceBusyTimeoutInSec, images = $images, mock_device = $mockDevice"
        }
        ConfigProvider.set {
            maxDevicesAmount = maxAmount
            keepAliveDevicesAmount = keepAliveAmount
            defaultApi = defaultApiValue
            deviceBusyTimeoutSec = deviceBusyTimeoutInSec
            isMock = mockDevice
        }
        AndroidImage.set(images)
        if (ConfigProvider.get().isMock) {
            DevicePoolProvider.devicePool = MockDevicePool()
        }
        Monitor.startMonitors()
    }
}

fun main(args: Array<String>) = FarmApplication().main(args)

