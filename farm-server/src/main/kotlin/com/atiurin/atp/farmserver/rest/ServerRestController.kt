package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.api.response.GetServersResponse
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.servers.repository.ServerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/server")
class ServerRestController @Autowired constructor(
    @Qualifier("serversRepository") private val serverRepository: ServerRepository
) : AbstractRestController() {
    @GetMapping("/list")
    fun list(): GetServersResponse {
        return processRequest {
            log.info { "list servers request" }
            val servers = serverRepository.all()
            GetServersResponse(servers)
        }
    }

    @GetMapping("/alive")
    fun alive(): GetServersResponse {
        return processRequest {
            log.info { "alive servers request" }
            val servers = serverRepository.getAliveServers()
            GetServersResponse(servers)
        }
    }
}