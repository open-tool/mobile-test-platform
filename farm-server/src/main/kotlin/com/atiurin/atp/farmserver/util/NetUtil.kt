package com.atiurin.atp.farmserver.util

import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

object NetUtil {
    // Thread-safe set to store used ports
    private val usedPorts: MutableSet<Int> = ConcurrentHashMap.newKeySet()

    fun getFreePort(): Int {
        // Infinite loop until a new free port is found
        while (true) {
            try {
                ServerSocket(0).use { serverSocket ->
                    val port = serverSocket.localPort
                    // returns true if the port was not already added.
                    if (usedPorts.add(port)) {
                        return port
                    }
                }
            } catch (e: Exception) {
                throw Exception("No free ports available")
            }
        }
    }

    fun getFreePortInRange(startPort: Int = 1024, endPort: Int = 65534): Int {
        val totalPorts = endPort - startPort + 1
        // If the number of used ports is equal to or exceeds the total range, no free port is available.
        if (usedPorts.size >= totalPorts) {
            throw IllegalStateException("No available port in the specified range")
        }
        // Attempt to select a random port from the range
        for (i in 0 until totalPorts) {
            val portCandidate = ThreadLocalRandom.current().nextInt(startPort, endPort + 1)
            if (usedPorts.contains(portCandidate)) continue
            try {
                ServerSocket(portCandidate).use { serverSocket ->
                    if (serverSocket.localPort > 0) {
                        usedPorts.add(portCandidate)
                        return portCandidate
                    }
                }
            } catch (ex: Exception) {
                // Port is busy or unavailable, try the next one
            }
        }
        // Fallback: sequential scanning of ports if random selection did not yield a result
        for (port in startPort..endPort) {
            if (usedPorts.contains(port)) continue
            try {
                ServerSocket(port).use { serverSocket ->
                    if (serverSocket.localPort > 0) {
                        usedPorts.add(port)
                        return port
                    }
                }
            } catch (ex: Exception) {
                // Continue if the port is busy
            }
        }
        throw IllegalStateException("No available port in the specified range")
    }

    val localhostName: String by lazy {
        try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            ""
        }
    }

    fun isLocalServerIp(ip: String): Boolean = ip == localhostName
}
