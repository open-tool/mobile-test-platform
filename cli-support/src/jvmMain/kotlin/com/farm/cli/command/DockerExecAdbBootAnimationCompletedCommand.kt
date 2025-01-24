package com.farm.cli.command

import com.farm.cli.analyzer.CliCommandResultAnalyzer
import com.farm.cli.executor.CliCommandExecutor

class DockerExecAdbBootAnimationCompletedCommand(
    private val containerId: String,
    private val adbContainerPath: String,
) : CliCommand {
    private val executor = CliCommandExecutor(
        object : CliCommandResultAnalyzer {
            override fun analyze(result: String): Boolean {
                return result.trim() == "stopped"
            }
        }
    )

    override suspend fun execute(): CliCommandResult {
        return executor.execute("docker exec -i $containerId \"$adbContainerPath\" shell getprop init.svc.bootanim")
    }
}