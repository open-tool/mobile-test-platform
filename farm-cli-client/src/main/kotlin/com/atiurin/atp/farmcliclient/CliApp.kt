package com.atiurin.atp.farmcliclient

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import kotlin.system.exitProcess

class CliApp : CliktCommand() {
//    val url: String by option("-u", "--url").required()
    val command : Command? by option("-c", "--command").enum<Command>()
    val amount by  option("-a", "--amount").int()
    val serial by  option("-s", "--serial")

    override fun run() {
        val cmdLine = CommandLine.parse("/home/atiurin/Android/marathon-0.7.6/bin/marathon --version")
        val executor = DefaultExecutor()
        val output = executor.execute(cmdLine)

        println(output)
    }
}

fun main(args: Array<String>) = CliApp().main(args)

enum class Command { RUN, ACQUIRE, RELEASE }