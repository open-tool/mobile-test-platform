package com.atiurin.atp.runner

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.request.misc.GetAdbServerVersionRequest
import com.malinskiy.adam.request.pkg.InstallRemotePackageRequest
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.ConnectException
import java.net.InetAddress


class App : CliktCommand() {
    val apk: File? by option(help = "App under test apk file path").file()
    val testApk: File? by option(help = "Test app apk file path").file(mustExist = true)
    val devicesAmount: Int? by option(help = "Amount of devices to run").int().default(1)
    val api: Int? by option(help = "Android api of devices to run test on").int()

    companion object {
//        val logger: Logger = LoggerFactory.getLogger(App::class.java)
    }

    override fun run() {
        println("apk: '$apk', testApk: '$testApk', devicesAmount: $devicesAmount, api: $api ")
        val adbClient = AndroidDebugBridgeClientFactory().apply {
            port = 49157
        }.build()
        println("${adbClient.host}")
        runBlocking {
            try {
                StartAdbInteractor().execute()
                printAdbServerVersion(adbClient)
            } catch (e: ConnectException) {
                val success = StartAdbInteractor().execute()
                if (!success) {
                    println("Adbd unavaiable")
                }
                printAdbServerVersion(adbClient)
            }
            val device = AdbDevice(adbClient,"emulator-5554")
            device.setup()
            device.installPackage(apk!!.absolutePath, true, "-g")
        }
    }
}

suspend fun printAdbServerVersion(client: AndroidDebugBridgeClient) {
    val adbVersion = client.execute(GetAdbServerVersionRequest())
    println("Android Debug Bridge version $adbVersion")
}

fun pushFile(absolutePath: String){
    val file = File(absolutePath)
    val remotePath = "/data/local/tmp/${file.name}"

}

fun main(args: Array<String>) = App().main(args)
