package domain.command

import com.farm.cli.command.ConnectDeviceCommand
import com.farm.cli.command.DisconnectDeviceCommand

actual fun getPlatformCommands(): List<Command> = Command.entries

actual suspend fun executeCommand(commandData: CommandData): CommandResult {
    println("Try to execute command $commandData")
    val result = when (commandData){
        is CommandData.Connect -> {
            ConnectDeviceCommand(adbServerPort = null, device = commandData.device).execute()
        }
        is CommandData.Disconnect -> {
            DisconnectDeviceCommand(adbServerPort = null, device = commandData.device).execute()
        }
        is CommandData.Block -> {
            DisconnectDeviceCommand(adbServerPort = null, device = commandData.device).execute()
        }
        else -> throw IllegalArgumentException("Not supported command $commandData")
    }
    return CommandResult(success = result.success, message = result.message)
}