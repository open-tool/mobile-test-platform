package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.config.ConfigProvider

object DevicePoolProvider {
    var devicePool: DevicePool = TestContainersPool()
}