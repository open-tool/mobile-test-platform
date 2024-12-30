package com.atiurin.atp.farmcliclient

import com.atiurin.atp.kmpclient.FarmClient
import com.atiurin.atp.kmpclient.FarmClientConfig

object FarmClientProvider {
    lateinit var client: FarmClient

    fun init(config: FarmClientConfig){
        client = FarmClient(config, doOnFailure = {
            println("Failure: $it")
        })
    }
}

