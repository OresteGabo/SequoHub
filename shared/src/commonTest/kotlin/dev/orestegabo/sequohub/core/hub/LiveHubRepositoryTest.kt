package dev.orestegabo.sequohub.core.hub

import dev.orestegabo.sequohub.core.auth.*
import dev.orestegabo.sequohub.core.network.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.serialization.json.*
import kotlin.test.*

class LiveHubRepositoryTest {
    @Test fun inFlightRefreshCannotResurrectLogoutOrCrossAnAccountSwitch() = runTest {
        for (switchAccount in listOf(false, true)) {
            val began = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>()
            var requireRefresh = false
            val store = Store(); val requests = mutableListOf<String>()
            val http = testClient { request ->
                requests += request.url.encodedPath + ":" + request.headers[HttpHeaders.Authorization].orEmpty()
                when(request.url.encodedPath) {
                    "/api/auth/me" -> when {
                        request.headers[HttpHeaders.Authorization] == "Bearer account-b" -> respond(user.replace("operator-a", "operator-b"), headers = jsonHeaders)
                        request.headers[HttpHeaders.Authorization] == "Bearer access" && requireRefresh -> respond("{}", HttpStatusCode.Unauthorized, jsonHeaders)
                        else -> respond(user, headers = jsonHeaders)
                    }
                    "/api/auth/refresh" -> { began.complete(Unit); release.await(); respond("""{"accessToken":"old-refreshed","refreshToken":"old-rotated","expiresIn":3600}""", headers = jsonHeaders) }
                    "/api/auth/logout" -> respond("{}", headers = jsonHeaders)
                    else -> error("An old repository crossed the account boundary")
                }
            }
            val session = LiveHubSession(store, "https://local.fixture.invalid", http)
            session.restore(); val oldRepository = LiveScopedHubRepository(session)
            requireRefresh = true
            val pending = async { runCatching { session.me() } }
            began.await()
            if(switchAccount) session.accept(AuthSession("account-b", "refresh-b", 3600, "device-b")) else session.logout()
            release.complete(Unit)
            assertIs<HubSessionChanged>(pending.await().exceptionOrNull())
            if(switchAccount) { assertEquals("account-b", store.session?.accessToken); assertEquals("operator-b", session.user?.id) }
            else { assertNull(store.session); assertNull(session.user) }
            val count = requests.size
            assertFailsWith<HubSessionChanged> { oldRepository.sites() }
            assertEquals(count, requests.size)
            session.close()
        }
    }

    @Test fun localFixtureSourceIsExplicitOnTheWireAndRequiresPositiveCapability() = runTest {
        val bodies = mutableListOf<JsonObject>()
        val client = testClient { request -> when(request.url.encodedPath) {
            "/api/auth/development-access" -> { assertEquals("SEQUO_HUB", request.url.parameters["appSource"]); respond("""{"enabled":true,"mode":"LOCAL_FIXTURE","accounts":[{"key":"dede","label":"Fixture relay operator"}]}""", headers = jsonHeaders) }
            "/api/auth/development-session" -> { bodies += Json.parseToJsonElement((request.body as TextContent).text).jsonObject; respond("""{"accessToken":"fixture-access","refreshToken":"fixture-refresh","expiresIn":3600}""", headers = jsonHeaders) }
            "/api/auth/me" -> respond(user, headers = jsonHeaders)
            else -> error("Unexpected fixture request")
        } }
        val session = LiveHubSession(Store(), "https://local.fixture.invalid", client)
        if(NetworkConfig.IsLocalFixture) {
            assertEquals("operator-a", session.fixtureLogin("dede").id)
            assertEquals("SEQUO_HUB", bodies.single().getValue("appSource").jsonPrimitive.content)
            assertEquals("dede", bodies.single().getValue("accountKey").jsonPrimitive.content)
            assertTrue(bodies.single().getValue("deviceId").jsonPrimitive.content.startsWith("hub-"))
            assertFailsWith<IllegalArgumentException> { session.fixtureLogin("arbitrary-user-id") }
        } else { assertFalse(session.fixtureAccess().enabled); assertTrue(bodies.isEmpty()); assertFailsWith<IllegalStateException> { session.fixtureLogin("dede") } }
        session.close()
    }

    @Test fun realScopedRowsAndStableCredentialWritesHaveNoSampleFallback() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        var detailReads = 0
        val client = testClient { request -> requests += request
            val payload = when(request.url.encodedPath) {
                "/api/auth/me" -> user
                "/api/relay/parcels/sites" -> """[{"id":"site-a","name":"Actual assigned relay"}]"""
                "/api/hub/lockers" -> """[{"lockerId":"actual-slot","relayPointId":"site-a","lockerCode":"COUNTER-7","status":"OCCUPIED","updatedAt":"2026-11-08T12:00:00Z"}]"""
                "/api/relay/parcels" -> """[{"id":"actual-parcel","relayPointId":"site-a","lockerId":"actual-slot","status":"Deposited","updatedAt":"2026-11-08T12:00:00Z"}]"""
                "/api/relay/parcels/actual-parcel" -> { detailReads++
                    """{"id":"actual-parcel","relayPointId":"site-a","lockerId":"actual-slot","status":"Delayed","updatedAt":"2026-11-08T12:04:00Z","history":[
                        {"type":"Deposit","createdAt":"2026-11-08T12:00:00Z","actorUserId":"rider-a"},
                        {"type":"Problem","createdAt":"2026-11-08T12:01:00Z","message":"First recorded note"},
                        {"type":"Delay","createdAt":"2026-11-08T12:02:00Z"},
                        {"type":"Review","createdAt":"2026-11-08T12:03:00Z"},
                        {"type":"Problem","createdAt":"2026-11-08T12:04:00Z","message":"Operator followup","actorUserId":"operator-a"}
                    ]}"""
                }
                "/api/returns/relay" -> "[]"
                "/api/hub/control-state" -> """{"relayPointId":"site-a","effectiveMode":"NORMAL","hub":{"target":"HUB","status":"ACTIVE"},"generatedAt":"2026-11-08T12:00:00Z"}"""
                "/api/hub/opening-hours" -> """{"relayPointId":"site-a","timezone":"Africa/Lome","weeklyHours":[],"exceptions":[]}"""
                "/api/relay/parcels/actual-parcel/release" -> "{}"
                else -> error("Unexpected live request: ${request.url.encodedPath}")
            }
            respond(payload, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val session = LiveHubSession(Store(), "https://local.fixture.invalid", client); session.restore()
        val repository = LiveScopedHubRepository(session)
        val site = repository.sites().single(); val actual = repository.snapshot(site)
        assertEquals("actual-slot", actual.slots.single().lockerId)
        assertEquals("COUNTER-7", actual.slots.single().lockerCode)
        assertEquals("actual-parcel", actual.parcels.single().id)
        assertEquals("Delayed", actual.parcels.single().status)
        assertEquals(5, actual.parcels.single().history.size)
        assertEquals("operator-a", actual.parcels.single().history.last().actorUserId)
        assertEquals("Operator followup", actual.parcels.single().history.last().message)
        assertEquals(1, detailReads)
        assertEquals(5, repository.snapshot(site).parcels.single().history.size)
        assertEquals(2, detailReads) // Refresh rereads authoritative history rather than caching a list projection.
        assertTrue(requests.filter { it.url.encodedPath == "/api/relay/parcels/actual-parcel" }.all {
            it.method == HttpMethod.Get && it.url.parameters["relayPointId"] == "site-a"
        })
        assertTrue(actual.openingHours.weeklyHours.isEmpty())
        assertTrue(requests.all { it.headers[HttpHeaders.Authorization] == "Bearer access" })
        assertTrue(requests.filter { it.url.encodedPath in setOf("/api/hub/lockers", "/api/returns/relay", "/api/relay/parcels") }.all { it.url.parameters["relayPointId"] == "site-a" })
        assertFailsWith<IllegalArgumentException> { repository.releaseIntent("site-a", "actual-parcel", "123456", false, false) }
        val intent = repository.releaseIntent("site-a", "actual-parcel", "nonce-for-real-qr", true, true)
        repository.write(intent); repository.write(intent)
        val bodies = requests.filter { it.method == HttpMethod.Post }.map { (it.body as TextContent).text }
        assertEquals(2, bodies.size); assertEquals(bodies[0], bodies[1])
        val submitted = Json.parseToJsonElement(bodies.first()).jsonObject
        assertEquals("nonce-for-real-qr", submitted.getValue("rawQrNonce").jsonPrimitive.content)
        assertEquals(true, submitted.getValue("identityDocumentMatched").jsonPrimitive.boolean)
        assertFalse("rawNumericCode" in submitted)
        session.close()
    }

    @Test fun failedOrMismatchedScopedDetailsNeverBecomeEmptyHistory() = runTest {
        for (mode in listOf("forbidden", "missing", "unavailable", "wrong-site", "wrong-id", "wrong-list-site")) {
            val requests = mutableListOf<HttpRequestData>()
            val http = testClient { request ->
                requests += request
                when (request.url.encodedPath) {
                    "/api/auth/me" -> respond(user, headers = jsonHeaders)
                    "/api/hub/lockers" -> respond("[]", headers = jsonHeaders)
                    "/api/relay/parcels" -> {
                        val site = if (mode == "wrong-list-site") "site-b" else "site-a"
                        val row = buildJsonObject { put("id", "actual-parcel"); put("relayPointId", site); put("status", "Deposited"); put("updatedAt", "2026-11-08T12:00:00Z") }
                        respond(JsonArray(listOf(row)).toString(), headers = jsonHeaders)
                    }
                    "/api/relay/parcels/actual-parcel" -> when (mode) {
                        "forbidden" -> respond("{}", HttpStatusCode.Forbidden, jsonHeaders)
                        "missing" -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
                        "unavailable" -> respond("{}", HttpStatusCode.ServiceUnavailable, jsonHeaders)
                        else -> {
                            val id = if (mode == "wrong-id") "another-parcel" else "actual-parcel"
                            val site = if (mode == "wrong-site") "site-b" else "site-a"
                            val row = buildJsonObject { put("id", id); put("relayPointId", site); put("status", "Deposited"); put("updatedAt", "2026-11-08T12:00:00Z"); put("history", JsonArray(emptyList())) }
                            respond(row.toString(), headers = jsonHeaders)
                        }
                    }
                    else -> error("No snapshot should be published after an unconfirmed detail read")
                }
            }
            val session = LiveHubSession(Store(), "https://local.fixture.invalid", http)
            session.restore()
            val repository = LiveScopedHubRepository(session)
            val failure = assertFails { repository.snapshot(AssignedHub("site-a", "Actual relay")) }
            if (mode.startsWith("wrong-")) assertIs<IllegalArgumentException>(failure)
            if (mode in setOf("forbidden", "missing")) assertIs<SequoApiException>(failure)
            val details = requests.filter { it.url.encodedPath == "/api/relay/parcels/actual-parcel" }
            assertEquals(if (mode == "wrong-list-site") 0 else 1, details.size)
            assertTrue(details.all { it.url.parameters["relayPointId"] == "site-a" })
            assertTrue(requests.all { it.method == HttpMethod.Get && it.headers[HttpHeaders.Authorization] == "Bearer access" })
            session.close()
        }
    }

    @Test fun wrongSiteOrFailedControlReadNeverProducesOnlineFallback() = runTest {
        var forged = true
        val client = testClient { request ->
            when(request.url.encodedPath) {
                "/api/auth/me" -> respond(user, headers = jsonHeaders)
                "/api/hub/lockers" -> respond("""[{"lockerId":"other-slot","relayPointId":"${if(forged) "site-b" else "site-a"}","lockerCode":"B1","status":"AVAILABLE","updatedAt":"2026-11-08T12:00:00Z"}]""", headers = jsonHeaders)
                "/api/relay/parcels", "/api/returns/relay" -> respond("[]", headers = jsonHeaders)
                "/api/hub/control-state" -> if(forged) respond("""{"relayPointId":"site-a","effectiveMode":"NORMAL","hub":{"target":"HUB","status":"ACTIVE"},"generatedAt":"2026-11-08T12:00:00Z"}""", headers = jsonHeaders) else respond("{}", HttpStatusCode.Forbidden, jsonHeaders)
                "/api/hub/opening-hours" -> respond("""{"relayPointId":"site-a","weeklyHours":[],"exceptions":[]}""", headers = jsonHeaders)
                else -> error("Unexpected request")
            }
        }
        val session = LiveHubSession(Store(), "https://local.fixture.invalid", client); session.restore(); val repository = LiveScopedHubRepository(session)
        assertFailsWith<IllegalArgumentException> { repository.snapshot(AssignedHub("site-a", "Actual")) }
        forged = false
        assertFailsWith<SequoApiException> { repository.snapshot(AssignedHub("site-a", "Actual")) }
        assertFalse(repository.recordCollectionHandoff("site-a", "unsupported-batch", listOf("actual-parcel")).success)
        assertFailsWith<IllegalStateException> { repository.loadCollectionManifest("site-a", "not-server-owned") }
        session.close()
    }

    @Test fun expiredAccessRefreshesWithDeviceBindingAndDeniedRefreshClearsSession() = runTest {
        val store = Store(); var failRefresh = false; var refreshes = 0
        val client = testClient { request -> when(request.url.encodedPath) {
            "/api/auth/me" -> if(request.headers[HttpHeaders.Authorization] == "Bearer renewed") respond(user, headers = jsonHeaders) else respond("{}", HttpStatusCode.Unauthorized, jsonHeaders)
            "/api/auth/refresh" -> { refreshes++; assertTrue((request.body as TextContent).text.contains("device-test")); if(failRefresh) respond("{}", HttpStatusCode.Unauthorized, jsonHeaders) else respond("""{"accessToken":"renewed","refreshToken":"new-refresh","expiresIn":3600}""", headers = jsonHeaders) }
            else -> error("Unexpected request")
        } }
        val session = LiveHubSession(store, "https://local.fixture.invalid", client)
        assertEquals("operator-a", session.restore()?.id); assertEquals(1, refreshes); assertEquals("renewed", store.session?.accessToken); assertEquals("device-test", store.session?.deviceId)
        session.close()
        store.session = AuthSession("access", "refresh", 3600, "device-test"); failRefresh = true
        val denied = LiveHubSession(store, "https://local.fixture.invalid", testClient { respond("{}", HttpStatusCode.Unauthorized, jsonHeaders) })
        assertFailsWith<IllegalStateException> { denied.restore() }; assertNull(store.session); assertNull(denied.user)
        denied.close()
    }
    @Test fun preferencesAndDatedHoursUseOwnedServerContractsWhileDeletionOnlyBuildsConfirmedIntent() = runTest {
        val bodies = mutableListOf<Pair<String, JsonObject>>()
        var theme = "SYSTEM"
        val http = testClient { request ->
            when(request.url.encodedPath) {
                "/api/auth/me" -> respond(user, headers = jsonHeaders)
                "/api/preferences" -> {
                    assertEquals("SEQUO_HUB", request.url.parameters["appFamily"])
                    if(request.method == HttpMethod.Patch) {
                        val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                        bodies += "preferences" to body; theme = body.getValue("theme").jsonPrimitive.content
                    }
                    respond("""{"userId":"operator-a","appFamily":"SEQUO_HUB","theme":"$theme","language":"fr","quickScanOnOpen":true,"soundFeedback":false,"largeLockerLabels":true}""", headers = jsonHeaders)
                }
                "/api/hub/opening-hours" -> {
                    bodies += "hours" to Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    respond("{}", headers = jsonHeaders)
                }
                else -> error("Unexpected request; no deletion request is executed by this fixture")
            }
        }
        val session = LiveHubSession(Store(), "https://local.fixture.invalid", http); session.restore()
        val repository = LiveScopedHubRepository(session)
        assertEquals("DARK", repository.savePreferences("DARK", "fr", true, false, true).theme)
        val prefs = bodies.single().second
        assertEquals(true, prefs.getValue("largeLockerLabels").jsonPrimitive.boolean)
        assertFalse("userId" in prefs)
        repository.saveHours(HubOpeningHoursResponse("site-a", "Africa/Lome", listOf(WeeklyOpeningHourResponse("MONDAY", true, "08:30", "17:00")),
            listOf(OpeningHourExceptionResponse("2026-12-25", true, reason = "Closed by operator"))))
        val hours = bodies.single { it.first == "hours" }.second
        assertEquals("site-a", hours.getValue("relayPointId").jsonPrimitive.content)
        assertEquals("2026-12-25", hours.getValue("exceptions").jsonArray.single().jsonObject.getValue("date").jsonPrimitive.content)
        assertFailsWith<IllegalArgumentException> { repository.deletionIntent("yes", "") }
        val deletion = repository.deletionIntent("DELETE_MY_ACCOUNT", "Requested by account owner")
        assertEquals("/api/account/deletion-requests", deletion.path)
        assertEquals("DELETE_MY_ACCOUNT", deletion.body.getValue("confirmation").jsonPrimitive.content)
        assertFalse("userId" in deletion.body)
        session.close()
    }

    private class Store : AuthSessionStore {
        var session: AuthSession? = AuthSession("access", "refresh", 3600, "device-test")
        override suspend fun save(session: AuthSession) { this.session = session }
        override suspend fun get() = session
        override suspend fun clear() { session = null }
    }
    private fun testClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData) = HttpClient(MockEngine { handler(it) }) {
        expectSuccess = true; defaultRequest { headers.append(HttpHeaders.ContentType, "application/json") }; install(ContentNegotiation) { json(defaultNetworkJson) }
        HttpResponseValidator { handleResponseExceptionWithRequest { error, _ -> if(error is ClientRequestException) throw SequoApiException(error.response.status.value, null, "Fixture HTTP rejected") } }
    }
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val user = """{"id":"operator-a","displayName":"Actual operator","active":true,"status":"ACTIVE","roles":["RELAY_PARTNER"]}"""
}
