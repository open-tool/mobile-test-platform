package com.atiurin.atp.farmcore.util

import java.net.InetAddress
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

    fun getFreePortInRange(startPort: Int, endPort: Int = 65534): Int {
        for (port in startPort..endPort) {
            try {
                val assignedPort = ServerSocket(port).use { serverSocket ->
                    assert(serverSocket.localPort > 0)
                    serverSocket.localPort
                }
                return assignedPort
            } catch (ex: Exception) {
                // Port is not available, continue to the next one
            }
        }
        throw IllegalStateException("No available port in the specified range")
    }

    fun getLocalhostName(): String? = try {
        InetAddress.getLocalHost().hostName
    } catch (e: Exception) {
        null
    }
}