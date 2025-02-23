package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcliclient.logger.log
import com.atiurin.atp.farmcliclient.services.DeviceConnectionService
import com.atiurin.atp.farmcliclient.services.FarmDeviceConnectionService
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.util.waitFor
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
    private val deviceConnectionTimeoutSec: Long = 5 * 60,
    private val timeoutSec: Long = 30 * 60,
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
            deviceConnectionTimeoutSec = deviceConnectionTimeoutSec)
        connectionService.connect(deviceAmount, groupId)
        val isConnected = waitFor(timeoutMs = deviceConnectionTimeoutSec * 1000){
            connectedDeviceQueue.isNotEmpty()
        }
        val success = if (isConnected){
            log.info { "Already connected devices size = ${connectedDeviceQueue.size}, start marathon test run" }
            val cmd = CliCommandExecutor(addSystemOut = true)
            val result = cmd.execute(buildCliCommand(), envs = environments, timeoutMs = timeoutSec * 1000)
            log.info { "marathon cli command success = ${result.success}, message = ${result.message}" }
            result.success
        } else {
            log.error { "Couldn't connect devices in $deviceConnectionTimeoutSec sec" }
            false
        }
        connectionService.disconnect()
        adbServer.kill()
        return success
    }
}