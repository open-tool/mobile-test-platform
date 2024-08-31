package com.farm.cli.command

import com.atiurin.atp.farmcore.entity.Device
import com.farm.cli.executor.Cli

/**
 * @param adbServerPort if null, when use default port
 */
class DisconnectDeviceCommand(
    val adbServerPort: Int? = null,
    val device: Device
) : CliCommand {
    override suspend fun execute(): CliCommandResult {
        val portCmdPart = adbServerPort?.toString() ?: ""
        return Cli.execute("adb $portCmdPart disconnect ${device.ip}:${device.adbConnectPort}")
    }
}