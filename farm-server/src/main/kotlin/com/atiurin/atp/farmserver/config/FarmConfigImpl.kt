package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.models.Config
import com.atiurin.atp.farmserver.logging.log
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import javax.inject.Singleton

@Singleton
@Component
@Lazy
class FarmConfigImpl : FarmConfig {
    private val config: Config by lazy { InitialArguments.config.toConfig() }

    override fun set(block: Config.() -> Unit) {
        config.block()
        log.info { "Config initialised/updated $config" }
    }

    override fun get() = config
}
