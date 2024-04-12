package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmserver.config.FarmConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
class DevicePoolConfiguration @Autowired constructor(private val farmConfig: FarmConfiguration) {
    @Autowired
    lateinit var mockDevicePool: MockDevicePool

    @Autowired
    lateinit var testContainersPool: TestContainersPool

    @Bean
    fun devicePool(): DevicePool {
        return if (farmConfig.get().isMock){
            mockDevicePool
        } else {
            testContainersPool
        }
    }
}