package dev.orestegabo.sequohub.feature.handover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoverScreen(
    onValidatePickup: (String) -> Unit,
    onCollectFeeAndOpen: () -> Unit,
    onValidateReturn: (String) -> Unit,
    onReceiveReturn: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(HandoverMode.Pickup) }
    var code by rememberSaveable { mutableStateOf("") }
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
                    if (mode == HandoverMode.Pickup) {
                        onValidatePickup(code)
                    } else {
                        onValidateReturn(code)
                    }
                },
            )
        }

        if (mode == HandoverMode.Pickup) {
            PickupFeeModal(onCollectFeeAndOpen = onCollectFeeAndOpen)
        } else {
            ReturnValidationCard(onReceiveReturn = onReceiveReturn)
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
    onCollectFeeAndOpen: () -> Unit,
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
                    text = strings.pickupReady,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Locker A04 - Package SQ-4482",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            StatusBadge(label = strings.feeDue, tone = BadgeTone.Fee)
        }

        DetailRow(label = strings.freeWindow, value = strings.endedTwoDaysAgo)
        DetailRow(label = strings.storageFee, value = "1,000 CFA")
        DetailRow(label = strings.source, value = strings.backendStorageRules)

        FeeNotice(amount = 1000)
        PrimaryActionButton(label = strings.collectFeeOpenLocker, onClick = onCollectFeeAndOpen)
    }
}

@Composable
private fun ReturnValidationCard(
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

        DetailRow(label = strings.idCheck, value = strings.requiredBeforeIntake)
        DetailRow(label = strings.refundDecision, value = strings.sequoFinalValidationOnly)
        PrimaryActionButton(label = strings.receiveReturn, onClick = onReceiveReturn)
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
