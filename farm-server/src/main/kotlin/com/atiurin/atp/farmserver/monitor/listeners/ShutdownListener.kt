package com.atiurin.atp.farmserver.monitor.listeners

import com.atiurin.atp.farmserver.servers.repository.LocalServerRepository
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component

@Component
class ShutdownListener(
    private val localServerRepository: LocalServerRepository
) : ApplicationListener<ContextClosedEvent> {
    override fun onApplicationEvent(event: ContextClosedEvent) {
        localServerRepository.unregister()
    }
}