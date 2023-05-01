package com.atiurin.atp.farmserver.node

import org.springframework.data.annotation.Id

data class Node(
    @Id val id: String? = null,
    val url: String
)
