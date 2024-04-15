package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.repository.DeviceRepository
import com.atiurin.atp.farmserver.repository.MockDeviceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class MockDevicePool @Autowired private constructor(repository: MockDeviceRepository) : DevicePool() {
    override val deviceRepository: DeviceRepository = repository

    override fun release(deviceId: String) {
        remove(deviceId)
    }
}