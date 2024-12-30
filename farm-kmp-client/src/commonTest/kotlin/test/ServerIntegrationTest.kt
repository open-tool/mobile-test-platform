package test

import com.atiurin.atp.kmpclient.FarmClient
import com.atiurin.atp.kmpclient.FarmClientConfig
import com.atiurin.atp.kmpclient.getFarmUrlFromString
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class ServerIntegrationTest {
    val client = FarmClient(
        FarmClientConfig(
            farmUrls = listOf(
                getFarmUrlFromString("http://localhost:7070"),
                getFarmUrlFromString("http://localhost:8080")
            ),
            userAgent = "test"
        ), doOnFailure = {
            println("Failed")
        }
    )

    @Test
    fun acquireDeviceTest() {
        val devices = runBlocking { client.acquire(1, "30") }
        assertTrue(devices.isNotEmpty())
        runBlocking { client.release(devices.map { it.id }) }
    }

    @Test
    fun getDeviceInfoTest() {
        runBlocking {
            val devices = client.list()
            assertTrue(devices.isNotEmpty())
            val infos = client.info(devices.take(2).map { it.device.id })
            assertTrue(infos.isNotEmpty())
        }
    }
}