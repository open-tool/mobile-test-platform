package com.atiurin.atp.farmserver.monitor

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.springframework.context.annotation.Bean

@Bean
fun meterRegistry(): MeterRegistry {
    return PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
}