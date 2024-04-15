package com.atiurin.atp.farmserver.test.util

import java.lang.Thread.sleep
import java.time.Instant

object AssertUtils {
    fun <T> awaitTrue(
        valueProviderBlock: () -> T,
        assertionBlock: (value: T) -> Boolean,
        timeoutMs: Long = 5_000,
        delay: Long = 100,
        desc: (value: T) -> String = { value -> "Actual value = $value" }
    ) {
        val startTime = Instant.now().toEpochMilli()
        var value: T? = null
        while (Instant.now().toEpochMilli() < startTime + timeoutMs){
            value = valueProviderBlock()
            if (assertionBlock(value)) return
            else sleep(delay)
        }
        val valueToDescribe: T = value ?: valueProviderBlock()
        throw AssertionError("Assertion '${desc.invoke(valueToDescribe)}' failed during $timeoutMs ms")
    }
}