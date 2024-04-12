package com.atiurin.atp.farmserver.monitor

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Primary
@Component
@Scope("test")
class TestMonitor : MonitorInterface {
    override fun startMonitors() = Unit
}