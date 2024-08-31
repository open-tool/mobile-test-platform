package com.atiurin.atp.farmserver.monitor

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.entity.DeviceStatus
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.pool.DevicePool
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MetricsComponent @Autowired constructor(
    private val meterRegistry: MeterRegistry,
    private val devicePool: DevicePool,
    private val farmConfig: FarmConfig
) {
    init {
        groupDevices()
    }

    private val usedDevices: Gauge =
        Gauge.builder("metrics.pool.devices.busy", this) {
            devicePool.all().count { it.status == DeviceStatus.BUSY }.toDouble()
        }.register(meterRegistry)

    private val totalFreeDevices: Gauge =
        Gauge.builder("metrics.pool.devices.free", this) {
            devicePool.all().count { it.status == DeviceStatus.FREE }.toDouble()
        }.register(meterRegistry)

    private val totalNeedRemoveDevices: Gauge =
        Gauge.builder("metrics.pool.devices.need_remove", this) {
            devicePool.all().count { it.device.state == DeviceState.NEED_REMOVE }.toDouble()
        }.register(meterRegistry)

    private val totalNeedCreateDevices: Gauge =
        Gauge.builder("metrics.pool.devices.need_create", this) {
            devicePool.all().count { it.device.state == DeviceState.NEED_CREATE }.toDouble()
        }.register(meterRegistry)

    private val totalCreatingDevices: Gauge =
        Gauge.builder("metrics.pool.devices.creating", this) {
            devicePool.all().count { it.device.state == DeviceState.CREATING }.toDouble()
        }.register(meterRegistry)

    private val totalReadyDevices: Gauge =
        Gauge.builder("metrics.pool.devices.ready", this) {
            devicePool.all().count { it.device.state == DeviceState.READY }.toDouble()
        }.register(meterRegistry)



    private fun groupDevices(): List<Gauge> {
        val gauges = mutableListOf<Gauge>()
        farmConfig.get().keepAliveDevicesMap.forEach { (groupId, _) ->
            gauges.add(
                Gauge.builder("metrics.pool.devices.$groupId.free", this) {
                    devicePool.all()
                        .count { it.device.deviceInfo.groupId == groupId && it.status == DeviceStatus.FREE }
                        .toDouble()
                }.register(meterRegistry)
            )
            gauges.add(
                Gauge.builder("metrics.pool.devices.$groupId.busy", this) {
                    devicePool.all()
                        .count { it.device.deviceInfo.groupId == groupId && it.status == DeviceStatus.FREE }
                        .toDouble()
                }.register(meterRegistry)
            )
        }
        return gauges
    }
}
