package com.atiurin.atp.farmserver.db

import com.atiurin.atp.farmcore.models.FarmMode
import com.atiurin.atp.farmserver.config.FarmConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataSourceConfiguration @Autowired constructor(val farmConfig: FarmConfig){
    @Bean
    fun hikariDataSource(): DataSource {
        if (farmConfig.get().farmMode == FarmMode.Local) {
            return DataSource()
        }
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/postgres_db"
            driverClassName = "org.postgresql.Driver"
            username = "postgres"
            password = "postgres_pw"
        }
        val dataSource = HikariDataSource(config)
        val db = Database.connect(dataSource)
        SchemaUtils.create(Devices)
        return DataSource(db)
    }
}