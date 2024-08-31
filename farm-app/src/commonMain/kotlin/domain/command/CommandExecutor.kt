package domain.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CommandExecutor {
    var doOnFailure: ((String) -> Unit) = {}

    suspend fun execute(commandData: CommandData, onFail: ((String) -> Unit)? = null){
        withContext(Dispatchers.Default){
            val result = executeCommand(commandData)
            if (!result.success){
                doOnFailure(result.message)
                onFail?.invoke(result.message)
            }
        }
    }
}