package com.atiurin.atp.farmcore.util

import java.net.ServerSocket

object NetUtil {
    fun getFreePort(): Int {
        return try {
            ServerSocket(0).use { serverSocket ->
                assert(serverSocket.localPort > 0)
                serverSocket.localPort
            }
        } catch (e: Exception) {
            throw Exception("No free port available")
        }
    }
}