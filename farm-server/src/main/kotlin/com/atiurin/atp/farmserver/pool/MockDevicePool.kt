package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.provider.DeviceProvider
import com.atiurin.atp.farmserver.provider.MockDeviceProvider

class MockDevicePool : DevicePool() {
    override val deviceProvider: DeviceProvider = MockDeviceProvider()
}