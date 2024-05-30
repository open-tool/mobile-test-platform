package com.atiurin.atp.farmserver.db

import com.atiurin.atp.farmserver.logging.log
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(value = ["farm.mode"], havingValue = "MULTIPLE")
class DBInitializationConfig(
    private val dataSource: DataSource,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Bean
    fun transactionManager(): PlatformTransactionManager {
        return SpringTransactionManager(dataSource)
    }

    @PostConstruct
    fun initializeDB() {
        log.info { "Initializing database" }
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(Devices, Servers)
        }
        log.info { "Database initialized. Send event DatasourceInitializedEvent" }
        eventPublisher.publishEvent(DatasourceInitializedEvent(this))
    }
}

