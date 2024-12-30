package com.farm.cli.executor

import com.farm.cli.command.CliCommandResult
import com.farm.cli.extensions.maskSensitiveData
import com.farm.cli.log.log
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.PumpStreamHandler
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.system.measureTimeMillis

object Cli {
    private val executor = DefaultExecutor()

    /**
     * return exit code
     */
    fun execute(
        cmdLine: String,
        envs: Map<String, String> = emptyMap(),
        timeoutMs: Long = 30_000_000L
    ): CliCommandResult {
        var executionTime = 0L
        var exitCode = 0
        var message = ""
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val exit = runCatching {
            log.info { "Execute CLI command '$cmdLine' with timeoutMs = $timeoutMs and envs '${envs.maskSensitiveData()}'"}
            val cmd = CommandLine.parse(cmdLine)
            executor.watchdog = ExecuteWatchdog(timeoutMs)
            executor.streamHandler = PumpStreamHandler(outputStream, errorStream)
            executionTime = measureTimeMillis {
                exitCode = executor.execute(cmd, envs)
            }
        }.onFailure { ex ->
            val processError = errorStream.toString()

            message = "Command failed: ${processError}\nexception: ${ex.message}\ncause ${ex.cause}"
            println(message)
            log.error { message }
        }.onSuccess {
            message = outputStream.toString().ifBlank { errorStream.toString() }
            log.error { "Command '$cmdLine' executed successfully in $executionTime ms" }
        }.isSuccess

        return CliCommandResult(
            success = exit && exitCode == 0,
            message = message
        )
    }
}

class CliInputStream : InputStream() {
    override fun read(): Int {
        return this.read()
    }
}