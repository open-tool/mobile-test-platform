package com.farm.cli.analyzer

interface CliCommandResultAnalyzer {
    /**
     * Return true if command executed successfully, false otherwise
     */
    fun analyze(result: String): Boolean
}