package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.entity.Config
import com.atiurin.atp.farmcore.entity.FarmMode
import com.atiurin.atp.farmserver.logging.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import javax.inject.Singleton

@Singleton
@Component
@Lazy
class FarmConfigImpl : FarmConfig {
    @Value("\${farm.mode}")
    private var farmModeProperty: String? = null

    private val configDelegate: Config by lazy {
        val farmMode = farmModeProperty?.let { mode -> FarmMode.valueOf(mode.uppercase()) } ?: FarmMode.LOCAL
        InitialArguments.config.toConfig(farmMode)
    }

    private var config: Config = configDelegate

    override fun set(block: Config.() -> Unit) {
        config.block()
        log.info { "Config initialised/updated $config" }
    }

    override fun set(config: Config) {
        this.config = config
        log.info { "Config initialised/updated $config" }
    }

    override fun get() = config
}
