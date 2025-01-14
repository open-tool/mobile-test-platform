package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcliclient.log
import com.atiurin.atp.farmcliclient.services.DeviceConnectionService
import com.atiurin.atp.farmcliclient.services.FarmDeviceConnectionService
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmserver.util.NetUtil
import com.farm.cli.executor.Cli
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.apache.commons.exec.environment.EnvironmentUtils


class MarathonTestRunCommand(
    private val deviceAmount: Int,
    private val groupId: String,
    private val isAllure: Boolean = false,
    private val marathonConfigFilePath: String? = null,
    private val adbPortVariable: String? = null,
    private val marathonCommand: String? = null,
    private val deviceConnectionTimeoutMs: Long = 5 * 60_000,
    envs: Map<String, String> = mutableMapOf()
) : Command {
    private val environments =
        envs.toMutableMap().apply { this.putAll(EnvironmentUtils.getProcEnvironment()) }

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

    private fun buildCliCommand(): String {
        val cmd = StringBuilder()
        if (isAllure) cmd.append("allurectl watch -- ")
        marathonCommand?.let { cmd.append("$it ") } ?: cmd.append("marathon ")
        marathonConfigFilePath?.let { cmd.append("-m $marathonConfigFilePath ") }
        log.info { "Cli command = '$cmd'" }
        return cmd.toString()
    }

    override fun execute(): Boolean {
        val adbServer = runAdbServer()
        val channel = Channel<Device>()
        val connectionService: DeviceConnectionService = FarmDeviceConnectionService(
            farmClient = FarmClientProvider.client,
            adbServer = adbServer,
            channel = channel,
            deviceConnectionTimeoutMs = deviceConnectionTimeoutMs)
        connectionService.connect(deviceAmount, groupId)
        runBlocking {
            // wait 1st device to be connected
            val firstDevice = channel.receive()
            log.info { "1st device connected: $firstDevice, start marathon test run" }
        }
        val result = Cli.execute(buildCliCommand(), environments)
        log.info { "marathon cli command success = ${result.success}, message = ${result.message}" }
        connectionService.disconnect()
        adbServer.kill()
        return result.success
    }
}