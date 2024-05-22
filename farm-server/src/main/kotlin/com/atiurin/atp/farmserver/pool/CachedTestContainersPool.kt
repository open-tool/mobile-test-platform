package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.device.DeviceRepository
import com.atiurin.atp.farmserver.device.TestContainersDeviceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CachedTestContainersPool @Autowired constructor(repository: TestContainersDeviceRepository) : CachedDevicePool() {
    override val deviceRepository: DeviceRepository = repository

    override fun release(deviceId: String) {
        remove(deviceId)
    }
}