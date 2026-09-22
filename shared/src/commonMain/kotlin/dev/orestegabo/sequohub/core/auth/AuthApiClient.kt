package dev.orestegabo.sequohub.core.auth

import dev.orestegabo.sequohub.core.network.NetworkConfig
import dev.orestegabo.sequohub.core.network.createSequoHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AuthApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoHttpClient(baseUrl),
) {
    suspend fun loginWithGoogle(idToken: String): AuthSession =
        httpClient.post("/api/auth/login/social") {
            setBody(
                SocialLoginRequest(
                    provider = SocialLoginProvider.Google,
                    token = idToken,
                ),
            )
        }.body()

    fun close() {
        httpClient.close()
    }
}

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
