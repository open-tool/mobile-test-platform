package com.atiurin.atp.farmserver.pool

import com.atiurin.atp.farmcore.entity.FarmMode
import com.atiurin.atp.farmserver.config.FarmConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Lazy
@Component
class DevicePoolConfiguration @Autowired constructor(@Lazy val farmConfig: FarmConfig) {
    @Autowired
    lateinit var mockDevicePool: MockCachedDevicePool

    @Autowired
    lateinit var localTestContainersPool: CachedTestContainersPool

    @Autowired
    lateinit var mockDBDevicePool: MockDBDevicePool

    @Autowired
    lateinit var dbTestContainersPool: DBTestContainersPool

    @Bean
    fun devicePool(): DevicePool {
        val config = farmConfig.get()
        val p = when  {
            config.farmMode == FarmMode.LOCAL && config.isMock ->  mockDevicePool
            config.farmMode == FarmMode.LOCAL && !config.isMock ->  localTestContainersPool
            config.farmMode == FarmMode.MULTIPLE && config.isMock -> mockDBDevicePool
            config.farmMode == FarmMode.MULTIPLE && !config.isMock -> dbTestContainersPool
            else -> throw IllegalStateException("Unknown farm mode")
        }
        return p
    }
}