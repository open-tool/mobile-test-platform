package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.device.DeviceRepository
import com.atiurin.atp.farmserver.device.MockDeviceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Lazy
@Component
class MockDBDevicePool @Autowired private constructor(
    @Lazy repository: MockDeviceRepository,
) : DBDevicePool() {
    override val deviceRepository: DeviceRepository = repository

    override fun release(deviceId: String) {
        remove(deviceId)
    }
}