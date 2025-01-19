package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcliclient.log
import com.atiurin.atp.farmcliclient.services.DeviceConnectionService
import com.atiurin.atp.farmcliclient.services.FarmDeviceConnectionService
import com.atiurin.atp.farmcliclient.util.waitFor
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmserver.util.NetUtil
import com.farm.cli.executor.CliCommandExecutor
import org.apache.commons.exec.environment.EnvironmentUtils
import java.util.concurrent.LinkedBlockingQueue

class MarathonTestRunCommand(
    private val deviceAmount: Int,
    private val groupId: String,
    private val isAllure: Boolean = false,
    private val marathonConfigFilePath: String? = null,
    private val adbPortVariable: String? = null,
    private val marathonCommand: String? = null,
    private val deviceConnectionTimeoutMs: Long = 5 * 60_000,
    private val timeoutMs: Long = 30 * 60_000,
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
        val connectedDeviceQueue = LinkedBlockingQueue<Device>()
        val connectionService: DeviceConnectionService = FarmDeviceConnectionService(
            farmClient = FarmClientProvider.client,
            adbServer = adbServer,
            connectedDeviceQueue = connectedDeviceQueue,
            deviceConnectionTimeoutMs = deviceConnectionTimeoutMs)
        connectionService.connect(deviceAmount, groupId)
        val isConnected = waitFor(timeoutMs = deviceConnectionTimeoutMs){
            connectedDeviceQueue.isNotEmpty()
        }
        val success = if (isConnected){
            log.info { "Already connected devices size = ${connectedDeviceQueue.size}, start marathon test run" }
            val cmd = CliCommandExecutor()
            val result = cmd.execute(buildCliCommand(), envs = environments, timeoutMs = timeoutMs)
            log.info { "marathon cli command success = ${result.success}, message = ${result.message}" }
            result.success
        } else {
            log.error { "Couldn't connect devices in $deviceConnectionTimeoutMs ms" }
            false
        }
        connectionService.disconnect()
        adbServer.kill()
        return success
    }
}