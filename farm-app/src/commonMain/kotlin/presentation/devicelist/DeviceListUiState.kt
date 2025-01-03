package presentation.devicelist

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmcore.entity.PoolDevice

data class DeviceListUiState(
    val devices: List<PoolDevice>,
    val isLoading: Boolean,
    val error: String? = null,
    val selectedStatus: DeviceStatus? = null,
    val selectedState: DeviceState? = null,
    val selectedGroup: String? = null,
    val sortBy: SortField = SortField.Name,
    val sortAscending: Boolean = true
)
