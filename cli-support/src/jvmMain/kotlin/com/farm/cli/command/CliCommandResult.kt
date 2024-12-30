package com.farm.cli.command

data class CliCommandResult (
    val success: Boolean,
    val message: String = ""
)