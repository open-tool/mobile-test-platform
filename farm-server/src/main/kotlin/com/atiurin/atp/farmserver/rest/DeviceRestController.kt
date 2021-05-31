package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.responses.BaseResponse
import com.atiurin.atp.farmcore.responses.GetDevicesResponse
import com.atiurin.atp.farmcore.responses.GetPoolDevicesResponse
import com.atiurin.atp.farmserver.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/device")
class DeviceRestController : AbstractRestController() {
    @GetMapping("/acquire")
    fun acquire(
        @RequestParam amount: Int,
        @RequestParam api: Int,
        @RequestParam userAgent: String
    ): GetDevicesResponse {
        return processRequest {
            val devices = DevicePool.acquire(amount, api, userAgent).map { it.toDevice() }
            GetDevicesResponse(devices)
        }
    }

    @GetMapping("/create")
    fun create(
        @RequestParam api: Int,
        @RequestParam name: String
    ): GetDevicesResponse {
        return processRequest {
            val device = createDevice(DeviceInfo(name, api, getDeviceImageForApi(api)))
            DevicePool.join(device)
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
            DevicePool.join(createDevice(DeviceInfo(name, api, getDeviceImageForApi(api))))
            BaseResponse()
        }
    }

    @GetMapping("/list")
    fun list(): GetPoolDevicesResponse {
        return processRequest {
            val poolDevices = DevicePool.all().map { it.toPoolDevice() }
            GetPoolDevicesResponse(poolDevices)
        }
    }

//    @PostMapping("/release")
//    fun release(@RequestParam deviceId: String): BaseResponse {
//        return processRequest {
//            DevicePool.release(deviceId)
//            BaseResponse()
//        }
//    }

    @PostMapping("/release")
    fun release(@RequestParam deviceIds: List<String>): BaseResponse {
        return processRequest {
            DevicePool.release(deviceIds)
            BaseResponse()
        }
    }

    @PostMapping("/remove")
    fun remove(@RequestParam deviceId: String): BaseResponse {
        return processRequest {
            DevicePool.remove(deviceId)
            BaseResponse()
        }
    }
}