package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.provider.DeviceProvider
import com.atiurin.atp.farmserver.provider.DeviceProviderContainer

class TestContainersPool : DevicePool() {
    override val deviceProvider: DeviceProvider = DeviceProviderContainer.deviceProvider

    override fun release(deviceId: String) {
        remove(deviceId)
    }
}