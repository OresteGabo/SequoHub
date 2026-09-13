package dev.orestegabo.sequohub.feature.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.orestegabo.sequohub.core.designsystem.component.AppScroll
import dev.orestegabo.sequohub.core.designsystem.component.BadgeTone
import dev.orestegabo.sequohub.core.designsystem.component.DetailRow
import dev.orestegabo.sequohub.core.designsystem.component.MetricCard
import dev.orestegabo.sequohub.core.designsystem.component.MinimalCard
import dev.orestegabo.sequohub.core.designsystem.component.Package2
import dev.orestegabo.sequohub.core.designsystem.component.PrimaryActionButton
import dev.orestegabo.sequohub.core.designsystem.component.SectionTitle
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.designsystem.component.StatusColors
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader
import dev.orestegabo.sequohub.core.designsystem.component.badgeColors
import dev.orestegabo.sequohub.core.designsystem.component.statusColors
import dev.orestegabo.sequohub.feature.handover.FeeNotice
import kotlin.text.uppercase

@Composable
fun HubDashboard(
    lockers: List<LockerUi>,
    hubBlockedBySequo: Boolean = false,
    onLockerTap: (LockerUi) -> Unit,
) {
    var selectedDashboardTab by remember { mutableIntStateOf(0) }
    var lockerSearch by remember { mutableStateOf("") }
    val dashboardTabs = listOf("Grid", "Attention")
    val visibleLockers = if (lockerSearch.isBlank()) {
        lockers
    } else {
        lockers.filter { it.id.contains(lockerSearch.trim(), ignoreCase = true) }
    }
    val pendingSyncCount = lockers.count { it.syncState != LockerSyncState.Synced }
    val attentionLockers = visibleLockers.filter { it.needsAttention }

    AppScroll {
        TopHeader(
            eyebrow = "Point de Relai",
            title = "Hub dashboard",
            subtitle = "Lomé Relay 04",
            status = if (hubBlockedBySequo) {
                "Blocked"
            } else if (pendingSyncCount == 0) {
                "Synced"
            } else {
                "$pendingSyncCount pending"
            },
        )

        PrimaryTabRow(
            selectedTabIndex = selectedDashboardTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            dashboardTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedDashboardTab == index,
                    onClick = { selectedDashboardTab = index },
                    text = { Text(title) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Occupied",
                value = "17",
                note = "25 lockers",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Fees due",
                value = "3",
                note = "today",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Sync",
                value = pendingSyncCount.toString(),
                note = if (pendingSyncCount == 0) "clear" else "pending",
            )
        }

        LockerSearchCard(
            lockerSearch = lockerSearch,
            onLockerSearchChange = { lockerSearch = it.take(3).uppercase() },
        )

        if (selectedDashboardTab == 0) {
            LockerGridCard(
                lockers = visibleLockers,
                hubBlockedBySequo = hubBlockedBySequo,
                onLockerTap = onLockerTap,
            )
        } else {
            AttentionCard(
                lockers = attentionLockers,
                onLockerTap = onLockerTap,
            )
        }
    }
}

@Composable
private fun LockerSearchCard(
    lockerSearch: String,
    onLockerSearchChange: (String) -> Unit,
) {
    MinimalCard {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lockerSearch,
            onValueChange = onLockerSearchChange,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                )
            },
            placeholder = { Text("Search locker, e.g. A04") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
            ),
            shape = SequoHubShapes.Small,
        )
    }
}

@Composable
private fun LockerGridCard(
    lockers: List<LockerUi>,
    hubBlockedBySequo: Boolean,
    onLockerTap: (LockerUi) -> Unit,
) {
    MinimalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle("Locker grid")
                Text(
                    text = "Tap a box to inspect package age and status.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            StatusBadge(
                label = if (hubBlockedBySequo) "Blocked" else "5x5",
                tone = if (hubBlockedBySequo) BadgeTone.Hold else BadgeTone.Neutral,
            )
        }

        Box {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lockers.chunked(5).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { locker ->
                            LockerCell(
                                modifier = Modifier.weight(1f),
                                locker = locker,
                                hubBlockedBySequo = hubBlockedBySequo,
                                onClick = {
                                    if (!hubBlockedBySequo) {
                                        onLockerTap(locker)
                                    }
                                },
                            )
                        }
                        repeat(5 - row.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (hubBlockedBySequo) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 18.dp),
                    shape = SequoHubShapes.Card,
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.32f)),
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Blocked by Sequo",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Sequo has temporarily paused new drop-offs at this hub. Customers and riders may still collect packages already stored here, but no new packages can be accepted until the hub is reactivated.",
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.76f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        LockerLegend()
    }
}

@Composable
private fun AttentionCard(
    lockers: List<LockerUi>,
    onLockerTap: (LockerUi) -> Unit,
) {
    MinimalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle("Needs attention")
                Text(
                    text = "Maintenance, fee due, and local actions waiting to sync.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            StatusBadge(label = lockers.size.toString(), tone = BadgeTone.Fee)
        }

        if (lockers.isEmpty()) {
            Text(
                text = "No locker needs attention right now.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                lockers.forEach { locker ->
                    AttentionLockerRow(
                        locker = locker,
                        onClick = { onLockerTap(locker) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AttentionLockerRow(
    locker: LockerUi,
    onClick: () -> Unit,
) {
    val status = locker.lockerVisualColors(MaterialTheme.colorScheme)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = SequoHubShapes.Small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.64f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Locker ${locker.id}",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = locker.nextAction,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StatusBadge(label = locker.statusLabel, tone = locker.badgeTone)
        }
    }
}

@Composable
fun LockerDetailOverlay(
    locker: LockerUi,
    onDismiss: () -> Unit,
    onReportProblem: (LockerUi) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.scrim.copy(alpha = 0.68f))
                .clickable(onClick = onDismiss),
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            shape = SequoHubShapes.Card,
            color = colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, colorScheme.outlineVariant),
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Locker ${locker.id}",
                            color = colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = locker.packageLabel,
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    StatusBadge(label = locker.state.label, tone = locker.state.badgeTone)
                }

                DetailRow(label = "Package age", value = locker.ageLabel)
                DetailRow(label = "Reference", value = locker.reference)
                DetailRow(label = "Sync", value = locker.syncState.label)
                DetailRow(label = "Next action", value = locker.nextAction)

                CustodyTimeline(locker = locker)

                if (locker.feeDueCfa > 0) {
                    FeeNotice(amount = locker.feeDueCfa)
                    PrimaryActionButton(label = "Collect Fee & Open Locker")
                } else {
                    PrimaryActionButton(label = locker.primaryAction)
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onReportProblem(locker) },
                    shape = SequoHubShapes.Small,
                ) {
                    Text("Report problem")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss,
                    shape = SequoHubShapes.Small,
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun CustodyTimeline(locker: LockerUi) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Custody timeline")
        locker.custodySteps.forEach { step ->
            CustodyStepRow(step = step)
        }
    }
}

@Composable
private fun CustodyStepRow(step: CustodyStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = step.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = step.detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        StatusBadge(label = step.tone.label, tone = step.tone)
    }
}

@Composable
private fun LockerCell(
    modifier: Modifier,
    locker: LockerUi,
    hubBlockedBySequo: Boolean,
    onClick: () -> Unit,
) {
    val status = locker.lockerVisualColors(MaterialTheme.colorScheme)
    val isMaintenance = locker.state == LockerState.Maintenance
    val isOccupied = locker.state == LockerState.Occupied

    val backgroundColor = if (isMaintenance) {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    } else if (hubBlockedBySequo) {
        MaterialTheme.colorScheme.surfaceVariant.compositeOver(MaterialTheme.colorScheme.surface)
    } else {
        status.background.compositeOver(MaterialTheme.colorScheme.surface)
    }

    val contentColor = if (isMaintenance) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    } else if (hubBlockedBySequo) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
    } else {
        status.text
    }

    val depthColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (hubBlockedBySequo) 0.14f else 0.24f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
    ) {
        if (!isMaintenance) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 3.dp, y = (-3).dp)
                    .clip(SequoHubShapes.Small),
                shape = SequoHubShapes.Small,
                color = depthColor,
                shadowElevation = 1.dp,
            ) {}
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(SequoHubShapes.Small),
            shape = SequoHubShapes.Small,
            color = if (isMaintenance) MaterialTheme.colorScheme.surface else backgroundColor,
            border = BorderStroke(
                1.dp,
                if (isMaintenance || hubBlockedBySequo) {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                } else {
                    status.border
                },
            ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isMaintenance) {
                    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val leftHalfPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, 0f)
                            lineTo(w * 0.44f, 0f)
                            lineTo(w * 0.38f, h * 0.25f)
                            lineTo(w * 0.52f, h * 0.5f)
                            lineTo(w * 0.35f, h * 0.75f)
                            lineTo(w * 0.42f, h)
                            lineTo(0f, h)
                            close()
                        }

                        val rightHalfPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w, 0f)
                            lineTo(w * 0.56f, 0f)
                            lineTo(w * 0.50f, h * 0.25f)
                            lineTo(w * 0.64f, h * 0.5f)
                            lineTo(w * 0.47f, h * 0.75f)
                            lineTo(w * 0.54f, h)
                            lineTo(w, h)
                            close()
                        }

                        drawPath(path = leftHalfPath, color = backgroundColor)
                        drawPath(path = rightHalfPath, color = backgroundColor)
                        drawPath(
                            path = leftHalfPath,
                            color = outlineColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                        )
                        drawPath(
                            path = rightHalfPath,
                            color = outlineColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = "Under Maintenance",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp),
                    )
                }

                // Bottom-right watermark package icon for occupied state
                if (isOccupied && !isMaintenance) {
                    Icon(
                        imageVector = Icons.Filled.Package2,
                        contentDescription = null,
                        tint = status.text.copy(alpha = 0.18f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 4.dp, bottom = 4.dp)
                            .size(28.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = locker.id,
                        color = contentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = locker.cellLabel,
                        color = contentColor.copy(alpha = if (isMaintenance) 0.6f else 0.82f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LockerLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LegendItem(
                modifier = Modifier.weight(1f),
                item = LockerLegendItem(label = "Package stored", icon = Icons.Filled.Package2, tone = BadgeTone.Active),
            )
            LegendItem(
                modifier = Modifier.weight(1f),
                item = LockerLegendItem(label = "Maintenance", icon = Icons.Filled.Build, tone = BadgeTone.Hold),
            )
        }
    }
}

@Composable
private fun LegendItem(
    modifier: Modifier,
    item: LockerLegendItem,
) {
    val status = item.tone.badgeColors(MaterialTheme.colorScheme)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            shape = SequoHubShapes.Small,
            color = status.background,
            border = BorderStroke(1.dp, status.border),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = status.text,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        Text(
            text = item.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class LockerLegendItem(
    val label: String,
    val icon: ImageVector,
    val tone: BadgeTone,
)

private data class CustodyStep(
    val title: String,
    val detail: String,
    val tone: BadgeTone,
)

private val LockerUi.custodySteps: List<CustodyStep>
    get() = when {
        syncState == LockerSyncState.Failed -> listOf(
            CustodyStep(
                title = "Sync failed",
                detail = "This locker has a local action that needs operator review.",
                tone = BadgeTone.Hold,
            ),
            CustodyStep(
                title = "Last known action",
                detail = nextAction,
                tone = badgeTone,
            ),
        )
        syncState == LockerSyncState.Pending -> listOf(
            CustodyStep(
                title = "Pending sync",
                detail = "Local action is queued and will sync when connectivity returns.",
                tone = BadgeTone.Fee,
            ),
            CustodyStep(
                title = "Last known action",
                detail = nextAction,
                tone = badgeTone,
            ),
        )
        state == LockerState.Free -> listOf(
            CustodyStep(
                title = "Locker ready",
                detail = "No parcel is assigned to this compartment.",
                tone = BadgeTone.Neutral,
            ),
            CustodyStep(
                title = "Next intake",
                detail = "Assign this locker when a package is scanned in.",
                tone = BadgeTone.Active,
            ),
        )
        state == LockerState.Maintenance -> listOf(
            CustodyStep(
                title = "Locker closed",
                detail = "This compartment is blocked from handover flows.",
                tone = BadgeTone.Hold,
            ),
            CustodyStep(
                title = "Manager review",
                detail = nextAction,
                tone = BadgeTone.Neutral,
            ),
        )
        feeDueCfa > 0 -> listOf(
            CustodyStep(
                title = "Package stored",
                detail = "$reference is waiting in locker $id.",
                tone = BadgeTone.Active,
            ),
            CustodyStep(
                title = "Pickup overdue",
                detail = ageLabel,
                tone = BadgeTone.Fee,
            ),
            CustodyStep(
                title = "Release control",
                detail = "Collect the fee, then validate the pickup code.",
                tone = BadgeTone.New,
            ),
        )
        else -> listOf(
            CustodyStep(
                title = "Package stored",
                detail = "$reference is waiting in locker $id.",
                tone = BadgeTone.Active,
            ),
            CustodyStep(
                title = "Customer notified",
                detail = ageLabel,
                tone = BadgeTone.Neutral,
            ),
            CustodyStep(
                title = "Pickup code required",
                detail = nextAction,
                tone = BadgeTone.New,
            ),
        )
    }

private val LockerState.badgeTone: BadgeTone
    get() = when (this) {
        LockerState.Free -> BadgeTone.Neutral
        LockerState.Occupied -> BadgeTone.Active
        LockerState.Maintenance -> BadgeTone.Hold
    }

private val LockerUi.badgeTone: BadgeTone
    get() = when {
        syncState == LockerSyncState.Failed -> BadgeTone.Hold
        syncState == LockerSyncState.Pending -> BadgeTone.Fee
        feeDueCfa > 0 -> BadgeTone.Fee
        else -> state.badgeTone
    }

private val LockerUi.statusLabel: String
    get() = when {
        syncState == LockerSyncState.Failed -> "Sync fail"
        syncState == LockerSyncState.Pending -> "Pending"
        feeDueCfa > 0 -> "Fee due"
        state == LockerState.Occupied -> "Stored"
        else -> state.shortLabel
    }

private val LockerUi.cellLabel: String
    get() = when (state) {
        LockerState.Free -> LockerState.Free.shortLabel
        LockerState.Occupied -> LockerState.Occupied.shortLabel
        LockerState.Maintenance -> LockerState.Maintenance.shortLabel
    }

private fun LockerUi.lockerVisualColors(colorScheme: androidx.compose.material3.ColorScheme): StatusColors =
    state.statusColors(colorScheme)

private val LockerSyncState.badgeTone: BadgeTone
    get() = when (this) {
        LockerSyncState.Synced -> BadgeTone.Active
        LockerSyncState.Pending -> BadgeTone.Fee
        LockerSyncState.Failed -> BadgeTone.Hold
    }
