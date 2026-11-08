package dev.orestegabo.sequohub.core.hub

import dev.orestegabo.sequohub.core.auth.LiveHubSession
import dev.orestegabo.sequohub.core.auth.hubOperationId
import dev.orestegabo.sequohub.core.localization.SequoStrings
import dev.orestegabo.sequohub.feature.hub.*
import io.ktor.http.HttpMethod
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable data class AssignedHub(val id: String, val name: String)
@Serializable data class LiveHubSlot(val lockerId: String, val relayPointId: String, val lockerCode: String, val status: String,
    val reason: String? = null, val expectedAvailableAt: String? = null, val updatedAt: String)
@Serializable data class LiveCustodyEvent(val type: String, val createdAt: String, val message: String? = null, val actorUserId: String? = null)
@Serializable data class LiveDeadlines(val feeStartsAt: String? = null, val reviewDueAt: String, val storageFeeEligible: Boolean,
    val reviewDue: Boolean, val feePolicyKnown: Boolean)
@Serializable data class LiveHubParcel(val id: String, val relayPointId: String, val lockerId: String? = null, val orderId: String? = null,
    val status: String, val depositedAt: String? = null, val pickedUpAt: String? = null, val collectedAt: String? = null,
    val updatedAt: String, val history: List<LiveCustodyEvent> = emptyList(), val custody: LiveDeadlines? = null)
@Serializable data class LiveReturnEvent(val action: String, val createdAt: String, val actorUserId: String? = null, val metadata: String? = null)
@Serializable data class LiveHubReturn(val id: String, val orderId: String, val status: String, val relayPointId: String? = null,
    val requestedAt: String, val droppedAt: String? = null, val collectedBySequoAt: String? = null, val history: List<LiveReturnEvent> = emptyList())
@Serializable data class LiveHubNotification(val id: String, val title: String, val body: String, val createdAt: String,
    val readAt: String? = null, val archivedAt: String? = null)
data class LiveHubSnapshot(val site: AssignedHub, val slots: List<LiveHubSlot>, val parcels: List<LiveHubParcel>,
    val returns: List<LiveHubReturn>, val control: HubControlStateResponse, val openingHours: HubOpeningHoursResponse)
data class HubWriteIntent(val path: String, val body: JsonObject)
@Serializable data class LiveHubPreferences(val userId: String, val appFamily: String, val theme: String, val language: String,
    val quickScanOnOpen: Boolean, val soundFeedback: Boolean, val largeLockerLabels: Boolean)

/** All live rows are server-owned. No sample lockers, synthetic summaries or local-success fallback. */
class LiveScopedHubRepository(private val session: LiveHubSession) : HubRepository {
    private val accountScope = requireNotNull(session.scopeIdentity) { "Verify the operator session before reading relay data." }
    private suspend inline fun <reified T> call(path: String, method: HttpMethod = HttpMethod.Get, body: JsonElement? = null): T = session.request(path, method, body, accountScope)
    suspend fun sites(): List<AssignedHub> = call("/api/relay/parcels/sites")
    suspend fun snapshot(site: AssignedHub): LiveHubSnapshot {
        val encoded = site.id.encodeURLPathPart()
        val slots: List<LiveHubSlot> = call("/api/hub/lockers?relayPointId=$encoded")
        val parcelRows: List<LiveHubParcel> = call("/api/relay/parcels?relayPointId=$encoded")
        require(parcelRows.all { it.relayPointId == site.id }) { "The server returned another relay's data. Refresh your assignment." }
        // The list omits custody history; use the existing scoped detail read before publishing records.
        val parcels = parcelRows.map { parcel(site.id, it.id) }
        val returns: List<LiveHubReturn> = call("/api/returns/relay?relayPointId=$encoded")
        val control: HubControlStateResponse = call("/api/hub/control-state?relayPointId=$encoded")
        val hours: HubOpeningHoursResponse = call("/api/hub/opening-hours?relayPointId=$encoded")
        require(slots.all { it.relayPointId == site.id } && parcels.all { it.relayPointId == site.id } && returns.all { it.relayPointId == site.id } && control.relayPointId == site.id && hours.relayPointId == site.id) { "The server returned another relay's data. Refresh your assignment." }
        return LiveHubSnapshot(site, slots, parcels, returns, control, hours)
    }
    suspend fun parcel(siteId: String, parcelId: String): LiveHubParcel = call<LiveHubParcel>("/api/relay/parcels/${parcelId.encodeURLPathPart()}?relayPointId=${siteId.encodeURLPathPart()}").also {
        require(it.id == parcelId && it.relayPointId == siteId) { "The server returned another relay's parcel. Refresh your assignment." }
    }
    suspend fun scan(siteId: String, raw: String): HubScanResolutionResponse = call("/api/hub/scan/resolve", HttpMethod.Post,
        buildJsonObject { put("hubId", siteId); put("credential", raw); put("credentialType", "AUTO"); put("idempotencyKey", hubOperationId()) })
    fun releaseIntent(siteId: String, parcelId: String, raw: String, qr: Boolean, identityChecked: Boolean): HubWriteIntent {
        require(raw.isNotBlank() && raw.length <= if (qr) 256 else 32); require(identityChecked) { "Confirm the customer's identity before release." }
        val key = hubOperationId()
        return HubWriteIntent("/api/relay/parcels/${parcelId.encodeURLPathPart()}/release", buildJsonObject {
            put("relayPointId", siteId); put(if (qr) "rawQrNonce" else "rawNumericCode", raw)
            put("identityDocumentMatched", true); put("eventId", key); put("idempotencyKey", key)
        })
    }
    fun problemIntent(parcelId: String, note: String): HubWriteIntent {
        require(note.isNotBlank() && note.length <= 2000); val key = hubOperationId()
        return HubWriteIntent("/api/relay/parcels/${parcelId.encodeURLPathPart()}/problem", buildJsonObject { put("metadata", note); put("eventId", key); put("idempotencyKey", key) })
    }
    fun returnIntent(siteId: String, returnId: String, pin: String, note: String): HubWriteIntent {
        require(returnId.isNotBlank() && pin.matches(Regex("[0-9]{6}"))); val key = hubOperationId()
        return HubWriteIntent("/api/returns/${returnId.encodeURLPathPart()}/relay-dropoff", buildJsonObject {
            put("relayPointId", siteId); put("rawReturnPin", pin); put("idempotencyKey", key); if (note.isNotBlank()) put("metadata", note)
        })
    }
    suspend fun write(intent: HubWriteIntent) { call<JsonElement>(intent.path, HttpMethod.Post, intent.body) }
    suspend fun inbox(): List<LiveHubNotification> = call("/api/notifications/inbox?includeArchived=true&limit=50")
    suspend fun notification(id: String, action: String) {
        require(action in setOf("read", "archive", "unarchive"))
        session.response("/api/notifications/inbox/${id.encodeURLPathPart()}/${if (action == "unarchive") "archive" else action}",
            if (action == "read") HttpMethod.Patch else if (action == "unarchive") HttpMethod.Delete else HttpMethod.Post, null, accountScope)
    }
    suspend fun preferences(): LiveHubPreferences = call<LiveHubPreferences>("/api/preferences?appFamily=SEQUO_HUB").also {
        require(it.userId == session.user?.id && it.appFamily == "SEQUO_HUB") { "Preferences belong to another account." }
    }
    suspend fun savePreferences(theme: String, language: String, quickScan: Boolean, sound: Boolean, largeLabels: Boolean): LiveHubPreferences {
        call<JsonElement>("/api/preferences?appFamily=SEQUO_HUB", HttpMethod.Patch, buildJsonObject {
            put("theme", theme); put("language", language); put("quickScanOnOpen", quickScan); put("soundFeedback", sound); put("largeLockerLabels", largeLabels)
        })
        return preferences()
    }
    fun deletionIntent(confirmation: String, reason: String): HubWriteIntent {
        require(confirmation == "DELETE_MY_ACCOUNT" && reason.length <= 500)
        return HubWriteIntent("/api/account/deletion-requests", buildJsonObject {
            put("confirmation", confirmation); if(reason.isNotBlank()) put("reason", reason); put("idempotencyKey", hubOperationId())
        })
    }
    suspend fun saveHours(hours: HubOpeningHoursResponse) {
        call<JsonElement>("/api/hub/opening-hours", HttpMethod.Put, Json.encodeToJsonElement(SaveOpeningHoursRequest(hours.relayPointId, requireNotNull(hours.timezone?.takeIf { it.isNotBlank() }) { "Enter the relay time zone before saving opening hours." }, hours.weeklyHours, hours.exceptions, hubOperationId())))
    }
    override suspend fun loadHubState(hubId: String, strings: SequoStrings): HubOperationalState {
        val site = sites().singleOrNull { it.id == hubId } ?: error("This account has no matching relay assignment.")
        val actual = snapshot(site)
        val lockers = actual.slots.map { slot ->
            val parcel = actual.parcels.firstOrNull { it.lockerId == slot.lockerId && it.pickedUpAt == null && it.collectedAt == null }
            LockerUi(slot.lockerId, when(slot.status) { "AVAILABLE" -> LockerState.Free; "OCCUPIED" -> LockerState.Occupied; else -> LockerState.Maintenance },
                parcel?.status ?: slot.lockerCode, parcel?.id ?: slot.lockerCode, parcel?.depositedAt ?: "—", strings.validate, 0,
                storedSinceLabel = parcel?.depositedAt ?: "—", returnToSellerDue = parcel?.custody?.reviewDue == true,
                primaryAction = strings.validate, syncState = LockerSyncState.Synced)
        }
        return HubOperationalState(hubId, site.name, lockers, HubSummaryUi(actual.slots.count { it.status == "AVAILABLE" }, actual.slots.count { it.status == "OCCUPIED" },
            actual.parcels.count { it.custody?.storageFeeEligible == false }, 0, actual.parcels.count { it.custody?.storageFeeEligible == true }, actual.parcels.count { it.custody?.reviewDue == true },
            actual.returns.count { it.status == "DroppedAtRelay" }, actual.slots.count { it.status == "MAINTENANCE" }, strings.synced),
            HubControlUi(actual.control.effectiveMode in setOf(HubEffectiveMode.HUB_DISABLED, HubEffectiveMode.HUB_PAUSED), actual.control.hub.staffMessage),
            actual.parcels.flatMap { p -> p.history.map { HubAuditItem(it.createdAt, it.type, p.id, HubAuditTone.Neutral) } }, HubConnectionState.Online)
    }
    override suspend fun resolveManualCode(hubId: String, code: String): HubScanUiResult = scan(hubId, code).let {
        HubScanUiResult(when(it.workflowType) { HubWorkflowType.CUSTOMER_PICKUP -> HubWorkflowTypeUi.CustomerPickup; HubWorkflowType.CUSTOMER_RETURN_DROPOFF -> HubWorkflowTypeUi.CustomerReturnDropoff; else -> HubWorkflowTypeUi.Unknown }, it.parcelId ?: it.returnId ?: it.displayReference.orEmpty(), it.lockerId, it.identityVerificationRequired, it.feeDueCfa, it.blockingReason)
    }
    override suspend fun setLockerMaintenance(hubId: String, lockerId: String, unavailable: Boolean): HubOperationResult {
        call<JsonElement>("/api/hub/lockers/${lockerId.encodeURLPathPart()}/availability", HttpMethod.Post,
            Json.encodeToJsonElement(LockerAvailabilityRequest(hubId, if(unavailable) HubLockerStatus.MAINTENANCE else HubLockerStatus.AVAILABLE,
                if(unavailable) HubLockerAvailabilityReason.OTHER else HubLockerAvailabilityReason.OPERATOR_CONFIRMED_AVAILABLE, idempotencyKey = hubOperationId())))
        return HubOperationResult(true, "Server recorded the slot state.")
    }
    override suspend fun recordReturnDropoff(hubId: String, returnId: String, rawReturnPin: String, conditionNote: String): HubOperationResult {
        write(returnIntent(hubId, returnId, rawReturnPin, conditionNote)); return HubOperationResult(true, "Server recorded return intake.")
    }
    override suspend fun loadCollectionManifest(hubId: String, manifestCode: String): HubCollectionManifestUi = error("Batch collection is not supported here. Use SEQUO's authorized collection workflow.")
    override suspend fun recordCollectionHandoff(hubId: String, manifestCode: String, packageIds: List<String>) = HubOperationResult(false, "No batch collection was recorded.")
    override fun close() = Unit
}
