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

/**
 * Class for executing CLI commands with subsequent result analysis.
 *
 * This class utilizes [DefaultExecutor] from the Apache Commons Exec library to run
 * command line commands. The execution results are captured from both the standard and error
 * streams and are then analyzed using the provided instance of [CliCommandResultAnalyzer].
 * If needed, the output is also duplicated to the system streams (stdout and stderr) when the
 * [addSystemOut] parameter is set to `true`.
 *
 * @property analyzer The analyzer for the command execution results. By default, [DummyCommandResultAnalyzer] is used.
 * @property addSystemOut Flag indicating whether to duplicate the command output to the system streams.
 */
class CliCommandExecutor(
    private val analyzer: CliCommandResultAnalyzer = DummyCommandResultAnalyzer(),
    private val addSystemOut: Boolean = false
) {
    private val executor = DefaultExecutor()

    /**
     * Executes the specified CLI command with the given environment variables and timeout.
     *
     * The command is executed using [DefaultExecutor]. The output (both standard and error)
     * is captured in separate streams. If [addSystemOut] is set to `true`, the output is additionally
     * directed to the system streams (stdout/stderr).
     *
     * After execution, the result is analyzed using [analyzer]. The execution is considered successful if:
     * - The exit code is 0,
     * - No exceptions occurred during execution,
     * - The [analyzer] confirmed the validity of the output.
     *
     * @param cmdLine The command line to be executed.
     * @param envs A map of environment variables to be applied during command execution (default is empty).
     * @param timeoutMs Maximum execution time for the command in milliseconds (default is 5 minutes).
     * @return An instance of [CliCommandResult] containing the success status and the output message.
     */
    fun execute(
        cmdLine: String,
        envs: Map<String, String> = emptyMap(),
        timeoutMs: Long = 5 * 60_000L
    ): CliCommandResult {
        var executionTime = 0L
        var exitCode = -1
        var message = ""
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val isExecutedSuccessful = runCatching {
            log.debug { "Execute CLI command '$cmdLine' with timeoutMs = $timeoutMs and envs '${envs.maskSensitiveData()}'" }
            val cmd = CommandLine.parse(cmdLine)
            executor.watchdog = ExecuteWatchdog(timeoutMs)
            executor.streamHandler = when (addSystemOut) {
                false -> PumpStreamHandler(outputStream, errorStream)
                true -> {
                    PumpStreamHandler(
                        TeeOutputStream(System.out, outputStream),
                        TeeOutputStream(System.err, errorStream)
                    )
                }
            }
            executionTime = measureTimeMillis {
                exitCode = executor.execute(cmd, envs)
            }
        }.onFailure { ex ->
            val processError = errorStream.toString()
            message =
                "Command '$cmdLine' failed: ${processError}\nexception: ${ex.message}\ncause ${ex.cause}"
            log.debug { message }
        }.onSuccess {
            message = outputStream.toString().ifBlank { errorStream.toString() }
            log.debug { "Command '$cmdLine' executed successfully in $executionTime ms. Output: \n$message" }
        }.isSuccess
        val analyzeResult = runCatching {
            val outputToAnalyze = outputStream.toString().ifBlank {
                errorStream.toString()
            }
            analyzer.analyze(outputToAnalyze)
        }
        val isAnalyzedSuccessful = analyzeResult.isSuccess && analyzeResult.getOrDefault(false)

        return CliCommandResult(
            success = exitCode == 0 && isExecutedSuccessful && isAnalyzedSuccessful,
            message = message
        )
    }
}