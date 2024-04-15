package com.atiurin.atp.farmserver.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FarmConfigConfiguration {
    @Autowired
    lateinit var configImpl: FarmConfigImpl
    @Bean
    fun farmConfig(): FarmConfig {
        return configImpl
    }
}