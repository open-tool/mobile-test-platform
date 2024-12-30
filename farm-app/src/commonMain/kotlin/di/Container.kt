package di

import com.atiurin.atp.kmpclient.FarmClient
import com.atiurin.atp.kmpclient.FarmClientConfig
import data.DeviceRepository

object Container {
    fun setFarmClient(
        config: FarmClientConfig,
        doOnFailure: (String) -> Unit
    ){
        if (farmClient != null) return
        farmClient = FarmClient(config, doOnFailure)
    }
    private var farmClient: FarmClient? = null
    val deviceRepository by lazy {
        farmClient?.let { DeviceRepository(it) } ?: throw RuntimeException("FarmClient not initialized")
    }
}