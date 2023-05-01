package com.atiurin.atp.farmcore.responses

open class BaseResponse(open var success: Boolean = true, open var message: String = "") {
    override fun toString() = "success = $success, message = '$message'"
}