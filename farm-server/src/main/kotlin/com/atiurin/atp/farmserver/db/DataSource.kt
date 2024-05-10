package com.atiurin.atp.farmserver.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataSource {
    @Bean
    fun initDatabaseConnection(){
        Database.connect(
            url="jdbc:postgresql://localhost:49153/farmdb",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgrespw"
        )
        SchemaUtils.create(Devices)
    }
}