package com.farm.cli.command

import com.atiurin.atp.farmcore.entity.Device
import com.farm.cli.executor.CliCommandExecutor

/**
 * @param adbServerPort if null, when use default port
 */
class DisconnectDeviceCommand(
    val adbServerPort: Int? = null,
    val device: Device
) : CliCommand {
    override suspend fun execute(): CliCommandResult {
        val port = adbServerPort ?: -1
        val portCmdPart = if (port > 0) "-P $port" else ""
        return CliCommandExecutor().execute("adb $portCmdPart disconnect ${device.ip}:${device.adbConnectPort}")
    }
}