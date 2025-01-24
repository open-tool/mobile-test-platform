package com.atiurin.atp.farmcore.util

import kotlinx.coroutines.delay

fun waitFor(
    timeoutMs: Long,
    intervalMs: Long = 100L,
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

suspend fun waitForWithDelay(
    timeoutMs: Long,
    intervalMs: Long = 100L,
    condition: () -> Boolean
): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeoutMs) {
        if (condition()) {
            return true
        }
        delay(intervalMs)
    }
    return false
}