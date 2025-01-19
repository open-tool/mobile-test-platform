package com.farm.cli.analyzer

class DummyCommandResultAnalyzer : CliCommandResultAnalyzer {
    override fun analyze(result: String): Boolean = true
}