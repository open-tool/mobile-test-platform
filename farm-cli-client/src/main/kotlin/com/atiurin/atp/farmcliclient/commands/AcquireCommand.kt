package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmcore.models.Device
import org.apache.commons.exec.environment.EnvironmentUtils


class AcquireCommand(
    private val deviceAmount: Int,
    private val groupId: String,
    private val adbPortVariable: String? = null,
    envs: Map<String, String> = mutableMapOf()
) : Command {
    private val farmClient = FarmClientProvider.client
    private val devices = mutableListOf<Device>()
    private val environments = envs.toMutableMap().apply { this.putAll(EnvironmentUtils.getProcEnvironment()) }

    fun requestDevices() {
        devices.addAll(farmClient.acquire(deviceAmount, groupId))
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

    override fun execute(): Boolean {
        requestDevices()
        val adbServer = runAdbServer()
        adbServer.connect(devices)
        adbServer.kill()
        farmClient.releaseAllCaptured()
        return true
    }
}