package domain.command

actual fun getPlatformCommands(): List<Command> = listOf(Command.BLOCK, Command.UNBLOCK)

actual suspend fun executeCommand(commandData: CommandData): CommandResult {
    return CommandResult(false, "Command isn't supported on web")
}