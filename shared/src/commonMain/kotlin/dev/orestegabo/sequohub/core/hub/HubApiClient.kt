package dev.orestegabo.sequohub.core.hub

import dev.orestegabo.sequohub.core.network.NetworkConfig
import dev.orestegabo.sequohub.core.network.SequoApiException
import dev.orestegabo.sequohub.core.network.SequoNetworkException
import dev.orestegabo.sequohub.core.network.createSequoHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class HubApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoHttpClient(baseUrl),
) {
    suspend fun summary(hubId: String): HubSummaryResponse = safeCall {
        httpClient.get("${baseUrl.trimEnd('/')}/api/hub/summary") {
            parameter("hubId", hubId)
        }.body()
    }

    suspend fun controlState(relayPointId: String): HubControlStateResponse = safeCall {
        httpClient.get("${baseUrl.trimEnd('/')}/api/hub/control-state") {
            parameter("relayPointId", relayPointId)
        }.body()
    }

    suspend fun resolveScan(
        hubId: String,
        credential: String,
        credentialType: HubScanCredentialType = HubScanCredentialType.AUTO,
        idempotencyKey: String? = null,
    ): HubScanResolutionResponse = safeCall {
        httpClient.post("${baseUrl.trimEnd('/')}/api/hub/scan/resolve") {
            setBody(
                HubScanResolveRequest(
                    hubId = hubId,
                    credential = credential,
                    credentialType = credentialType,
                    idempotencyKey = idempotencyKey,
                )
            )
        }.body()
    }

    suspend fun updateLockerAvailability(
        lockerId: String,
        relayPointId: String,
        status: HubLockerStatus,
        reason: HubLockerAvailabilityReason? = null,
        expectedAvailableAt: String? = null,
        idempotencyKey: String? = null,
    ): LockerAvailabilityResponse = safeCall {
        httpClient.post("${baseUrl.trimEnd('/')}/api/hub/lockers/$lockerId/availability") {
            setBody(
                LockerAvailabilityRequest(
                    relayPointId = relayPointId,
                    status = status,
                    reason = reason,
                    expectedAvailableAt = expectedAvailableAt,
                    idempotencyKey = idempotencyKey,
                )
            )
        }.body()
    }

    suspend fun openingHours(relayPointId: String): HubOpeningHoursResponse = safeCall {
        httpClient.get("${baseUrl.trimEnd('/')}/api/hub/opening-hours") {
            parameter("relayPointId", relayPointId)
        }.body()
    }

    suspend fun saveOpeningHours(request: SaveOpeningHoursRequest): HubOpeningHoursResponse = safeCall {
        httpClient.put("${baseUrl.trimEnd('/')}/api/hub/opening-hours") {
            setBody(request)
        }.body()
    }

    fun close() {
        httpClient.close()
    }
}

private suspend fun <T> safeCall(block: suspend () -> T): T =
    try {
        block()
    } catch (error: SequoApiException) {
        throw HubApiException(error.statusCode, safeHubMessage(error.statusCode), error)
    } catch (error: SequoNetworkException) {
        throw HubApiException(null, "Unable to reach Sequo service. Use local fallback until sync is available.", error)
    }

private fun safeHubMessage(statusCode: Int): String = when (statusCode) {
    401 -> "Sign in again to sync hub operations."
    403 -> "This account is not allowed to manage this relay point."
    404 -> "No matching hub record was found."
    409 -> "This hub action conflicts with the latest backend state. Refresh and try again."
    429 -> "Too many hub requests. Please wait and try again."
    else -> "Hub sync could not be completed. Try again."
}

class HubApiException(
    val statusCode: Int?,
    val safeMessage: String,
    override val cause: Throwable? = null,
) : Exception(safeMessage, cause)

@Serializable
data class HubSummaryResponse(
    val hubId: String,
    val freeLockerCount: Long = 0,
    val occupiedLockerCount: Long = 0,
    val feeDuePackageCount: Long = 0,
    val pendingSequoCollectionCount: Long = 0,
    val openIncidentCount: Long = 0,
    val lastSuccessfulSyncTimestamp: String? = null,
)

@Serializable
data class HubScanResolveRequest(
    val hubId: String,
    val credential: String,
    val credentialType: HubScanCredentialType = HubScanCredentialType.AUTO,
    val idempotencyKey: String? = null,
)

@Serializable
data class HubScanResolutionResponse(
    val workflowType: HubWorkflowType,
    val displayReference: String? = null,
    val parcelId: String? = null,
    val returnId: String? = null,
    val collectionBatchId: String? = null,
    val lockerId: String? = null,
    val identityVerificationRequired: Boolean = false,
    val feeDueCfa: Int = 0,
    val blockingReason: String? = null,
)

@Serializable
enum class HubScanCredentialType {
    QR_TOKEN,
    PICKUP_CODE,
    PACKAGE_CODE,
    RETURN_ID,
    COLLECTION_BATCH_CODE,
    AUTO,
}

@Serializable
enum class HubWorkflowType {
    CUSTOMER_PICKUP,
    PACKAGE_INTAKE,
    CUSTOMER_RETURN_DROPOFF,
    SEQUO_COLLECTION,
    UNKNOWN,
}

@Serializable
data class LockerAvailabilityRequest(
    val relayPointId: String,
    val status: HubLockerStatus,
    val reason: HubLockerAvailabilityReason? = null,
    val expectedAvailableAt: String? = null,
    val idempotencyKey: String? = null,
)

@Serializable
data class LockerAvailabilityResponse(
    val lockerId: String,
    val relayPointId: String,
    val lockerCode: String,
    val status: HubLockerStatus,
    val reason: HubLockerAvailabilityReason? = null,
    val expectedAvailableAt: String? = null,
    val updatedAt: String,
)

@Serializable
enum class HubLockerStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE,
}

@Serializable
enum class HubLockerAvailabilityReason {
    BROKEN_DOOR,
    JAMMED_LOCK,
    DIRTY,
    WRONG_CONTENTS,
    OTHER,
    OPERATOR_CONFIRMED_AVAILABLE,
    SYSTEM_OCCUPIED,
}

@Serializable
data class HubControlStateResponse(
    val relayPointId: String,
    val effectiveMode: HubEffectiveMode,
    val hub: HubControlTargetStateResponse,
    val services: List<HubControlTargetStateResponse> = emptyList(),
    val generatedAt: String,
)

@Serializable
data class HubControlTargetStateResponse(
    val target: String,
    val status: HubControlStatus,
    val reasonCode: String? = null,
    val staffMessage: String? = null,
    val customerMessage: String? = null,
    val effectiveUntil: String? = null,
    val source: String? = null,
    val actorType: String? = null,
    val decidedAt: String? = null,
)

@Serializable
enum class HubControlStatus {
    ACTIVE,
    PAUSED,
    DISABLED,
}

@Serializable
enum class HubEffectiveMode {
    NORMAL,
    HUB_PAUSED,
    HUB_DISABLED,
    INTAKE_PAUSED_PICKUP_ALLOWED,
    SERVICE_RESTRICTED,
}

@Serializable
data class HubOpeningHoursResponse(
    val relayPointId: String,
    val timezone: String? = null,
    val weeklyHours: List<WeeklyOpeningHourResponse> = emptyList(),
    val exceptions: List<OpeningHourExceptionResponse> = emptyList(),
)

@Serializable
data class WeeklyOpeningHourResponse(
    val dayOfWeek: String,
    val isOpen: Boolean,
    val opensAt: String? = null,
    val closesAt: String? = null,
)

@Serializable
data class OpeningHourExceptionResponse(
    val date: String,
    val isClosed: Boolean,
    val opensAt: String? = null,
    val closesAt: String? = null,
    val reason: String? = null,
    val effectiveUntil: String? = null,
)

@Serializable
data class SaveOpeningHoursRequest(
    val relayPointId: String,
    val timezone: String,
    val weeklyHours: List<WeeklyOpeningHourResponse>,
    val exceptions: List<OpeningHourExceptionResponse> = emptyList(),
    val idempotencyKey: String? = null,
)
