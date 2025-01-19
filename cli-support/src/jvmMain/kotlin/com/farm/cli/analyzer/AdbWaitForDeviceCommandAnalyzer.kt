package com.farm.cli.analyzer

class AdbWaitForDeviceCommandAnalyzer : CliCommandResultAnalyzer {
    override fun analyze(result: String): Boolean {
        val normalizedResult = result.lowercase()
        return errorKeywords.none { normalizedResult.contains(it.lowercase()) }
    }

    companion object {
        val errorKeywords = listOf(
            "error: device unauthorized",
            "failed to authenticate",
            "unable to connect",
            "connection refused",
            "no devices/emulators found",
            "host is down",
            "couldn't read status",
            "timeout expired while waiting for device",
            "more than one device/emulator",
            "adb server version"
        )
    }
}