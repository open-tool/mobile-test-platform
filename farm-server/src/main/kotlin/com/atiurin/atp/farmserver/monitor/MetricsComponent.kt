package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.pool.LocalDevicePool
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MetricsComponent @Autowired constructor(
    private val meterRegistry: MeterRegistry,
    private val devicePool: LocalDevicePool,
    private val farmConfig: FarmConfig
) {
    init {
        groupDevices()
    }

    private val usedDevices: Gauge =
        Gauge.builder("metrics.pool.devices.busy", this) {
            devicePool.all().count { it.isBusy }.toDouble()
        }
            .register(meterRegistry)

    private val totalFreeDevices: Gauge =
        Gauge.builder("metrics.pool.devices.free", this) {
            devicePool.all().count { !it.isBusy }.toDouble()
        }
            .register(meterRegistry)

    private fun groupDevices(): List<Gauge> {
        val gauges = mutableListOf<Gauge>()
        farmConfig.get().keepAliveDevicesMap.forEach { (groupId, _) ->
            gauges.add(
                Gauge.builder("metrics.pool.devices.$groupId.free", this) {
                    devicePool.all().count { it.device.deviceInfo.groupId == groupId && !it.isBusy }
                        .toDouble()
                }.register(meterRegistry)
            )
            gauges.add(
                Gauge.builder("metrics.pool.devices.$groupId.busy", this) {
                    devicePool.all().count { it.device.deviceInfo.groupId == groupId && it.isBusy }
                        .toDouble()
                }.register(meterRegistry)
            )
        }
        return gauges
    }
}
