package com.atiurin.atp.farmcliclient.util

fun waitFor(
    timeoutMs: Long,
    intervalMs: Long = 100L, // Default polling interval
    condition: () -> Boolean
): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeoutMs) {
        if (condition()) {
            return true
        }
        Thread.sleep(intervalMs)
    }
    return false
}