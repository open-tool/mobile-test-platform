package com.atiurin.atp.farmserver.db

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource


@Configuration
@ConditionalOnProperty(value = ["farm.mode"], havingValue = "LOCAL")
class CacheInitializationConfig(
    private val eventPublisher: ApplicationEventPublisher
) {
    @PostConstruct
    fun sendDataSourceEvent() {
        eventPublisher.publishEvent(DatasourceInitializedEvent(this))
    }
}