package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.config.FarmConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class DevicePoolConfiguration @Autowired constructor(private val farmConfig: FarmConfig) {
    @Autowired
    lateinit var mockDevicePool: MockLocalDevicePool

    @Autowired
    lateinit var localTestContainersPool: LocalTestContainersPool

    @Bean
    fun devicePool(): LocalDevicePool {
        return if (farmConfig.get().isMock){
            mockDevicePool
        } else {
            localTestContainersPool
        }
    }
}