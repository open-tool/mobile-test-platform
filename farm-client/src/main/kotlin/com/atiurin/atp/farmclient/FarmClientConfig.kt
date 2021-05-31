package com.atiurin.atp.farmclient

import com.atiurin.atp.farmclient.Const

data class FarmClientConfig(
    val farmUrl: String,
    var userAgent: String,
    var connectionTimeoutMs: Long = Const.CONNECTION_TIMEOUT,
    var readTimeoutMs: Long = Const.READ_TIMEOUT
)
