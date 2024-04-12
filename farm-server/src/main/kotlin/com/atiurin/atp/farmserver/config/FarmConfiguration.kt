package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.models.Config
import com.atiurin.atp.farmserver.logging.log
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import javax.inject.Singleton

@Singleton
@Component
@Lazy
class FarmConfiguration {
    private var config: Config = InitialArguments.config.toConfig()

    fun set(block: Config.() -> Unit) {
        config.block()
        log.info { "Config initialised/updated $config" }
    }

    fun get() = config
}
