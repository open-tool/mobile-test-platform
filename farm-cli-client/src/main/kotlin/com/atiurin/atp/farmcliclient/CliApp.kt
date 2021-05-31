package com.atiurin.atp.farmcliclient

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int

class CliApp : CliktCommand() {
    val url: String by argument()
    val command : Command? by option("-c", "--command").enum<Command>()
    val amount by  option("-a", "--amount").int()
    val serial by  option("-s", "--serial")

    override fun run() {
        TODO("Not yet implemented")
    }
}

fun main(args: Array<String>) = CliApp().main(args)

enum class Command { CONNECT, DISCONNECT, DISCONNECT_ALL }