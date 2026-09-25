package dev.orestegabo.sequohub.core.auth

import dev.orestegabo.sequohub.core.network.NetworkConfig
import dev.orestegabo.sequohub.core.network.createSequoHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AuthApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoHttpClient(baseUrl),
) {
    suspend fun loginWithGoogle(idToken: String): AuthApiResponse {
        val response = httpClient.post("${baseUrl.trimEnd('/')}/api/auth/login/social") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                SocialLoginRequest(
                    provider = SocialLoginProvider.Google,
                    token = idToken,
                ),
            )
        }
        return response.toAuthApiResponse()
    }

    fun close() {
        httpClient.close()
    }

    private suspend fun HttpResponse.toAuthApiResponse(): AuthApiResponse {
        val statusCode = status.value
        if (statusCode !in 200..299) {
            throw AuthApiException(
                statusCode = statusCode,
                safeMessage = "Backend Google login failed with HTTP $statusCode.",
            )
        }

        return AuthApiResponse(
            statusCode = statusCode,
            session = body(),
        )
    }
}

data class AuthApiResponse(
    val statusCode: Int,
    val session: AuthSession,
)

class AuthApiException(
    val statusCode: Int,
    val safeMessage: String,
) : Exception(safeMessage)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

@Serializable
private data class SocialLoginRequest(
    val provider: SocialLoginProvider,
    val token: String,
)

@Serializable
private enum class SocialLoginProvider {
    @SerialName("GOOGLE")
    Google,
}
