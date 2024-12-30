package presentation.devicedetails

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atiurin.atp.farmcore.entity.Device
import data.DeviceRepository
import domain.command.CommandData
import domain.command.CommandExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceDetailsViewModel(
    private val repository: DeviceRepository,
    private val deviceUid: String,
) : ViewModel(){
    private val _state = mutableStateOf(
        DeviceDetailsScreenState(
            device = null,
            isLoading = true
        )
    )

    private fun getDeviceState(){
        viewModelScope.launch {
            val device = repository.getDeviceInfo(deviceUid)
            _state.value = _state.value.copy(device = device, isLoading = false)
        }
    }

    val state: State<DeviceDetailsScreenState>
        get() = _state

    init {
        getDeviceState()
    }

    fun connectDevice(device: Device){
        _state.value = _state.value.copy(isLoading = true)
        blockDevice(device.id)
        viewModelScope.launch {
            CommandExecutor.execute(commandData = CommandData.Connect(device))
            getDeviceState()
        }
    }

    fun disconnectDevice(device: Device){
        unblockDevice(device.id)
        viewModelScope.launch {
            withContext(Dispatchers.Default){
                CommandExecutor.execute(CommandData.Disconnect(device))
                getDeviceState()
            }
        }
    }

    fun blockDevice(deviceId: String){
        viewModelScope.launch {
            withContext(Dispatchers.Default){
                repository.blockDevice(deviceId)
                getDeviceState()
            }
        }
    }

    fun unblockDevice(deviceId: String){
        viewModelScope.launch {
            withContext(Dispatchers.Default){
                repository.unblockDevice(deviceId)
                getDeviceState()
            }
        }
    }
}