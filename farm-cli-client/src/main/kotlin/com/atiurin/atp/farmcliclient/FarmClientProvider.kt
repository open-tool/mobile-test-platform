package com.atiurin.atp.farmcliclient

import com.atiurin.atp.farmclient.FarmClient
import com.atiurin.atp.farmclient.FarmClientConfig

object FarmClientProvider {
    lateinit var client: FarmClient

    fun init(config: FarmClientConfig){
        client = FarmClient(config)
    }
}

