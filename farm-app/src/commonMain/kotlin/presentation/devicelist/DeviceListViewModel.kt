package presentation.devicelist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.DeviceRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class DeviceListViewModel(
    private val repository: DeviceRepository
) : ViewModel() {
    private val _state = mutableStateOf(
        DeviceListScreenState(
            devices = listOf(),
            isLoading = true
        )
    )
    val state: State<DeviceListScreenState>
        get() = _state
    init {
        getDevices()
    }
    
    fun getDevices(){
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            _state.value = _state.value.copy(error = exception.message, isLoading = false)
        }

        viewModelScope.launch(errorHandler) {
            val devices = repository.getDevices()
            _state.value = _state.value.copy(devices = devices, isLoading = false)
        }
    }
}