package com.atiurin.atp.farmcore.logger

import mu.KotlinLogging

object LoggerFactory {
    fun logger(name: String) = KotlinLogging.logger(name)
    inline fun <reified T> logger(): mu.KLogger = KotlinLogging.logger(T::class.qualifiedName ?: "Unknown")
}