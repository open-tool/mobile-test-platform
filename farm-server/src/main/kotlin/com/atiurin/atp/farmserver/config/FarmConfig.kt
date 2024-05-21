package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.models.Config
import javax.inject.Singleton

@Singleton
interface FarmConfig {
    fun set(block: Config.() -> Unit)
    fun get() : Config
}