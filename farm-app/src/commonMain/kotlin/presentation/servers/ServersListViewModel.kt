package presentation.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atiurin.atp.farmcore.entity.ServerInfo
import data.ServerRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServersListViewModel(private val repository: ServerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ServersListUiState(
            servers = listOf(),
            isLoading = true
        )
    )
    val uiState: StateFlow<ServersListUiState> = _uiState

    init {
        getServers()
    }

    fun getServers() {
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            _uiState.update { it.copy(isLoading = false, error = exception.message) }
        }
        viewModelScope.launch(errorHandler) {
            _uiState.update { it.copy(isLoading = true) }
            val devices = repository.getList()
            updateServers(devices)
        }
    }

    private fun updateServers(servers: List<ServerInfo>) {
        _uiState.update { it.copy(servers = servers, isLoading = false) }
    }
}