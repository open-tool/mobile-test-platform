package com.atiurin.atp.runner

import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.exception.PushFailedException
import com.malinskiy.adam.request.Feature
import com.malinskiy.adam.request.device.FetchDeviceFeaturesRequest
import com.malinskiy.adam.request.pkg.InstallRemotePackageRequest
import com.malinskiy.adam.request.pkg.UninstallRemotePackageRequest
import com.malinskiy.adam.request.prop.GetPropRequest
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import com.malinskiy.adam.request.sync.compat.CompatPushFileRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import java.io.File
import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime

class AdbDevice(private val client: AndroidDebugBridgeClient,
                val adbSerial: String):CoroutineScope {
    private var props: Map<String, String> = emptyMap()
    private lateinit var supportedFeatures: List<Feature>
    protected lateinit var md5cmd: String
    suspend fun setup() {
        withContext(coroutineContext) {
            md5cmd = detectMd5Binary()
            fetchProps()
            supportedFeatures = client.execute(FetchDeviceFeaturesRequest(adbSerial))
        }
    }

    private suspend fun fetchProps() {
        val map = client.execute(GetPropRequest(), serial = adbSerial)
        props = map
    }
    suspend fun pushFile(localFilePath: String, remoteFilePath: String, verify: Boolean) {
        val file = File(localFilePath)
        var progress: Double = 0.0

        try {
            measureFileTransfer(File(localFilePath)) {
                val channel = client.execute(
                    CompatPushFileRequest(file, remoteFilePath, supportedFeatures, this),
                    serial = adbSerial
                )
                for (update in channel) {
                    progress = update
                }
            }
        } catch (e: PushFailedException) {
            throw TransferException(e)
        }

        if (progress != 1.0) {
            throw TransferException("Couldn't push file $localFilePath to device $adbSerial:$remoteFilePath. Last progress: $progress")
        }

        if (verify) {
            val expectedMd5 = Files.asByteSource(File(localFilePath)).hash(Hashing.md5()).toString()
            waitForRemoteFileSync(expectedMd5, remoteFilePath)
        }
    }

    suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean): String? {
        return withTimeoutOrNull(Duration.ofSeconds(20)) {
            client.execute(UninstallRemotePackageRequest(appPackage, keepData = keepData), serial = adbSerial).output
        }
    }

    suspend fun installPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String? {
        val file = File(absolutePath)
        val remotePath = "/data/local/tmp/${file.name}"

        try {
            withTimeoutOrNull(Duration.ofSeconds(60)) {
                pushFile(absolutePath, remotePath, verify = true)
            } ?: throw InstallException("Timeout transferring $absolutePath")
        } catch (e: TransferException) {
            throw InstallException(e)
        }

        val result = withTimeoutOrNull(Duration.ofSeconds(20)) {
            client.execute(
                InstallRemotePackageRequest(
                    remotePath,
                    reinstall = reinstall,
                    extraArgs = optionalParams.split(" ").toList() + " "
                ), serial = adbSerial
            )
        } ?: throw InstallException("Timeout transferring $absolutePath")

        safeExecuteShellCommand("rm $remotePath")
        return result.output.trim()
    }
    private inline fun measureFileTransfer(file: File, block: () -> Unit) {
        measureTimeMillis {
            block()
        }.let { time ->
            val fileSize = file.length()
            val timeInSeconds = time.toDouble() / 1000
            if (timeInSeconds > .0f && fileSize > 0) {
                val speed = "%.2f".format((fileSize / 1000) / timeInSeconds)
                println(
                    "Transferred ${file.name} to/from $adbSerial. $speed KB/s ($fileSize bytes in ${
                        "%.4f".format(
                            timeInSeconds
                        )
                    })"
                )
            }
        }
    }
    private suspend fun hasBinary(path: String): Boolean {
        val output = safeExecuteShellCommand("ls $path")
        val value: String = output?.trim { it <= ' ' } ?: return false
        return !value.endsWith("No such file or directory")
    }

    private suspend fun detectMd5Binary(): String {
        for (path in listOf("/system/bin/md5", "/system/bin/md5sum")) {
            if (hasBinary(path)) return path.split("/").last()
        }
        return ""
    }
    protected suspend fun waitForRemoteFileSync(
        md5: String,
        remotePath: String
    ) {
        if (md5.isNotEmpty() && md5cmd.isNotEmpty()) {
            val syncTimeMillis = measureTimeMillis {
                do {
                    val remoteMd5 = safeExecuteShellCommand("$md5cmd $remotePath") ?: ""
                    delay(10)
                } while (!remoteMd5.contains(md5))
            }
            println( "$remotePath synced in ${syncTimeMillis}ms" )
        } else {
            println("no md5 was calculated for $remotePath. unable to sync" )
        }
    }
    suspend fun executeShellCommand(command: String, errorMessage: String): String? {
        return try {
            return client.execute(ShellCommandRequest(command), serial = adbSerial).output
        } catch (e: Exception) {
            println(errorMessage)
            null
        }
    }


    suspend fun safeExecuteShellCommand(command: String, errorMessage: String = "Unknown error"): String? {
        return try {
            withTimeoutOrNull(Duration.ofSeconds(20)) {
                client.execute(ShellCommandRequest(command), serial = adbSerial).output
            }
        } catch (e: Exception) {
            println(errorMessage)
            null
        }
    }

    private val dispatcher by lazy {
        newFixedThreadPoolContext(2, "AndroidDevice - execution - $adbSerial")
    }
    override val coroutineContext: CoroutineContext = dispatcher
}

suspend fun <T> withTimeoutOrNull(timeout: Duration, block: suspend CoroutineScope.() -> T): T? =
    kotlinx.coroutines.withTimeoutOrNull(timeout.toMillis(), block)