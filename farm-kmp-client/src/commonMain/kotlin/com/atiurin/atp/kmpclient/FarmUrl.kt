package com.atiurin.atp.kmpclient

import io.ktor.http.URLProtocol
import io.ktor.http.URLProtocol.Companion.HTTP
import io.ktor.http.URLProtocol.Companion.HTTPS

data class FarmUrl(val host: String, val port: Int, val protocol: URLProtocol = HTTP)

/**
 * Expected url format protocol://host:port
 * sample - http://localhost:8080
 */
fun getFarmUrlFromString(url: String): FarmUrl{
    return try {
        val formatedUrl = if (url.endsWith("/")) {
            url.substringBeforeLast("/")
        } else url
        val protocol = when (formatedUrl.substringBefore("://")){
            "https" -> HTTPS
            else -> HTTP
        }
        val host = formatedUrl.substringAfter("://").substringBefore(":")
        val port = formatedUrl.substringAfter("://").substringAfter(":").toInt()
        FarmUrl(host, port, protocol)
    } catch (ex: Exception){
        throw RuntimeException("Invalid farm url format '$url'. Should be 'protocol://host:port', sample - http://localhost:8080")
    }
}