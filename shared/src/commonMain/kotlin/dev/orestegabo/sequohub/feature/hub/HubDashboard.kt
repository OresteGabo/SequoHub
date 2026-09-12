package dev.orestegabo.sequohub.feature.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import dev.orestegabo.sequohub.core.designsystem.component.PrimaryActionButton
import dev.orestegabo.sequohub.core.designsystem.component.SectionTitle
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.statusColors
import dev.orestegabo.sequohub.feature.handover.FeeNotice

@Composable
fun HubDashboard(
    lockers: List<LockerUi>,
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
    val attentionLockers = visibleLockers.filter { it.needsAttention }

    AppScroll {
        dev.orestegabo.sequohub.core.designsystem.component.TopHeader(
            eyebrow = "Point de Relai",
            title = "Hub dashboard",
            subtitle = "Lomé Relay 04",
            status = "Live sync",
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
                label = "Sequo",
                value = "5",
                note = "pending",
            )
        }

        LockerSearchCard(
            lockerSearch = lockerSearch,
            onLockerSearchChange = { lockerSearch = it.take(3).uppercase() },
        )

        if (selectedDashboardTab == 0) {
            LockerGridCard(
                lockers = visibleLockers,
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
            StatusBadge(label = "5x5", tone = BadgeTone.Neutral)
        }

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
                            onClick = { onLockerTap(locker) },
                        )
                    }
                    repeat(5 - row.size) {
                        Box(modifier = Modifier.weight(1f))
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
                    text = "Only lockers with fees, holds, reservations, or maintenance.",
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
            StatusBadge(label = locker.state.label, tone = locker.state.badgeTone)
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
                DetailRow(label = "Next action", value = locker.nextAction)

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
private fun LockerCell(
    modifier: Modifier,
    locker: LockerUi,
    onClick: () -> Unit,
) {
    val status = locker.state.statusColors(MaterialTheme.colorScheme)

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(SequoHubShapes.Small)
            .clickable(onClick = onClick),
        shape = SequoHubShapes.Small,
        color = status.background,
        border = BorderStroke(1.dp, status.border),
    ) {
        Column(
            modifier = Modifier.padding(7.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = locker.id,
                color = status.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = locker.state.shortLabel,
                color = status.text.copy(alpha = 0.82f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LockerLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LockerState.entries.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { state ->
                    LegendItem(
                        modifier = Modifier.weight(1f),
                        state = state,
                    )
                }
                repeat(3 - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    modifier: Modifier,
    state: LockerState,
) {
    val status = state.statusColors(MaterialTheme.colorScheme)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(status.background, SequoHubShapes.Small)
                .border(1.dp, status.border, SequoHubShapes.Small),
        )
        Text(
            text = state.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val LockerState.badgeTone: BadgeTone
    get() = when (this) {
        LockerState.Free -> BadgeTone.Neutral
        LockerState.Reserved -> BadgeTone.New
        LockerState.Occupied -> BadgeTone.Active
        LockerState.Overdue -> BadgeTone.Fee
        LockerState.Blocked -> BadgeTone.Hold
        LockerState.Maintenance -> BadgeTone.Neutral
    }
