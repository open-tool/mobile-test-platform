package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.responses.BaseResponse
import com.atiurin.atp.farmcore.responses.GetDevicesResponse
import com.atiurin.atp.farmcore.responses.GetPoolDevicesResponse
import com.atiurin.atp.farmserver.*
import com.atiurin.atp.farmserver.images.AndroidImage
import com.atiurin.atp.farmserver.pool.DevicePool
import com.atiurin.atp.farmserver.pool.DevicePoolProvider
import com.atiurin.atp.farmserver.pool.toPoolDevice
import com.atiurin.atp.farmserver.provider.DeviceProvider
import com.atiurin.atp.farmserver.provider.TestContainersDeviceProvider
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/device")
class DeviceRestController : AbstractRestController() {
    private final val devicePool: DevicePool = DevicePoolProvider.devicePool
    val deviceProvider: DeviceProvider = devicePool.deviceProvider

    @GetMapping("/acquire")
    fun acquire(
        @RequestParam amount: Int,
        @RequestParam api: Int,
        @RequestParam userAgent: String
    ): GetDevicesResponse {
        return processRequest {
            val devices = devicePool.acquire(amount, api, userAgent).map { it.toDevice() }
            GetDevicesResponse(devices)
        }
    }

    @GetMapping("/create")
    fun create(
        @RequestParam api: Int,
        @RequestParam name: String
    ): GetDevicesResponse {
        return processRequest {
            val device = deviceProvider.createDevice(DeviceInfo(name, api))
            devicePool.join(device)
            GetDevicesResponse(devices = listOf(device.toDevice()))
        }
    }

    @GetMapping("/createAsync")
    fun createAsync(
        @RequestParam api: Int,
        @RequestParam name: String
    ): BaseResponse {
        //TODO support async device creation
        return processRequest {
            devicePool.join(deviceProvider.createDevice(DeviceInfo(name, api)))
            BaseResponse()
        }
    }

    @GetMapping("/list")
    fun list(): GetPoolDevicesResponse {
        return processRequest {
            val poolDevices = devicePool.all().map { it.toPoolDevice() }
            GetPoolDevicesResponse(poolDevices)
        }
    }

    @PostMapping("/release")
    fun release(@RequestParam deviceIds: List<String>): BaseResponse {
        return processRequest {
            devicePool.release(deviceIds)
            BaseResponse()
        }
    }

    @PostMapping("/remove")
    fun remove(@RequestParam deviceId: String): BaseResponse {
        return processRequest {
            devicePool.remove(deviceId)
            BaseResponse()
        }
    }
}