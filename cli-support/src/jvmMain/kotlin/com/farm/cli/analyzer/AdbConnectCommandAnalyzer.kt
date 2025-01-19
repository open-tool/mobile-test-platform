package com.farm.cli.analyzer

class AdbConnectCommandAnalyzer : CliCommandResultAnalyzer {
    override fun analyze(result: String): Boolean {
        val normalizedResult = result.lowercase()
        return errorKeywords.none { normalizedResult.contains(it.lowercase()) }
    }

    companion object {
        val errorKeywords = listOf(
            "failed to connect",
            "failed to authenticate",
            "connection refused",
            "unable to connect",
            "error",
            "no route to host",
            "device unauthorized"
        )
    }
}