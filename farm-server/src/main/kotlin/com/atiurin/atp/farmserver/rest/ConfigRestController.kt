package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.api.response.BaseResponse
import com.atiurin.atp.farmcore.api.response.GetConfigResponse
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.images.AndroidImagesConfiguration
import com.atiurin.atp.farmserver.logging.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/config")
class ConfigRestController  @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val androidImages: AndroidImagesConfiguration
): AbstractRestController() {
    @PostMapping("/group-amount")
    fun updateGroupAmount(
        @RequestParam groupId: String,
        @RequestParam amount: Int
    ): BaseResponse {
        return processRequest {
            val devicesMapToBeConfigure = farmConfig.get().keepAliveDevicesMap.toMutableMap()
            devicesMapToBeConfigure[groupId] = amount
            val sum = devicesMapToBeConfigure.values.sum()
            if (sum > farmConfig.get().maxDevicesAmount) {
                throw RuntimeException("Invalid sum off all devices in all groups = $sum, maxDevicesAmount = ${farmConfig.get().maxDevicesAmount}")
            }
            farmConfig.set {
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
            farmConfig.set {
                maxDevicesAmount = amount
            }
            BaseResponse()
        }
    }

    @PostMapping("/busy-device-timeout")
    fun updateBusyDeviceTimeout(
        @RequestParam("Timeout in seconds") timeout: Long
    ): BaseResponse {
        return processRequest {
            farmConfig.set {
                busyDeviceTimeoutSec = timeout
            }
            BaseResponse()
        }
    }

    @PostMapping("/creating-device-timeout")
    fun updateCreatingDeviceTimeout(
        @RequestParam("Timeout in seconds") timeout: Long
    ): BaseResponse {
        return processRequest {
            farmConfig.set {
                creatingDeviceTimeoutSec = timeout
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
            androidImages.update(groupId, image)
            BaseResponse()
        }
    }

    @GetMapping("/current")
    fun getCurrentConfig(): GetConfigResponse {
        return processRequest {
            log.info { "Get current config" }
            GetConfigResponse(farmConfig.get())
        }
    }
}