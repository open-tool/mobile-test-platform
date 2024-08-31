package presentation.devicelist

import com.atiurin.atp.farmcore.entity.PoolDevice

data class DeviceListScreenState(
    val devices: List<PoolDevice>,
    val isLoading: Boolean,
    val error: String? = null
)
