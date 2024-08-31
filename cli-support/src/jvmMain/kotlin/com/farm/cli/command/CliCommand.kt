package com.farm.cli.command

interface CliCommand {
    suspend fun execute(): CliCommandResult
}