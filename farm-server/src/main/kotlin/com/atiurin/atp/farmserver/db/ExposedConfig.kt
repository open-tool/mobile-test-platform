package com.atiurin.atp.farmserver.db

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class ExposedConfig(private val dataSource: DataSource) {

    @Bean
    fun transactionManager(): PlatformTransactionManager {
        return SpringTransactionManager(dataSource)
    }

    @PostConstruct
    fun initialize() {
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(Devices, Servers)
        }
    }
}
