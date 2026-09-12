package dev.orestegabo.sequohub.feature.handover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import dev.orestegabo.sequohub.core.designsystem.component.SelectableChip
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader
import dev.orestegabo.sequohub.core.designsystem.component.cleanCode

@Composable
fun HandoverScreen(
    onValidatePickup: (String) -> Unit,
    onCollectFeeAndOpen: () -> Unit,
    onValidateReturn: (String) -> Unit,
    onReceiveReturn: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(HandoverMode.Pickup) }
    var code by rememberSaveable { mutableStateOf("") }

    AppScroll {
        TopHeader(
            eyebrow = "Actions",
            title = "Handover",
            subtitle = "Pickup and return workflows",
            status = "ID required",
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HandoverMode.entries.forEach { item ->
                SelectableChip(
                    modifier = Modifier.weight(1f),
                    label = item.label,
                    selected = item == mode,
                    onClick = {
                        mode = item
                        code = ""
                    },
                )
            }
        }

        MinimalCard {
            Text(
                text = if (mode == HandoverMode.Pickup) "Customer pickup" else "Customer return",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
            )
            CodeEntryRow(
                value = code,
                placeholder = if (mode == HandoverMode.Pickup) "Pickup code" else "Return code",
                onValueChange = { code = cleanCode(it, max = 18) },
                buttonLabel = "Validate",
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
                    text = "Extra storage fee",
                    color = colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = "Collect before package release.",
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Pickup ready",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Locker A04 - Package SQ-4482",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            StatusBadge(label = "Fee due", tone = BadgeTone.Fee)
        }

        DetailRow(label = "Free window", value = "Ended 2 days ago")
        DetailRow(label = "Storage fee", value = "1,000 CFA")
        DetailRow(label = "Source", value = "Backend storage rules")

        FeeNotice(amount = 1000)
        PrimaryActionButton(label = "Collect Fee & Open Locker", onClick = onCollectFeeAndOpen)
    }
}

@Composable
private fun ReturnValidationCard(
    onReceiveReturn: () -> Unit,
) {
    MinimalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Return drop-off",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Validate within the 72-hour return window.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            StatusBadge(label = "72h", tone = BadgeTone.New)
        }

        DetailRow(label = "ID check", value = "Required before intake")
        DetailRow(label = "Refund decision", value = "Sequo final validation only")
        PrimaryActionButton(label = "Receive Return", onClick = onReceiveReturn)
    }
}

private enum class HandoverMode(val label: String) {
    Pickup(label = "Pickup"),
    Return(label = "Return"),
}

private fun Int.formatCfa(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()
