package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.Log
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcliclient.executor.Cli
import com.atiurin.atp.farmcliclient.log
import com.atiurin.atp.farmcliclient.util.NetUtil
import com.atiurin.atp.farmcore.models.Device
import org.apache.commons.exec.environment.EnvironmentUtils


class MarathonTestRunCommand(
    private val deviceAmount: Int,
    private val api: Int,
    private val isAllure: Boolean = false,
    private val marathonConfigFilePath: String? = null,
    private val adbPortVariable: String? = null,
    envs: Map<String, String> = mutableMapOf()
) : Command {
    private val farmClient = FarmClientProvider.client
    private val devices = mutableListOf<Device>()
    private val environments =
        envs.toMutableMap().apply { this.putAll(EnvironmentUtils.getProcEnvironment()) }

    fun requestDevices() {
        devices.addAll(farmClient.acquire(deviceAmount, api))
        if (devices.isEmpty()) throw RuntimeException("No devices available")
    }

    fun runAdbServer(): AdbServer {
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
        if (isAllure) result.append("./allurectl watch -- ")
        result.append("marathon ")
        marathonConfigFilePath?.let { result.append("-m $marathonConfigFilePath ") }
        log.info { "Cli command = '$result'" }
        return result.toString()
    }

    override fun execute(): Boolean {
        requestDevices()
        val adbServer = runAdbServer()
        adbServer.connect(devices)
        val isSuccess = Cli.execute(buildCliCommand(), environments)
        log.info { "marathon cli command success = $isSuccess" }
        adbServer.kill()
        farmClient.releaseAllCaptured()
        return isSuccess
    }
}