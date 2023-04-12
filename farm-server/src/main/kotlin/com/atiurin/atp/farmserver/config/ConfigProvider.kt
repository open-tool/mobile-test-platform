package com.atiurin.atp.farmserver.config

object ConfigProvider {
    private var config: Config = Config(0, 0, 0)

    fun set(block: Config.() -> Unit) {
        config.block()
    }

    fun get() = config
}
data class Config(
    var maxDevicesAmount: Int,
    var keepAliveDevicesAmount: Int,
    var defaultApi: Int
)