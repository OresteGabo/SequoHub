package dev.orestegabo.sequohub.feature.handover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.AppScroll
import dev.orestegabo.sequohub.core.designsystem.component.BadgeTone
import dev.orestegabo.sequohub.core.designsystem.component.CodeEntryRow
import dev.orestegabo.sequohub.core.designsystem.component.DetailRow
import dev.orestegabo.sequohub.core.designsystem.component.MinimalCard
import dev.orestegabo.sequohub.core.designsystem.component.PrimaryActionButton
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader
import dev.orestegabo.sequohub.core.designsystem.component.cleanCode
import dev.orestegabo.sequohub.core.hub.HubConnectionState
import dev.orestegabo.sequohub.core.hub.HubScanUiResult
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoverScreen(
    pickupResolution: HubScanUiResult? = null,
    returnResolution: HubScanUiResult? = null,
    connectionState: HubConnectionState = HubConnectionState.LocalFallback,
    onValidatePickup: (String) -> Unit,
    onCollectFeeAndOpen: () -> Unit,
    onValidateReturn: (String) -> Unit,
    onReceiveReturn: (String) -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(HandoverMode.Pickup) }
    var code by rememberSaveable { mutableStateOf("") }
    var idVerified by rememberSaveable { mutableStateOf(false) }
    var returnConditionNote by rememberSaveable { mutableStateOf("") }
    var confirmInProgress by rememberSaveable { mutableStateOf(false) }
    val strings = LocalSequoStrings.current

    AppScroll {
        TopHeader(
            eyebrow = strings.navActions,
            title = strings.handover,
            subtitle = strings.handoverSubtitle,
            status = strings.idRequired,
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            HandoverMode.entries.forEachIndexed { index, item ->
                SegmentedButton(
                    selected = item == mode,
                    onClick = {
                        mode = item
                        code = ""
                        idVerified = false
                        returnConditionNote = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = HandoverMode.entries.size,
                    ),
                    label = { Text(item.localizedLabel) },
                )
            }
        }

        MinimalCard {
            Text(
                text = if (mode == HandoverMode.Pickup) strings.customerPickup else strings.customerReturn,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
            )
            CodeEntryRow(
                value = code,
                placeholder = if (mode == HandoverMode.Pickup) strings.pickupCode else strings.returnCode,
                onValueChange = { code = cleanCode(it, max = 18) },
                buttonLabel = strings.validate,
                onSubmit = {
                    val submittedCode = code
                    code = ""
                    idVerified = false
                    returnConditionNote = ""
                    if (mode == HandoverMode.Pickup) {
                        onValidatePickup(submittedCode)
                    } else {
                        onValidateReturn(submittedCode)
                    }
                },
            )
        }

        if (mode == HandoverMode.Pickup) {
            PickupFeeModal(resolution = pickupResolution, connectionState = connectionState, idVerified = idVerified, confirmInProgress = confirmInProgress, onIdVerifiedChange = { idVerified = it }, onCollectFeeAndOpen = { confirmInProgress = true; onCollectFeeAndOpen(); confirmInProgress = false })
        } else {
            ReturnValidationCard(
                resolution = returnResolution,
                idVerified = idVerified,
                conditionNote = returnConditionNote,
                onIdVerifiedChange = { idVerified = it },
                onConditionNoteChange = { returnConditionNote = sanitizeConditionNote(it) },
                onReceiveReturn = { onReceiveReturn(returnConditionNote) },
            )
        }
    }
}

@Composable
fun FeeNotice(amount: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val strings = LocalSequoStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SequoHubShapes.Small,
        color = colorScheme.tertiaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, colorScheme.tertiary.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = strings.extraStorageFee,
                    color = colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = strings.collectBeforeRelease,
                    color = colorScheme.onTertiaryContainer.copy(alpha = 0.74f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(
                text = "${amount.formatCfa()} CFA",
                color = colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PickupFeeModal(
    resolution: HubScanUiResult?,
    connectionState: HubConnectionState,
    idVerified: Boolean,
    confirmInProgress: Boolean,
    onIdVerifiedChange: (Boolean) -> Unit,
    onCollectFeeAndOpen: () -> Unit,
) {
    MinimalCard {
        val strings = LocalSequoStrings.current
        val blockingMessage = resolution?.blockingReason?.let { handoverBlockingMessage(it) }
        val canConfirm = resolution != null && blockingMessage == null && idVerified && connectionState == HubConnectionState.Online && !confirmInProgress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.pickupReady,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = resolution?.let { "Locker ${it.lockerId ?: "-"} - ${it.displayReference}" } ?: "Locker A04 - Package SQ-4482",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            StatusBadge(label = if (blockingMessage == null) strings.releaseReady else strings.blocked, tone = if (blockingMessage == null) BadgeTone.Active else BadgeTone.Hold)
        }

        DetailRow(label = strings.packageAndLocker, value = resolution?.let { "${it.displayReference} / ${it.lockerId ?: "-"}" } ?: strings.invalidPickupCode)
        DetailRow(label = strings.storageFee, value = "${(resolution?.feeDueCfa ?: 0).formatCfa()} CFA")
        DetailRow(label = strings.source, value = strings.backendStorageRules)
        blockingMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (connectionState != HubConnectionState.Online) {
            Text(
                text = strings.backendRequiredForRelease,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if ((resolution?.feeDueCfa ?: 0) > 0) {
            FeeNotice(amount = resolution?.feeDueCfa ?: 0)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = idVerified, onCheckedChange = onIdVerifiedChange)
            Text(strings.verifyCustomerId, style = MaterialTheme.typography.bodyMedium)
        }
        PrimaryActionButton(
            label = strings.confirmHandover,
            enabled = canConfirm,
            onClick = onCollectFeeAndOpen,
        )
    }
}

@Composable
private fun ReturnValidationCard(
    resolution: HubScanUiResult?,
    idVerified: Boolean,
    conditionNote: String,
    onIdVerifiedChange: (Boolean) -> Unit,
    onConditionNoteChange: (String) -> Unit,
    onReceiveReturn: () -> Unit,
) {
    MinimalCard {
        val strings = LocalSequoStrings.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.returnDropOff,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = strings.returnWindowHint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            StatusBadge(label = "72h", tone = BadgeTone.New)
        }

        DetailRow(label = strings.idCheck, value = if (resolution?.identityVerificationRequired != false) strings.requiredBeforeIntake else strings.clear)
        DetailRow(label = strings.refundDecision, value = strings.sequoFinalValidationOnly)
        DetailRow(label = strings.lockerAssigned, value = resolution?.lockerId ?: "C05")
        OutlinedTextField(
            value = conditionNote,
            onValueChange = onConditionNoteChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(strings.visibleConditionNote) },
            placeholder = { Text(strings.conditionNotePlaceholder) },
            minLines = 2,
            maxLines = 3,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = idVerified, onCheckedChange = onIdVerifiedChange)
            Text(strings.verifyCustomerId, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = strings.returnStoredForCollection,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        PrimaryActionButton(
            label = strings.receiveReturn,
            enabled = idVerified && conditionNote.isNotBlank() && resolution != null,
            onClick = onReceiveReturn,
        )
    }
}

private fun sanitizeConditionNote(input: String): String =
    input
        .filter { it.isLetterOrDigit() || it.isWhitespace() || it in ".,'/-()" }
        .take(160)

@Composable
private fun handoverBlockingMessage(reason: String): String {
    val strings = LocalSequoStrings.current
    return when (reason) {
        "credential_already_used", "credential_expired" -> strings.alreadyUsedOrExpired
        "credential_not_found", "parcel_not_releasable" -> strings.invalidPickupCode
        else -> strings.invalidPickupCode
    }
}

private enum class HandoverMode {
    Pickup,
    Return,
}

private val HandoverMode.localizedLabel: String
    @Composable get() {
        val strings = LocalSequoStrings.current
        return when (this) {
            HandoverMode.Pickup -> strings.pickup
            HandoverMode.Return -> strings.returnPackage
        }
    }

private fun Int.formatCfa(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()
