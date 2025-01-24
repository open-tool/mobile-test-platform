package com.atiurin.atp.farmcliclient.commands

import com.atiurin.atp.farmcliclient.FarmClientProvider
import com.atiurin.atp.farmcliclient.adb.AdbServer
import com.atiurin.atp.farmcliclient.adb.AdbServerImpl
import com.atiurin.atp.farmcliclient.services.DeviceConnectionService
import com.atiurin.atp.farmcliclient.services.FarmDeviceConnectionService
import com.atiurin.atp.farmcore.entity.Device
import com.atiurin.atp.farmcore.util.waitFor
import com.atiurin.atp.farmserver.util.NetUtil
import org.apache.commons.exec.environment.EnvironmentUtils
import java.util.concurrent.LinkedBlockingQueue

class AcquireCommand(
    private val deviceAmount: Int,
    private val groupId: String,
    private val adbPortVariable: String? = null,
    private val deviceConnectionTimeoutSec: Long = 5 * 60,
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
        val connectedDeviceQueue = LinkedBlockingQueue<Device>()
        val connectionService: DeviceConnectionService = FarmDeviceConnectionService(
            farmClient = FarmClientProvider.client,
            adbServer = adbServer,
            connectedDeviceQueue = connectedDeviceQueue,
            deviceConnectionTimeoutSec = deviceConnectionTimeoutSec
        )
        connectionService.connect(deviceAmount, groupId)
        waitFor(timeoutMs = deviceConnectionTimeoutSec){
            connectedDeviceQueue.size == deviceAmount
        }
//        connectionService.disconnect()
//        adbServer.kill()
        return true
    }
}