package com.atiurin.atp.farmserver.test

import junit.framework.TestCase.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment

@SpringBootTest
class PropertyLoadTest {

    @Autowired
    private lateinit var environment: Environment

    @Test
    fun testPropertyLoad() {
        val farmMode = environment.getProperty("farm.mode")
        println("Farm Mode: $farmMode")
        assertNotNull(farmMode)
    }
}