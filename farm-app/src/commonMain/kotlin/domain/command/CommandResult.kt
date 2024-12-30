package domain.command

data class CommandResult(
    val success: Boolean,
    val message: String = ""
)
