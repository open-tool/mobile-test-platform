package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcliclient.log
import com.atiurin.atp.farmcliclient.services.DeviceConnectionService
import com.atiurin.atp.farmcliclient.services.FarmDeviceConnectionService
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmserver.util.NetUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.apache.commons.exec.environment.EnvironmentUtils


class AcquireCommand(
    private val deviceAmount: Int,
    private val groupId: String,
    private val adbPortVariable: String? = null,
    private val deviceConnectionTimeoutMs: Long = 5 * 60_000,
    envs: Map<String, String> = mutableMapOf()
) : Command {
    private val environments = envs.toMutableMap().apply { this.putAll(EnvironmentUtils.getProcEnvironment()) }

    private fun runAdbServer(): AdbServer {
        val adbPort = adbPortVariable?.let {
            val port = NetUtil.getFreePort()
            environments[adbPortVariable] = port.toString()
            port
        } ?: -1
        val adbServer = AdbServerImpl(adbPort)
        adbServer.start()
        return adbServer
    }

    override fun execute(): Boolean {
        val adbServer = runAdbServer()
        val channel = Channel<Device>()
        val connectionService: DeviceConnectionService = FarmDeviceConnectionService(
            farmClient = FarmClientProvider.client,
            adbServer = adbServer,
            channel = channel,
            deviceConnectionTimeoutMs = deviceConnectionTimeoutMs
        )
        connectionService.connect(deviceAmount, groupId)
        runBlocking {
            withTimeout(deviceConnectionTimeoutMs){
                for (i in 1..deviceAmount) {
                    val device = channel.receive()
                    log.info { "Received device: $device" }
                }
            }
        }
//        connectionService.disconnect()
//        adbServer.kill()
        return true
    }
}