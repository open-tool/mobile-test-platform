package com.atiurin.atp.farmserver.rest

import com.atiurin.atp.farmcore.responses.BaseResponse

abstract class AbstractRestController {
    inline fun <reified T : BaseResponse> processRequest(operation: () -> T): T {
        return try {
            operation()
        } catch (ex: Exception) {
            ex.message?.let { println(it) }
            ex.stackTrace.forEach { println(it) }
            T::class.java.getConstructor().newInstance().also { response ->
                response.success = false
                response.message = ex.message.toString()
            }
        }
    }
}