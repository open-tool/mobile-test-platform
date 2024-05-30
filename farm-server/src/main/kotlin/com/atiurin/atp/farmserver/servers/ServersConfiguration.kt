package com.atiurin.atp.farmserver.servers

import com.atiurin.atp.farmcore.models.FarmMode
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.servers.repository.DBServerRepository
import com.atiurin.atp.farmserver.servers.repository.MockServersRepository
import com.atiurin.atp.farmserver.servers.repository.ServerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class ServersConfiguration @Autowired constructor(private val farmConfig: FarmConfig) {
    @Autowired
    lateinit var mockServersRepository: MockServersRepository

    @Autowired
    lateinit var dbServerRepository: DBServerRepository

    @Bean
    fun serversRepository(): ServerRepository {
        return when (farmConfig.get().farmMode) {
            FarmMode.LOCAL -> mockServersRepository
            FarmMode.MULTIPLE -> dbServerRepository
            FarmMode.CLUSTER -> {
                throw IllegalStateException("Cluster farm mode is not supported yet")
            }
        }
    }
}