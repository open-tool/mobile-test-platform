package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.device.DeviceRepository
import com.atiurin.atp.farmserver.device.TestContainersDeviceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Lazy
@Component
class DBTestContainersPool @Autowired constructor(
    @Lazy repository: TestContainersDeviceRepository,
) : DBDevicePool() {
    override val deviceRepository: DeviceRepository = repository

    override fun release(deviceId: String) {
        remove(deviceId)
    }

    override fun release(deviceIds: List<String>) {
        deviceIds.forEach {
            remove(it)
        }
    }
}