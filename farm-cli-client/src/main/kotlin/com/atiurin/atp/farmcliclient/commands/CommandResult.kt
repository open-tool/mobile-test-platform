package com.atiurin.atp.farmcliclient.commands

interface CommandResult<T> {
    val value: T
    val success: Boolean
    val message: String
}