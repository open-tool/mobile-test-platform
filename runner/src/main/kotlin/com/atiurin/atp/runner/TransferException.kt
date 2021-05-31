package com.atiurin.atp.runner

class TransferException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(t: Throwable) : super(t)
}
