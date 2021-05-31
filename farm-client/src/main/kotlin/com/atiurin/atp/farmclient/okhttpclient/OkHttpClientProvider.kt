package com.atiurin.atp.farmclient.okhttpclient

import com.atiurin.atp.farmclient.FarmClientConfig
import okhttp3.OkHttpClient

interface OkHttpClientProvider {
    fun provide(config: FarmClientConfig): OkHttpClient
}