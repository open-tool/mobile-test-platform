package com.farm.cli.command

import com.atiurin.atp.farmcore.entity.Device
import com.farm.cli.analyzer.AdbConnectCommandAnalyzer
import com.farm.cli.executor.CliCommandExecutor
import com.farm.cli.log.log

/**
 * @param adbServerPort if null, when use default port
 */
class ConnectDeviceCommand(
    private val adbServerPort: Int? = null,
    private val device: Device,
    private val timeoutMs: Long = 5_000,
) : CliCommand {
    private val executor = CliCommandExecutor(AdbConnectCommandAnalyzer())

    override suspend fun execute(): CliCommandResult {
        val port = adbServerPort ?: -1
        val portCmdPart = if (port > 0) "-P $port" else ""
        log.info { "Connect device: $device with timeout: $timeoutMs" }
        return executor.execute("adb $portCmdPart connect ${device.ip}:${device.adbConnectPort}", timeoutMs = timeoutMs)
    }
}