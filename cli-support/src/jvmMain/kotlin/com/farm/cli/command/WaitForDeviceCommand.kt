package com.farm.cli.command

import com.atiurin.atp.farmcore.entity.Device
import com.farm.cli.analyzer.AdbWaitForDeviceCommandAnalyzer
import com.farm.cli.executor.CliCommandExecutor
import com.farm.cli.log.log

/**
 * @param adbServerPort if null, when use default port
 */
class WaitForDeviceCommand(
    private val adbServerPort: Int? = null,
    private val device: Device,
    private val timeoutSec: Long = 5
) : CliCommand {
    private val executor = CliCommandExecutor(AdbWaitForDeviceCommandAnalyzer())

    override suspend fun execute(): CliCommandResult {
        val port = adbServerPort ?: -1
        val portCmdPart = if (port > 0) "-P $port" else ""
        log.info { "Wait for device: $device with timeout: $timeoutSec sec" }
        return executor.execute("adb $portCmdPart -s ${device.ip}:${device.adbConnectPort} wait-for-device", timeoutMs = timeoutSec * 1000)
    }
}