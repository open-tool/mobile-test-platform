package data

import com.atiurin.atp.farmcore.entity.PoolDevice
import com.atiurin.atp.kmpclient.FarmClient

class DeviceRepository(private val farmClient: FarmClient) {
    suspend fun getDevices(): List<PoolDevice> {
        return farmClient.list()
    }

    suspend fun getDeviceInfo(deviceId: String): PoolDevice {
        return farmClient.info(listOf(deviceId)).first()
    }

    suspend fun blockDevice(deviceId: String) {
        farmClient.block(deviceId, farmClient.getUserAgent())
    }

    suspend fun unblockDevice(deviceId: String) {
        farmClient.unblock(deviceId)
    }
}