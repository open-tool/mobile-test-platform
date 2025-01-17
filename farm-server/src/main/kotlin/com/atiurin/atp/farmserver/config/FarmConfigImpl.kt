package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.entity.Config
import com.atiurin.atp.farmcore.entity.FarmMode
import com.atiurin.atp.farmserver.logging.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import javax.inject.Singleton

@Singleton
@Component
@Lazy
@ConfigurationProperties
class FarmConfigImpl : FarmConfig {

    @Value("\${farm.mode}")
    private var farmModeProperty: String? = null

    private var _config: Config? = null
        get() {
            if (field == null) {
                val farmMode = farmModeProperty?.uppercase()?.let(FarmMode::valueOf) ?: FarmMode.LOCAL
                field = InitialArguments.config.toConfig(farmMode)
            }
            return field
        }
        set(value) {
            field = value
        }

    override fun set(block: Config.() -> Unit) {
        _config?.apply(block)
        log.info { "Config initialized/updated: $_config" }
    }

    override fun set(config: Config) {
        _config = config
        log.info { "Config initialized/updated: $config" }
    }

    override fun get(): Config = _config ?: throw IllegalStateException("Config not initialized")
}
