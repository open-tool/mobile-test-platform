package domain.command

import com.atiurin.atp.farmcore.entity.Device

enum class Command {
    CONNECT, DISCONNECT, BLOCK, UNBLOCK
}

sealed interface CommandData {
    data class Connect(val device: Device): CommandData
    data class Disconnect(val device: Device): CommandData
    data class Block(val device: Device): CommandData
    data class Unblock(val device: Device): CommandData
}

expect fun getPlatformCommands() : List<Command>

expect suspend fun executeCommand(commandData: CommandData) : CommandResult
