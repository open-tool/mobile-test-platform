package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.util.NetUtil
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
    var keepAliveDevicesMap: Map<String, Int> = mutableMapOf(),
    var deviceBusyTimeoutSec: Long = 30 * 60,
    var isMock: Boolean = false,
    var startPort: Int = 0,
    var endPort: Int = 65534
)

fun Config.getPortInRange() = NetUtil.getFreePortInRange(this.startPort, this.endPort)