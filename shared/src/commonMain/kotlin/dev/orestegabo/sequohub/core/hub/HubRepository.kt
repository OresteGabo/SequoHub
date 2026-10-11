package dev.orestegabo.sequohub.core.hub

import dev.orestegabo.sequohub.core.localization.SequoStrings
import dev.orestegabo.sequohub.feature.hub.LockerState
import dev.orestegabo.sequohub.feature.hub.LockerSyncState
import dev.orestegabo.sequohub.feature.hub.LockerUi
import dev.orestegabo.sequohub.feature.hub.sampleLockers

interface HubRepository {
    suspend fun loadHubState(hubId: String, strings: SequoStrings): HubOperationalState
    suspend fun resolveManualCode(hubId: String, code: String): HubScanUiResult
    suspend fun setLockerMaintenance(hubId: String, lockerId: String, unavailable: Boolean): HubOperationResult
    suspend fun recordReturnDropoff(hubId: String, returnId: String, rawReturnPin: String, conditionNote: String): HubOperationResult
    fun close()
}

class ApiHubRepository(
    private val apiClient: HubApiClient,
) : HubRepository {
    override suspend fun loadHubState(hubId: String, strings: SequoStrings): HubOperationalState {
        val fallbackLockers = sampleLockers(strings)
        val summary = apiClient.summary(hubId)
        val control = runCatching { apiClient.controlState(hubId) }.getOrNull()
        val hubBlocked = control?.effectiveMode in setOf(HubEffectiveMode.HUB_PAUSED, HubEffectiveMode.HUB_DISABLED, HubEffectiveMode.SERVICE_RESTRICTED)
        return HubOperationalState(
            hubId = hubId,
            displayName = "Lome Relay 04",
            lockers = fallbackLockers.withSummary(summary, strings),
            summary = summary.toUi(strings),
            control = HubControlUi(
                hubBlockedBySequo = hubBlocked,
                staffMessage = control?.hub?.staffMessage,
            ),
            auditItems = auditFromSummary(summary, strings),
            connection = HubConnectionState.Online,
        )
    }

    override suspend fun resolveManualCode(hubId: String, code: String): HubScanUiResult {
        val response = apiClient.resolveScan(
            hubId = hubId,
            credential = code,
            credentialType = HubScanCredentialType.AUTO,
        )
        return response.toUiResult()
    }

    override suspend fun setLockerMaintenance(hubId: String, lockerId: String, unavailable: Boolean): HubOperationResult {
        apiClient.updateLockerAvailability(
            lockerId = lockerId,
            relayPointId = hubId,
            status = if (unavailable) HubLockerStatus.MAINTENANCE else HubLockerStatus.AVAILABLE,
            reason = if (unavailable) HubLockerAvailabilityReason.OTHER else HubLockerAvailabilityReason.OPERATOR_CONFIRMED_AVAILABLE,
        )
        return HubOperationResult(success = true, safeMessage = "Locker status synced.")
    }

    override suspend fun recordReturnDropoff(hubId: String, returnId: String, rawReturnPin: String, conditionNote: String): HubOperationResult {
        apiClient.recordReturnDropoff(returnId = returnId, relayPointId = hubId, rawReturnPin = rawReturnPin)
        return HubOperationResult(success = true, safeMessage = "Return drop-off synced.")
    }

    override fun close() {
        apiClient.close()
    }
}

class FakeHubRepository : HubRepository {
    override suspend fun loadHubState(hubId: String, strings: SequoStrings): HubOperationalState {
        val lockers = sampleLockers(strings)
        val occupied = lockers.count { it.state == LockerState.Occupied }
        val feesDue = lockers.count { it.feeDueCfa > 0 }
        return HubOperationalState(
            hubId = hubId,
            displayName = "Lome Relay 04",
            lockers = lockers,
            summary = HubSummaryUi(
                freeLockerCount = lockers.count { it.state == LockerState.Free },
                occupiedLockerCount = occupied,
                feeDuePackageCount = feesDue,
                pendingSequoCollectionCount = 2,
                openIncidentCount = lockers.count { it.state == LockerState.Maintenance },
                lastSuccessfulSyncLabel = strings.local,
            ),
            control = HubControlUi(
                hubBlockedBySequo = false,
                staffMessage = null,
            ),
            auditItems = listOf(
                HubAuditItem("09:42", strings.pickupReady, "A04 - ${strings.collectBeforeRelease}", HubAuditTone.Active),
                HubAuditItem("09:18", strings.receiveReturn, "B03 - ${strings.sealedOk}", HubAuditTone.New),
                HubAuditItem("08:55", strings.needsAttention, "D02 - ${strings.damaged}", HubAuditTone.Hold),
                HubAuditItem("08:20", strings.packageStored, "COL-1042", HubAuditTone.Neutral),
            ),
            connection = HubConnectionState.LocalFallback,
        )
    }

    override suspend fun resolveManualCode(hubId: String, code: String): HubScanUiResult {
        val display = code.takeLast(6).ifBlank { "LOCAL" }
        return HubScanUiResult(
            workflowType = when {
                code.startsWith("RET", ignoreCase = true) -> HubWorkflowTypeUi.CustomerReturnDropoff
                code.startsWith("COL", ignoreCase = true) -> HubWorkflowTypeUi.SequoCollection
                code.startsWith("PKG", ignoreCase = true) || code.startsWith("SQ", ignoreCase = true) -> HubWorkflowTypeUi.PackageIntake
                else -> HubWorkflowTypeUi.CustomerPickup
            },
            displayReference = "...$display",
            lockerId = "A04",
            identityVerificationRequired = true,
            feeDueCfa = if (code.endsWith("FEE", ignoreCase = true)) 1000 else 0,
            blockingReason = null,
        )
    }

    override suspend fun setLockerMaintenance(hubId: String, lockerId: String, unavailable: Boolean): HubOperationResult =
        HubOperationResult(success = true, safeMessage = "Saved locally until hub sync is available.")

    override suspend fun recordReturnDropoff(hubId: String, returnId: String, rawReturnPin: String, conditionNote: String): HubOperationResult =
        HubOperationResult(success = true, safeMessage = "Return stored locally until hub sync is available.")

    override fun close() = Unit
}

private fun HubSummaryResponse.toUi(strings: SequoStrings): HubSummaryUi = HubSummaryUi(
    freeLockerCount = freeLockerCount.toInt(),
    occupiedLockerCount = occupiedLockerCount.toInt(),
    feeDuePackageCount = feeDuePackageCount.toInt(),
    pendingSequoCollectionCount = pendingSequoCollectionCount.toInt(),
    openIncidentCount = openIncidentCount.toInt(),
    lastSuccessfulSyncLabel = lastSuccessfulSyncTimestamp ?: strings.synced,
)

private fun HubScanResolutionResponse.toUiResult(): HubScanUiResult = HubScanUiResult(
    workflowType = when (workflowType) {
        HubWorkflowType.CUSTOMER_PICKUP -> HubWorkflowTypeUi.CustomerPickup
        HubWorkflowType.PACKAGE_INTAKE -> HubWorkflowTypeUi.PackageIntake
        HubWorkflowType.CUSTOMER_RETURN_DROPOFF -> HubWorkflowTypeUi.CustomerReturnDropoff
        HubWorkflowType.SEQUO_COLLECTION -> HubWorkflowTypeUi.SequoCollection
        HubWorkflowType.UNKNOWN -> HubWorkflowTypeUi.Unknown
    },
    displayReference = displayReference ?: parcelId ?: returnId ?: collectionBatchId ?: "-",
    lockerId = lockerId,
    identityVerificationRequired = identityVerificationRequired,
    feeDueCfa = feeDueCfa,
    blockingReason = blockingReason,
)

private fun List<LockerUi>.withSummary(summary: HubSummaryResponse, strings: SequoStrings): List<LockerUi> {
    val occupiedTarget = summary.occupiedLockerCount.toInt().coerceIn(0, size)
    return mapIndexed { index, locker ->
        when {
            index < occupiedTarget && locker.state == LockerState.Free -> locker.copy(
                state = LockerState.Occupied,
                packageLabel = strings.packageStored,
                reference = "SYNC-${index + 1}",
                ageLabel = strings.pickupReady,
                nextAction = strings.validate,
                primaryAction = strings.validate,
                syncState = LockerSyncState.Synced,
            )
            index >= occupiedTarget && locker.state == LockerState.Occupied -> locker.copy(
                state = LockerState.Free,
                packageLabel = strings.clear,
                reference = strings.clear,
                ageLabel = strings.clear,
                nextAction = strings.assign,
                feeDueCfa = 0,
                primaryAction = strings.assign,
                syncState = LockerSyncState.Synced,
            )
            else -> locker.copy(syncState = LockerSyncState.Synced)
        }
    }
}

private fun auditFromSummary(summary: HubSummaryResponse, strings: SequoStrings): List<HubAuditItem> = listOf(
    HubAuditItem(strings.today, strings.synced, summary.lastSuccessfulSyncTimestamp ?: strings.local, HubAuditTone.Active),
    HubAuditItem(strings.today, strings.occupied, summary.occupiedLockerCount.toString(), HubAuditTone.Neutral),
    HubAuditItem(strings.today, strings.feesDue, summary.feeDuePackageCount.toString(), HubAuditTone.Hold),
)
