package com.atiurin.atp.kmpclient.service

import com.atiurin.atp.farmcore.api.response.GetServersResponse
import com.atiurin.atp.kmpclient.get
import io.ktor.client.HttpClient
import io.ktor.http.path

class ServerService (private val client: HttpClient){
    suspend fun getList(): Result<GetServersResponse> =
        client.get {
            url { path("server/list") }
        }
}