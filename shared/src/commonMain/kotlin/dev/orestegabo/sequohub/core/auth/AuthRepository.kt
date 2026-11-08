package dev.orestegabo.sequohub.core.auth

class AuthRepository(
    private val authApiClient: AuthApiClient,
    private val sessionStore: AuthSessionStore,
    private val logger: AuthDebugLogger = AuthDebugLogger,
) {
    suspend fun loginWithGoogle(idToken: String): AuthSession {
        if (idToken.isBlank()) {
            throw GoogleIdTokenMissingException()
        }

        val response = authApiClient.loginWithGoogle(idToken)
        logger.logBackendStatus(response.statusCode)
        sessionStore.save(response.session)
        return response.session
    }

    suspend fun getSavedSession(): AuthSession? =
        sessionStore.get()

    suspend fun logout() {
        sessionStore.clear()
    }

    fun close() {
        authApiClient.close()
    }
}

class GoogleIdTokenMissingException : Exception(
    "Google ID token missing. Check OAuth client configuration.",
)

object AuthDebugLogger {
    fun logGoogleIdToken(idToken: String) {
        // Token values and token metadata are not diagnostic output.
    }

    fun logBackendStatus(statusCode: Int) {
        println("Google backend login status=$statusCode")
    }
}
