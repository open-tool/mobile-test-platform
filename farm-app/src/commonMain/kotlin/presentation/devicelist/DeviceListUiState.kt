package presentation.devicelist

import com.atiurin.atp.farmcore.entity.PoolDevice

data class DeviceListUiState(
    val devices: List<PoolDevice>,
    val isLoading: Boolean,
    val error: String? = null,
    val selectedStatus: String = "All",
    val selectedState: String = "All",
    val selectedGroup: String = "All",
    val sortBy: SortField = SortField.Name,
    val sortAscending: Boolean = true
)
