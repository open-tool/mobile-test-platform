package com.atiurin.atp.farmcore.api.response

import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse(open var success: Boolean = true, open var message: String = "") {
    override fun toString() = "success = $success, message = '$message'"
}