package com.atiurin.atp.farmserver.db

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(value = ["farm.mode"], havingValue = "MULTIPLE", matchIfMissing = false)
class DatasourceConfig {

    @Value("\${spring.datasource.url}")
    private lateinit var url: String

    @Value("\${spring.datasource.username}")
    private lateinit var username: String

    @Value("\${spring.datasource.password}")
    private lateinit var password: String

    @Value("\${spring.datasource.driver-class-name}")
    private lateinit var driverClassName: String

    @Bean
    @ConditionalOnProperty(value = ["farm.mode"], havingValue = "MULTIPLE", matchIfMissing = false)
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(url)
            .driverClassName(driverClassName)
            .username(username)
            .password(password)
            .build()
    }
}