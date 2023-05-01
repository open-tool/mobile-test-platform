package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmserver.logging.log

object ConfigProvider {
    private var config: Config = Config()

    fun set(block: Config.() -> Unit) {
        config.block()
        log.info { "Config initialised $config" }
    }

    fun get() = config
}
data class Config(
    var maxDevicesAmount: Int = 0,
    var keepAliveDevicesAmount: Int = 0,
    var defaultApi: Int = 0,
    var deviceBusyTimeoutSec: Long = 30 * 60,
    var isMock: Boolean = false
)