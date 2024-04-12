package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.responses.BaseResponse
import com.atiurin.atp.farmcore.responses.GetConfigResponse
import com.atiurin.atp.farmserver.config.ConfigProvider
import com.atiurin.atp.farmserver.images.AndroidImage
import com.atiurin.atp.farmserver.logging.log
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/config")
class ConfigRestController : AbstractRestController() {
    @PostMapping("/group-amount")
    fun updateGroupAmount(
        @RequestParam groupId: String,
        @RequestParam amount: Int
    ): BaseResponse {
        return processRequest {
            val devicesMapToBeConfigure = ConfigProvider.get().keepAliveDevicesMap.toMutableMap()
            devicesMapToBeConfigure[groupId] = amount
            val sum = devicesMapToBeConfigure.values.sum()
            if (sum > ConfigProvider.get().maxDevicesAmount) {
                throw RuntimeException("Invalid sum off all devices in all groups = $sum, maxDevicesAmount = ${ConfigProvider.get().maxDevicesAmount}")
            }
            ConfigProvider.set {
                keepAliveDevicesMap[groupId] = amount
            }
            BaseResponse()
        }
    }

    @PostMapping("/max-devices-amount")
    fun updateMaxDevices(
        @RequestParam amount: Int
    ): BaseResponse {
        return processRequest {
            ConfigProvider.set {
                maxDevicesAmount = amount
            }
            BaseResponse()
        }
    }

    @PostMapping("/device-busy-timeout")
    fun updateDeviceBusyTimeout(
        @RequestParam("Timeout in seconds") timeout: Long
    ): BaseResponse {
        return processRequest {
            ConfigProvider.set {
                deviceBusyTimeoutSec = timeout
            }
            BaseResponse()
        }
    }

    @PostMapping("/android-image")
    fun updateGroupImage(
        @RequestParam groupId: String,
        @RequestParam image: String
    ): BaseResponse {
        return processRequest {
            AndroidImage.update(groupId, image)
            BaseResponse()
        }
    }

    @GetMapping("/current")
    fun getCurrentConfig(): GetConfigResponse {
        return processRequest {
            log.info { "Get current config" }
            GetConfigResponse(ConfigProvider.get())
        }
    }
}