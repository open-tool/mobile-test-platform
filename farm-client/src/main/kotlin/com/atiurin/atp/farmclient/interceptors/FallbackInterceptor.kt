package com.atiurin.atp.farmclient.interceptors

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class FallbackInterceptor(private val urls: List<String>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        for (urlStr in urls) {
            println("FallbackInterceptor: intercepting request to $urlStr")
            val url = HttpUrl.parse(urlStr)
            if (url != null) {
                val newUrl = request.url().newBuilder()
                    .scheme(url.scheme())
                    .host(url.host())
                    .port(url.port())
                    .build()
                val newRequest = request.newBuilder().url(newUrl).build()

                try {
                    val response = chain.proceed(newRequest)
                    if (response.isSuccessful) {
                        return response
                    }
                } catch (e: IOException) {
                    println("FallbackInterceptor: Server $urlStr is unreachable")
                }
            }
        }
        throw IOException("All servers are unreachable")
    }
}