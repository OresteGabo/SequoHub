package dev.orestegabo.sequohub.core.hub

import dev.orestegabo.sequohub.feature.hub.LockerUi

const val DemoHubId = "relay-lome-04"

data class HubOperationalState(
    val hubId: String,
    val displayName: String,
    val lockers: List<LockerUi>,
    val summary: HubSummaryUi,
    val control: HubControlUi,
    val auditItems: List<HubAuditItem>,
    val connection: HubConnectionState,
)

data class HubSummaryUi(
    val freeLockerCount: Int,
    val occupiedLockerCount: Int,
    val underGracePackageCount: Int,
    val feeDuePackageCount: Int,
    val overduePackageCount: Int,
    val returnToSellerDueCount: Int,
    val pendingSequoCollectionCount: Int,
    val openIncidentCount: Int,
    val lastSuccessfulSyncLabel: String,
)

data class HubControlUi(
    val hubBlockedBySequo: Boolean,
    val staffMessage: String?,
)

data class HubAuditItem(
    val timeLabel: String,
    val title: String,
    val subtitle: String,
    val tone: HubAuditTone,
)

enum class HubAuditTone {
    Active,
    New,
    Hold,
    Neutral,
}

enum class HubConnectionState {
    Online,
    LocalFallback,
    Offline,
}

data class HubScanUiResult(
    val workflowType: HubWorkflowTypeUi,
    val displayReference: String,
    val lockerId: String?,
    val identityVerificationRequired: Boolean,
    val feeDueCfa: Int,
    val blockingReason: String?,
)

enum class HubWorkflowTypeUi {
    CustomerPickup,
    PackageIntake,
    CustomerReturnDropoff,
    SequoCollection,
    Unknown,
}

data class HubOperationResult(
    val success: Boolean,
    val safeMessage: String,
)

data class HubCollectionManifestUi(
    val manifestCode: String,
    val packages: List<HubCollectionPackageUi>,
    val localOnly: Boolean = true,
)

data class HubCollectionPackageUi(
    val id: String,
    val lockerId: String,
    val label: String,
)
