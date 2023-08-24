package com.atiurin.atp.farmcliclient.test

import org.junit.jupiter.api.Assertions

object AssertionUtil {
    fun assertException(expected: Boolean = true, block: () -> Unit) {
        var exceptionOccurs = false
        try {
            block()
        } catch (ex: Throwable) {
//            throw ex
            exceptionOccurs = true
        }
        Assertions.assertEquals(expected, exceptionOccurs)
    }
}