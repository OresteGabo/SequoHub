package dev.orestegabo.sequohub.core.auth

import dev.orestegabo.sequohub.core.network.defaultNetworkJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRepositoryTest {
    @Test
    fun loginWithGooglePostsProviderAndToken() = kotlinx.coroutines.test.runTest {
        var capturedBody: String? = null
        val client = testHttpClient { request ->
            capturedBody = (request.body as TextContent).text
            authSuccessResponse()
        }
        val apiClient = AuthApiClient(httpClient = client)

        val response = apiClient.loginWithGoogle("google-id-token")

        assertEquals(200, response.statusCode)
        assertEquals("backend-access", response.session.accessToken)
        assertNotNull(capturedBody)
        assertTrue(capturedBody.contains("\"provider\":\"GOOGLE\""))
        assertTrue(capturedBody.contains("\"token\":\"google-id-token\""))
    }

    @Test
    fun loginWithGoogleStoresBackendSession() = kotlinx.coroutines.test.runTest {
        val store = FakeAuthSessionStore()
        val repository = AuthRepository(
            authApiClient = AuthApiClient(httpClient = testHttpClient { authSuccessResponse() }),
            sessionStore = store,
        )

        repository.loginWithGoogle("google-id-token")

        assertEquals("backend-access", store.savedSession?.accessToken)
        assertEquals("backend-refresh", store.savedSession?.refreshToken)
    }

    @Test
    fun blankGoogleIdTokenDoesNotCallBackend() = kotlinx.coroutines.test.runTest {
        var backendCalled = false
        val repository = AuthRepository(
            authApiClient = AuthApiClient(
                httpClient = testHttpClient {
                    backendCalled = true
                    authSuccessResponse()
                },
            ),
            sessionStore = FakeAuthSessionStore(),
        )

        assertFailsWith<GoogleIdTokenMissingException> {
            repository.loginWithGoogle("")
        }
        assertEquals(false, backendCalled)
    }

    @Test
    fun backendUnauthorizedIsHandledAsAuthApiException() = kotlinx.coroutines.test.runTest {
        val apiClient = AuthApiClient(
            httpClient = testHttpClient {
                respond(
                    content = """{"error":"invalid_google_token"}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )

        val error = assertFailsWith<AuthApiException> {
            apiClient.loginWithGoogle("google-id-token")
        }

        assertEquals(401, error.statusCode)
    }

    @Test
    fun backendGoogleTokenReasonIsMappedToHelpfulMessage() = kotlinx.coroutines.test.runTest {
        val apiClient = AuthApiClient(
            httpClient = testHttpClient {
                respond(
                    content = """{"error":"invalid_google_token","reason":"invalid_audience"}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )

        val error = assertFailsWith<AuthApiException> {
            apiClient.loginWithGoogle("google-id-token")
        }

        assertEquals(401, error.statusCode)
        assertTrue(error.message.orEmpty().contains("Google client ID"))
    }

    private fun testHttpClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient =
        HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) {
                json(defaultNetworkJson)
            }
        }

    private fun MockRequestHandleScope.authSuccessResponse(): HttpResponseData =
        respond(
            content = """{"accessToken":"backend-access","refreshToken":"backend-refresh","expiresIn":3600}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )

    private class FakeAuthSessionStore : AuthSessionStore {
        var savedSession: AuthSession? = null

        override suspend fun save(session: AuthSession) {
            savedSession = session
        }

        override suspend fun get(): AuthSession? =
            savedSession

        override suspend fun clear() {
            savedSession = null
        }
    }
}
