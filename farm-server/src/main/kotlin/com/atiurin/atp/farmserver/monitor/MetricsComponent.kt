package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmserver.pool.DevicePoolProvider
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MetricsComponent @Autowired constructor(private val meterRegistry: MeterRegistry) {
    private val usedDevices: Gauge =
        Gauge.builder("metrics.pool.devices.busy", this) { DevicePoolProvider.devicePool.count { it.isBusy }.toDouble() }
        .register(meterRegistry)

    private val freeDevices: Gauge =
        Gauge.builder("metrics.pool.devices.free", this) { DevicePoolProvider.devicePool.count { !it.isBusy }.toDouble() }
        .register(meterRegistry)
}
