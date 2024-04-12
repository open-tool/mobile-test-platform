package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.models.Config
import com.atiurin.atp.farmcore.util.NetUtil
import com.atiurin.atp.farmserver.logging.log

object ConfigProvider {
    private var config: Config = Config()

    fun set(block: Config.() -> Unit) {
        config.block()
        log.info { "Config initialised/updated $config" }
    }

    fun get() = config
}
