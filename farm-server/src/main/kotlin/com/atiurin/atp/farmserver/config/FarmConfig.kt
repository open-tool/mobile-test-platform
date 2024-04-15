package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.models.Config

interface FarmConfig {
    fun set(block: Config.() -> Unit)
    fun get() : Config
}