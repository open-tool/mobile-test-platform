package presentation.servers

import com.atiurin.atp.farmcore.entity.ServerInfo

data class ServersListUiState(
    val servers: List<ServerInfo>,
    val isLoading: Boolean,
    val error: String? = null
)
