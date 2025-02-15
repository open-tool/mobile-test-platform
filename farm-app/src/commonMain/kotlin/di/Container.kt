package di

import com.atiurin.atp.kmpclient.FarmClient
import com.atiurin.atp.kmpclient.FarmClientConfig
import data.DeviceRepository
import data.ServerRepository

object Container {
    fun setFarmClient(
        config: FarmClientConfig,
        doOnFailure: (String) -> Unit,
    ) {
        if (farmClient != null) return
        farmClient = FarmClient(config, doOnFailure)
    }

    private var farmClient: FarmClient? = null
    val deviceRepository by lazy {
        farmClient?.let { DeviceRepository(it) } ?: throw RuntimeException("FarmClient not initialized")
    }
    val serverRepository by lazy {
        farmClient?.let { ServerRepository(it) } ?: throw RuntimeException("FarmClient not initialized")
    }
}