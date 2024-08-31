package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.entity.Config
import com.atiurin.atp.farmserver.util.NetUtil
import javax.inject.Singleton

@Singleton
interface FarmConfig {
    fun set(block: Config.() -> Unit)
    fun get() : Config
    fun getPortInRange() = NetUtil.getFreePortInRange(get().startPort, get().endPort)
}