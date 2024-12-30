package com.atiurin.atp.kmpclient

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

suspend inline fun <reified T> HttpClient.get(
    block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = execRequest {
    get { block() }
}

suspend inline fun <reified T> HttpClient.patch(
    block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = execRequest {
    patch { block() }
}

suspend inline fun <reified T> HttpClient.post(
    block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = execRequest {
    post { block() }
}

suspend inline fun <reified T> execRequest(
    requester: () -> HttpResponse,
): Result<T> = try {
    val httpResponse: HttpResponse = requester()
    if (!httpResponse.status.isSuccess()) {
        val exception =
            ResponseException(httpResponse, "HTTP ${httpResponse.status}: ${httpResponse.status.description}")
        Result.failure(exception)
    } else {
        val response: T = httpResponse.body()
        Result.success(response)
    }
} catch (exception: ResponseException) {
    Result.failure(exception)
} catch (exception: Throwable) {
    exception.printStackTrace()
    Result.failure(exception)
}
val farmUrl: FarmUrl
    get() = FarmClientConfig.getCurrentFarmUrl()

fun HttpClient(config: FarmClientConfig) = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) { logger = Logger.SIMPLE }
    FarmClientConfig.setNextFarmUrl(config.farmUrls)

    defaultRequest {
        url {
            host = farmUrl.host
            port = farmUrl.port
            protocol = farmUrl.protocol
        }
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Accept, ContentType.Application.Json)
        header(HttpHeaders.Origin, farmUrl.host)
    }
}

