package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcliclient.executor.Cli
import com.atiurin.atp.farmcliclient.log
import com.atiurin.atp.farmcliclient.services.DeviceConnectionService
import com.atiurin.atp.farmcliclient.services.FarmDeviceConnectionService
import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmcore.models.Device
import org.apache.commons.exec.environment.EnvironmentUtils


class MarathonTestRunCommand(
    private val deviceAmount: Int,
    private val groupId: String,
    private val isAllure: Boolean = false,
    private val marathonConfigFilePath: String? = null,
    private val adbPortVariable: String? = null,
    private val marathonCommand: String? = null,
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
        val result = StringBuilder()
        if (isAllure) result.append("allurectl watch -- ")
        marathonCommand?.let { result.append("$it ") } ?: result.append("marathon ")
        marathonConfigFilePath?.let { result.append("-m $marathonConfigFilePath ") }
        log.info { "Cli command = '$result'" }
        return result.toString()
    }

    override fun execute(): Boolean {
        val adbServer = runAdbServer()
        val connectionService: DeviceConnectionService = FarmDeviceConnectionService(FarmClientProvider.client, adbServer)
        connectionService.connect(deviceAmount, groupId)
        val isSuccess = Cli.execute(buildCliCommand(), environments)
        log.info { "marathon cli command success = $isSuccess" }
        connectionService.disconnect()
        adbServer.kill()
        return isSuccess
    }
}