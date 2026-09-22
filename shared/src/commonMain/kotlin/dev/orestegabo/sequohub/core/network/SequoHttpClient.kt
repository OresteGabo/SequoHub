package dev.orestegabo.sequohub.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createSequoHttpClient(
    baseUrl: String = NetworkConfig.ProductionBaseUrl,
    json: Json = defaultNetworkJson,
): HttpClient = HttpClient {
    defaultRequest {
        url.takeFrom(baseUrl)
        headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        accept(ContentType.Application.Json)
    }

    install(ContentNegotiation) {
        json(json)
    }
}

val defaultNetworkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
