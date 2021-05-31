package com.atiurin.atp.runner

class InstallException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(t: Throwable) : super(t)
}