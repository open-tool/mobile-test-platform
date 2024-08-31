package com.farm.cli.command

import com.atiurin.atp.farmcore.entity.Device
import com.farm.cli.executor.Cli

/**
 * @param adbServerPort if null, when use default port
 */
class ConnectDeviceCommand(
    val adbServerPort: Int? = null,
    val device: Device
) : CliCommand {
    override suspend fun execute(): CliCommandResult {
        val portCmdPart = adbServerPort?.toString() ?: ""
        //  adb exit code on fail to connect = 0, .. f*ck sh*t
        val connectResult = Cli.execute("adb $portCmdPart connect ${device.ip}:${device.adbConnectPort}", timeoutMs = 5_000L)
        if (!connectResult.success){
            println("Not success: ${connectResult.message}")
            return connectResult
        }
        return Cli.execute("adb $portCmdPart -s ${device.ip}:${device.adbConnectPort} wait-for-device")
    }
}