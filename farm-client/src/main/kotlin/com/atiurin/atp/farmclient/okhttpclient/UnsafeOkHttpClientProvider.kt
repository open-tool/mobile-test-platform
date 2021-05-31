package com.atiurin.atp.farmclient.okhttpclient

import com.atiurin.atp.farmclient.FarmClientConfig
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class UnsafeOkHttpClientProvider : OkHttpClientProvider {
    override fun provide(config: FarmClientConfig): OkHttpClient {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        val builder = OkHttpClient.Builder().apply {
            connectTimeout(config.connectionTimeoutMs, TimeUnit.MILLISECONDS)
            readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
            sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            hostnameVerifier(HostnameVerifier { _, _ -> true })
        }
        return builder.build()
    }
}