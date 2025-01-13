package presentation.devicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atiurin.atp.farmcore.entity.PoolDevice
import data.DeviceRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeviceListViewModel(
    private val repository: DeviceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceListUiState(
        devices = listOf(),
        isLoading = true
    ))
    val uiState: StateFlow<DeviceListUiState> = _uiState

    init {
        getDevices()
    }

    fun getDevices() {
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            _uiState.update { it.copy(isLoading = false, error = exception.message) }
        }

        viewModelScope.launch(errorHandler) {
            _uiState.update { it.copy(isLoading = true) }
            val devices = repository.getDevices()
            updateDevices(devices)
        }
    }

    fun updateDevices(devices: List<PoolDevice>) {
        _uiState.update { it.copy(devices = devices, isLoading = false) }
    }

    fun updateSelectedStatus(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun updateSelectedState(state: String) {
        _uiState.update { it.copy(selectedState = state) }
    }

    fun updateSelectedGroup(group: String) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun updateSortField(sortField: SortField) {
        _uiState.update { current ->
            val newAscending = if (current.sortBy == sortField) !current.sortAscending else true
            current.copy(sortBy = sortField, sortAscending = newAscending)
        }
    }

}