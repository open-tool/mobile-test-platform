package com.atiurin.atp.farmcore.models

data class Device(
    val id: String,
    val name: String,
    val groupId: String,
    val dockerImage: String,
    val ip: String,
    val adbConnectPort: Int,
)
