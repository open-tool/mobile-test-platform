package com.atiurin.atp.farmserver

import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.config.Config
import com.atiurin.atp.farmserver.monitor.Monitor
import com.atiurin.atp.farmserver.node.NodeRepository
import com.atiurin.atp.farmserver.pool.DevicePool
import com.atiurin.atp.farmserver.pool.DevicePoolProvider
import com.atiurin.atp.farmserver.provider.DeviceProviderContainer.deviceProvider
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
class FarmApplication : CliktCommand() {
    val devicePool = DevicePoolProvider.devicePool
    val maxAmount by option("-m", "--max_amount").int().required()
    val keepAliveAmount by option("-ka", "--keep_alive_amount").int().required()
    val defaultApiValue by option("-da", "--default_api").int().required()
    val initialDevices = mutableListOf<DeviceInfo>()

    @Bean
    fun createRepo(deviceRepository: NodeRepository) = CommandLineRunner {
        initialDevices.forEach { device ->
            devicePool.join(deviceProvider.createDevice(device))
        }
        println(devicePool.all().map { it.device.containerInfo.adbPort })
    }

    override fun run() {
        runApplication<FarmApplication>()
        ConfigProvider.set {
            maxDevicesAmount = maxAmount
            keepAliveDevicesAmount = keepAliveAmount
            defaultApi = defaultApiValue
        }
        Monitor.startMonitors()
    }
}

fun main(args: Array<String>) = FarmApplication().main(args)

