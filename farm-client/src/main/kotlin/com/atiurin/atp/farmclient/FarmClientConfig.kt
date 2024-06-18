package com.atiurin.atp.farmclient

data class FarmClientConfig(
    val farmUrls: List<String>,
    var userAgent: String,
    var connectionTimeoutMs: Long = Const.CONNECTION_TIMEOUT,
    var readTimeoutMs: Long = Const.READ_TIMEOUT
)
