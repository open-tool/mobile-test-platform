package presentation.devicedetails

import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmcore.entity.PoolDevice
import domain.command.Command
import domain.command.getPlatformCommands

data class DeviceDetailsScreenState(
    val device: PoolDevice? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val buttonEnabledMap: MutableMap<String, Boolean> = mutableMapOf()
){
    init {
        val commands = getPlatformCommands()
        commands.forEach { command ->
            if (command == Command.CONNECT) {
                buttonEnabledMap[command.name] = if (device?.status != DeviceStatus.FREE) {
                     false
                } else  {
                    true
                }
            } else {
                buttonEnabledMap[command.name] = true
            }
        }
    }
}