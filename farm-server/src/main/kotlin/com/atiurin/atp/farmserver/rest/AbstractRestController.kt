package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.responses.BaseResponse
import com.atiurin.atp.farmserver.logging.log

abstract class AbstractRestController {
    inline fun <reified T : BaseResponse> processRequest(operation: () -> T): T {
        return try {
            operation()
        } catch (ex: Exception) {
            ex.message?.let { msg -> log.error{ msg } }
            ex.stackTrace.forEach { trace -> log.error{ trace } }
            T::class.java.getConstructor().newInstance().also { response ->
                response.success = false
                response.message = ex.message.toString()
            }
        }
    }
}