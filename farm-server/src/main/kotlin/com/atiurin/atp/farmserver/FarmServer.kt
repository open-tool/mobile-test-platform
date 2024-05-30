package com.atiurin.atp.farmserver

import com.atiurin.atp.farmcore.models.FarmMode
import com.atiurin.atp.farmserver.config.InitialArguments
import com.atiurin.atp.farmserver.config.InitialConfig
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.servers.repository.LocalServerRepository
import com.atiurin.atp.farmserver.servers.repository.ServerRepository
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import jakarta.annotation.PreDestroy
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.logging.LoggingApplicationListener
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment


fun main(args: Array<String>) = App().main(args)

class App: CliktCommand() {
    val maxAmount by option("-m", "--max_amount").int().required()
    val keepAliveDevices: Map<String, String> by option("-kad", "--keep_alive_devices").associate()
    val deviceBusyTimeoutInSec by option("-dbt", "--device_busy_timeout").long().required()
    val images: Map<String, String> by option("-i", "--img").associate()
    val mockDevice by option("-md", "--mock_device").flag()
    val startPortParam by option("-sp", "--start_port").int()
    val endPortParam by option("-ep", "--end_port").int()

    override fun run() {
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
        InitialArguments.config = InitialConfig(
            maxDevicesAmount = maxAmount,
            keepAliveDevicesMap = devicesMap.toMutableMap(),
            deviceBusyTimeoutSec = deviceBusyTimeoutInSec,
            isMock = mockDevice,
            startPort = startPortParam ?: 0,
            endPort = endPortParam ?: 65534,
            imagesMap = images,
        )
        val app = runApplication<FarmServer>()
        app.addApplicationListener { LoggingApplicationListener() }
        val farmServer = app.getBean(FarmServer::class.java)
        farmServer.run()
    }
}

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, ExposedAutoConfiguration::class])
class FarmServer {
    @Autowired
    lateinit var localServerRepository: LocalServerRepository

    fun run(){
        localServerRepository.register()
    }
}

