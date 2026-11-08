package dev.orestegabo.sequohub.core.auth

import dev.orestegabo.sequohub.core.network.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable data class HubCurrentUser(val id: String, val displayName: String? = null, val email: String? = null, val active: Boolean, val status: String, val roles: List<String> = emptyList())
@Serializable data class HubFixtureAccount(val key: String, val label: String)
@Serializable data class HubFixtureAccess(val enabled: Boolean = false, val mode: String? = null, val accounts: List<HubFixtureAccount> = emptyList())
@Serializable private data class FixtureLogin(val appSource: String, val accountKey: String, val deviceId: String, val deviceLabel: String = "SequoHub local fixture")
@Serializable private data class RefreshRequest(val refreshToken: String, val deviceId: String?)
@Serializable private data class LogoutRequest(val refreshToken: String)
class HubSessionChanged : IllegalStateException("The signed-in session changed. Read the current account again.")
@PublishedApi internal data class HubSessionResponse(val response: HttpResponse, val generation: Long)
fun hubOperationId(): String = kotlin.random.Random.nextBytes(16).joinToString("") { ((it.toInt() and 255) + 256).toString(16).takeLast(2) }

/** Token mutation and result publication are bound to one session generation. */
class LiveHubSession(private val store: AuthSessionStore, private val baseUrl: String = NetworkConfig.BaseUrl,
    private val client: HttpClient = createSequoHttpClient(baseUrl)) {
    private val stateMutex = Mutex()
    private val refreshMutex = Mutex()
    private var generation = 0L
    private var saved: AuthSession? = null
    var user: HubCurrentUser? = null; private set
    var scopeIdentity: String? = null; private set
    suspend fun restore(): HubCurrentUser? {
        val ticket = stateMutex.withLock { generation++; saved = store.get(); user = null; scopeIdentity = null; generation.takeIf { saved != null } } ?: return null
        return verify(ticket)
    }
    suspend fun accept(session: AuthSession): HubCurrentUser {
        val ticket = stateMutex.withLock { generation++; saved = session; user = null; scopeIdentity = null; generation }
        return try { verify(ticket) } catch(e: Exception) { clearIfCurrent(ticket); throw e }
    }
    private suspend fun verify(ticket: Long): HubCurrentUser {
        val actual = me()
        require(actual.active && actual.status == "ACTIVE" && actual.roles.any { it in setOf("RELAY_PARTNER", "ADMIN", "SUPER_ADMIN") }) { "This account is not authorized to operate a relay. Ask SEQUO to review its role and relay assignment." }
        stateMutex.withLock { checkCurrent(ticket); store.save(requireNotNull(saved)); user = actual; scopeIdentity = "$ticket:${actual.id}" }
        return actual
    }
    suspend fun fixtureAccess(): HubFixtureAccess {
        if(!NetworkConfig.IsLocalFixture) return HubFixtureAccess()
        return client.get("$baseUrl/api/auth/development-access") { parameter("appSource", "SEQUO_HUB") }.body()
    }
    suspend fun fixtureLogin(accountKey: String): HubCurrentUser {
        check(NetworkConfig.IsLocalFixture) { "Local fixture sign-in is unavailable in this build." }
        val capability = fixtureAccess()
        require(capability.enabled && capability.mode == "LOCAL_FIXTURE" && capability.accounts.any { it.key == accountKey }) { "This local fixture account is unavailable." }
        val device = "hub-${hubOperationId()}"
        val tokens: AuthSession = client.post("$baseUrl/api/auth/development-session") { contentType(ContentType.Application.Json); setBody(FixtureLogin("SEQUO_HUB", accountKey, device)) }.body()
        return accept(tokens.copy(deviceId = device))
    }
    suspend fun me(): HubCurrentUser = request("/api/auth/me")
    suspend inline fun <reified T> request(path: String, method: HttpMethod = HttpMethod.Get, body: JsonElement? = null, expectedScope: String? = null): T {
        val scoped = scopedResponse(path, method, body, expectedScope)
        val decoded: T = scoped.response.body()
        ensureCurrent(scoped.generation)
        return decoded
    }
    suspend fun response(path: String, method: HttpMethod, body: JsonElement?, expectedScope: String? = null): HttpResponse = scopedResponse(path, method, body, expectedScope).response
    @PublishedApi internal suspend fun scopedResponse(path: String, method: HttpMethod, body: JsonElement?, expectedScope: String?): HubSessionResponse {
        require(path.startsWith("/api/") && !path.contains("://"))
        val (ticket, before) = stateMutex.withLock { if(expectedScope != null && scopeIdentity != expectedScope) throw HubSessionChanged(); generation to requireNotNull(saved) { "Sign in to continue." } }
        suspend fun perform(tokens: AuthSession): HttpResponse {
            ensureCurrent(ticket)
            return client.request("$baseUrl$path") { this.method = method; bearerAuth(tokens.accessToken); if(body != null) { contentType(ContentType.Application.Json); setBody(body) } }
        }
        val response = try { perform(before) } catch(e: SequoApiException) {
            if(e.statusCode != 401) throw e
            val current = refreshMutex.withLock {
                val latest = stateMutex.withLock { checkCurrent(ticket); requireNotNull(saved) }
                if(latest.accessToken != before.accessToken) latest
                else try {
                    val refreshed: AuthSession = client.post("$baseUrl/api/auth/refresh") { contentType(ContentType.Application.Json); setBody(RefreshRequest(latest.refreshToken, latest.deviceId)) }.body()
                    stateMutex.withLock { checkCurrent(ticket); refreshed.copy(deviceId = latest.deviceId).also { saved = it; store.save(it) } }
                } catch(failure: Exception) {
                    if(failure is CancellationException || failure is HubSessionChanged) throw failure
                    clearIfCurrent(ticket)
                    throw IllegalStateException("Your session ended. Sign in again.")
                }
            }
            perform(current)
        }
        ensureCurrent(ticket)
        return HubSessionResponse(response, ticket)
    }
    @PublishedApi internal suspend fun ensureCurrent(ticket: Long) = stateMutex.withLock { checkCurrent(ticket) }
    private fun checkCurrent(ticket: Long) { if(generation != ticket || saved == null) throw HubSessionChanged() }
    private suspend fun clearIfCurrent(ticket: Long) = stateMutex.withLock { if(generation == ticket) { generation++; saved = null; user = null; scopeIdentity = null; store.clear() } }
    suspend fun logout() {
        val tokens = stateMutex.withLock { generation++; val old = saved; saved = null; user = null; scopeIdentity = null; store.clear(); old }
        if(tokens != null) client.post("$baseUrl/api/auth/logout") { contentType(ContentType.Application.Json); setBody(LogoutRequest(tokens.refreshToken)) }
    }
    fun close() = client.close()
}
