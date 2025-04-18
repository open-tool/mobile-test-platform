package com.atiurin.atp.farmcliclient

import com.atiurin.atp.farmcliclient.commands.AcquireCommand
import com.atiurin.atp.farmcliclient.commands.MarathonTestRunCommand
import com.atiurin.atp.farmcliclient.util.GitLabUtil
import com.atiurin.atp.kmpclient.FarmClientConfig
import com.atiurin.atp.kmpclient.getFarmUrlFromString
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import kotlin.system.exitProcess

class FarmCliClient : CliktCommand() {
    val urls: List<String>? by option("-u", "--url").multiple()
    val command: Command? by option("-c", "--command").enum<Command>()
    val deviceAmount by option("-da", "--device_amount").int().required()
    val groupId by option("-g", "--group_id")
    val runCommand by option("-rc", "--run_command")
    val allureWatch by option("-aw", "--allure", "--allure_watch").flag()
    val marathonCommand by option("-mcmd", "--marathon_command")
    val marathonConfigFilePath by option("-mc", "--marathon_config")
    val marathonAdbPortVariable by option("-mapv", "--marathon_adb_port_variable")
    val environments: Map<String, String> by option("-e", "--env").associate()
    val userAgent by option("-ua", "--user_agent")
    val deviceConnectionTimeoutSec by option("-dct", "--device_connection_timeout_sec").long()
    val timeoutSec by option("-to", "--timeout_sec").long()

    override fun run() {
        val group = groupId ?: throw RuntimeException("Specify -g or --group_id option.")
        val farmUrls = urls?.map {
            getFarmUrlFromString(it)
        } ?: listOf(getFarmUrlFromString("http://localhost:8080"))
        val deviceConnectionTimeoutSec = deviceConnectionTimeoutSec ?: (5 * 60)
        val commandTimeout = timeoutSec ?: (30 * 60)
        FarmClientProvider.init(
            FarmClientConfig(
                farmUrls = farmUrls,
                userAgent = userAgent ?: GitLabUtil.getProjectId() ?: "test"
            )
        )
        val isSuccess = when (command) {
            Command.ACQUIRE -> {
                AcquireCommand(deviceAmount, group, deviceConnectionTimeoutSec = deviceConnectionTimeoutSec).execute()
            }
            else -> {
                MarathonTestRunCommand(
                    deviceAmount = deviceAmount,
                    groupId = group,
                    isAllureWatch = allureWatch,
                    marathonConfigFilePath = marathonConfigFilePath,
                    adbPortVariable = marathonAdbPortVariable,
                    marathonCommand = marathonCommand,
                    envs = environments,
                    deviceConnectionTimeoutSec = deviceConnectionTimeoutSec,
                    timeoutSec = commandTimeout
                ).execute()
            }
        }
        if (!isSuccess) exitProcess(1)
    }
}

fun main(args: Array<String>) = FarmCliClient().main(args)

enum class Command { RUN, ACQUIRE, RELEASE }