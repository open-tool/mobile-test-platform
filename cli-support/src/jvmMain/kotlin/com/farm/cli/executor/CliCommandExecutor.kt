package com.farm.cli.executor

import com.farm.cli.analyzer.CliCommandResultAnalyzer
import com.farm.cli.analyzer.DummyCommandResultAnalyzer
import com.farm.cli.command.CliCommandResult
import com.farm.cli.extensions.maskSensitiveData
import com.farm.cli.log.log
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.PumpStreamHandler
import org.apache.commons.io.output.TeeOutputStream
import java.io.ByteArrayOutputStream
import kotlin.system.measureTimeMillis

class CliCommandExecutor(
    private val analyzer: CliCommandResultAnalyzer = DummyCommandResultAnalyzer()
) {
    private val executor = DefaultExecutor()

    /**
     * return exit code
     */
    fun execute(
        cmdLine: String,
        envs: Map<String, String> = emptyMap(),
        timeoutMs: Long = 5 * 60_000L
    ): CliCommandResult {
        var executionTime = 0L
        var exitCode = 0
        var message = ""
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val stdout = TeeOutputStream(System.out, outputStream)
        val stderr = TeeOutputStream(System.err, errorStream)
        val exit = runCatching {
            log.debug { "Execute CLI command '$cmdLine' with timeoutMs = $timeoutMs and envs '${envs.maskSensitiveData()}'"}
            val cmd = CommandLine.parse(cmdLine)
            executor.watchdog = ExecuteWatchdog(timeoutMs)
            executor.streamHandler = PumpStreamHandler(outputStream, errorStream)
            executionTime = measureTimeMillis {
                exitCode = executor.execute(cmd, envs)
            }
        }.onFailure { ex ->
            val processError = errorStream.toString()
            message = "Command '$cmdLine' failed: ${processError}\nexception: ${ex.message}\ncause ${ex.cause}"
            log.debug { message }
        }.onSuccess {
            message = outputStream.toString().ifBlank { errorStream.toString() }
            log.debug { "Command '$cmdLine' executed successfully in $executionTime ms. Output: \n$message" }
        }.isSuccess
        val outputToAnalyze = outputStream.toString().ifBlank {
            errorStream.toString()
        }
        val isSuccessful = analyzer.analyze(outputToAnalyze)

        return CliCommandResult(
            success = exit && exitCode == 0 && isSuccessful,
            message = message
        )
    }
}