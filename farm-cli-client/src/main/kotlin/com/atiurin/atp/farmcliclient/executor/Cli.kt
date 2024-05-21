package com.atiurin.atp.farmcliclient.executor

import com.atiurin.atp.farmcliclient.extensions.maskSensitiveData
import com.atiurin.atp.farmcliclient.log
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.PumpStreamHandler
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
    ): Boolean {
        var executionTime = 0L
        var exitCode = 0
        val exit =  runCatching {
            log.info { "Execute CLI command '$cmdLine' with timeoutMs = $timeoutMs and envs '${envs.maskSensitiveData()}'"}
            val cmd = CommandLine.parse(cmdLine)
            executor.watchdog = ExecuteWatchdog(timeoutMs)
            executionTime = measureTimeMillis {
                exitCode = executor.execute(cmd, envs)
            }
        }.onFailure {
            log.error { "Command execution failed: ${it.message}, cause ${it.cause}" }
        }.onSuccess {
            log.error { "Command '$cmdLine' executed successfully in $executionTime ms" }
        }.isSuccess

        return exit && exitCode == 0
    }
}