package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.responses.BaseResponse
import com.atiurin.atp.farmcore.responses.GetDevicesResponse
import com.atiurin.atp.farmcore.responses.GetPoolDevicesResponse
import com.atiurin.atp.farmserver.*
import com.atiurin.atp.farmserver.device.DeviceInfo
import com.atiurin.atp.farmserver.device.toDevice
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.pool.LocalDevicePool
import com.atiurin.atp.farmserver.pool.toPoolDevice
import com.atiurin.atp.farmserver.repository.DeviceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/device")
class DeviceRestController @Autowired constructor(
    private val devicePool: LocalDevicePool
) : AbstractRestController() {
    val deviceRepository: DeviceRepository
        get() = devicePool.deviceRepository

    @GetMapping("/acquire")
    fun acquire(
        @RequestParam amount: Int,
        @RequestParam groupId: String,
        @RequestParam userAgent: String
    ): GetDevicesResponse {
        return processRequest {
            log.info { "Acquire device request: amount = $amount, groupId = $groupId, userAgent = '$userAgent'" }
            val devices = devicePool.acquire(amount, groupId, userAgent).map { it.toDevice() }
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
            val device = deviceRepository.createDevice(DeviceInfo(name, groupId))
            devicePool.join(device)
            GetDevicesResponse(devices = listOf(device.toDevice()))
        }
    }

    @GetMapping("/createAsync")
    fun createAsync(
        @RequestParam groupId: String,
        @RequestParam name: String
    ): BaseResponse {
        //TODO support async device creation
        return processRequest {
            log.info { "create async device request: groupId = $groupId, name = '$name'" }
            devicePool.join(deviceRepository.createDevice(DeviceInfo(name, groupId)))
            BaseResponse()
        }
    }

    @GetMapping("/list")
    fun list(): GetPoolDevicesResponse {
        log.info { "list devices request" }
        return processRequest {
            val poolDevices = devicePool.all().map { it.toPoolDevice() }
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
        log.info { "remove device request: deviceIds = $deviceId" }
        return processRequest {
            devicePool.remove(deviceId)
            BaseResponse()
        }
    }
}