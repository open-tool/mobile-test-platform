package com.atiurin.atp.farmcliclient

import com.atiurin.atp.farmcliclient.commands.AcquireCommand
import com.atiurin.atp.farmcliclient.commands.MarathonTestRunCommand
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
import org.apache.commons.exec.environment.EnvironmentUtils
import kotlin.system.exitProcess

var log = Log()

class Log {
    fun info(block: () -> Any?) = println(block())
    fun error(block: () -> Any?) = println(block())
}

class FarmCliClient : CliktCommand() {
    val urls: List<String>? by option("-u", "--url").multiple()
    val command: Command? by option("-c", "--command").enum<Command>()
    val deviceAmount by option("-da", "--device_amount").int().required()

    @Deprecated("api option will be deleted soon. Use groupId")
    val api by option("-a", "--api")
    val groupId by option("-g", "--group_id")
    val runCommand by option("-rc", "--run_command")
    val allure by option("-aw", "--allure").flag()
    val marathon by option("-m", "--marathon").flag()
    val marathonCommand by option("-mcmd", "--marathon_command")
    val marathonConfigFilePath by option("-mc", "--marathon_config")
    val environments: Map<String, String> by option("-e", "--env").associate()
    val marathonAdbPortVariable by option("-mapv", "--marathon_adb_port_variable")
    val userAgent by option("-ua", "--user_agent")
    val deviceConnectionTimeoutMs by option("-dct", "--device_connection_timeout_ms").long()


    override fun run() {
        println(allure)
        val group = groupId ?: api ?: throw RuntimeException("Specify -g or --group_id option.")
        val farmUrls = urls?.map {
            getFarmUrlFromString(it)
        } ?: listOf(getFarmUrlFromString("http://localhost:8080"))
        val deviceTimeoutMs = deviceConnectionTimeoutMs ?: (5 * 60_000L)
        FarmClientProvider.init(
            FarmClientConfig(
                farmUrls = farmUrls,
                userAgent = userAgent ?: getGitlabProjectId() ?: "test"
            )
        )
        when (command) {
            Command.ACQUIRE -> {
                AcquireCommand(deviceAmount, group, deviceConnectionTimeoutMs = deviceTimeoutMs).execute()
            }
            else -> {
                val isSuccess = MarathonTestRunCommand(
                    deviceAmount = deviceAmount,
                    groupId = group,
                    isAllure = allure,
                    marathonConfigFilePath = marathonConfigFilePath,
                    adbPortVariable = marathonAdbPortVariable,
                    marathonCommand = marathonCommand,
                    envs = environments,
                    deviceConnectionTimeoutMs = deviceTimeoutMs
                ).execute()
                if (!isSuccess) exitProcess(1)
            }
        }
    }

    fun getGitlabProjectId(): String? {
        return EnvironmentUtils.getProcEnvironment()["CI_PROJECT_TITLE"]
    }
}

fun main(args: Array<String>) = FarmCliClient().main(args)

enum class Command { RUN, ACQUIRE, RELEASE }