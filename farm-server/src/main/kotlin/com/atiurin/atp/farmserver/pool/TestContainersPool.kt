package com.atiurin.atp.farmserver.pool

class TestContainersPool : DevicePool() {
    override fun release(deviceId: String) {
        remove(deviceId)
    }
}