package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.api.response.BaseResponse
import com.atiurin.atp.farmcore.api.response.GetDevicesResponse
import com.atiurin.atp.farmcore.api.response.GetPoolDevicesResponse
import com.atiurin.atp.farmcore.entity.toApiDevice
import com.atiurin.atp.farmcore.entity.toApiPoolDevice
import com.atiurin.atp.farmcore.entity.toApiPoolDevices
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.pool.DevicePool
import com.atiurin.atp.farmserver.pool.toDevices
import com.atiurin.atp.farmserver.pool.toPoolDevice
import com.atiurin.atp.farmserver.pool.toPoolDevices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/device")
class DeviceRestController @Autowired constructor(
    private val devicePool: DevicePool
) : AbstractRestController() {

    @GetMapping("/acquire")
    fun acquire(
        @RequestParam amount: Int,
        @RequestParam groupId: String,
        @RequestParam userAgent: String
    ): GetDevicesResponse {
        return processRequest {
            log.info { "Acquire device request: amount = $amount, groupId = $groupId, userAgent = '$userAgent'" }
            val devices = devicePool.acquire(amount, groupId, userAgent)
                .toDevices()
                .map { it.toApiDevice() }
            GetDevicesResponse(devices)
        }
    }

    @GetMapping("/create")
    fun create(
        @RequestParam groupId: String,
        @RequestParam name: String
    ): GetDevicesResponse {
        return processRequest {
            log.info { "create device request: groupId = $groupId, name = '$name'" }
            val devices = devicePool.create(1, DeviceInfo(name, groupId))
                .toDevices()
                .map { it.toApiDevice() }
            GetDevicesResponse(devices = devices)
        }
    }

    @GetMapping("/list")
    fun list(): GetPoolDevicesResponse {
        log.info { "list devices request" }
        return processRequest {
            val poolDevices = devicePool.all().map { it.toPoolDevice().toApiPoolDevice() }
            GetPoolDevicesResponse(poolDevices)
        }
    }

    @GetMapping("/info")
    fun info(@RequestParam deviceIds: List<String>): GetPoolDevicesResponse {
        log.info { "list devices request" }
        return processRequest {
            val poolDevices = devicePool.all().filter { it.device.id in deviceIds }
                .toPoolDevices()
                .toApiPoolDevices()
            GetPoolDevicesResponse(poolDevices)
        }
    }

    @PostMapping("/release")
    fun release(@RequestParam deviceIds: List<String>): BaseResponse {
        log.info { "release devices request: deviceIds = $deviceIds" }
        return processRequest {
            devicePool.release(deviceIds)
            BaseResponse()
        }
    }

    @PostMapping("/release-all")
    fun releaseAll(@RequestParam groupId: String): BaseResponse {
        log.info { "release All devices in group '$groupId'" }
        return processRequest {
            devicePool.releaseAll(groupId)
            BaseResponse()
        }
    }

    @PostMapping("/remove")
    fun remove(@RequestParam deviceId: String): BaseResponse {
        log.info { "remove device request: deviceId = $deviceId" }
        return processRequest {
            devicePool.remove(deviceId)
            BaseResponse()
        }
    }

    @PostMapping("/block")
    fun block(@RequestParam deviceId: String, @RequestParam desc: String): BaseResponse {
        log.info { "Block device request: deviceId = $deviceId, desc = '$desc'" }
        return processRequest {
            devicePool.block(deviceId, desc)
            BaseResponse()
        }
    }

    @PostMapping("/unblock")
    fun unblock(@RequestParam deviceId: String): BaseResponse {
        log.info { "Unblock device request: deviceId = $deviceId" }
        return processRequest {
            devicePool.unblock(deviceId)
            BaseResponse()
        }
    }
}

