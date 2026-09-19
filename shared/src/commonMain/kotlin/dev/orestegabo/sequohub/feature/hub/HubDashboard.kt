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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import dev.orestegabo.sequohub.core.designsystem.theme.SequoSemanticColors
import dev.orestegabo.sequohub.core.designsystem.theme.sequoSemanticColors
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings
import dev.orestegabo.sequohub.core.localization.SequoStrings
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
    val strings = LocalSequoStrings.current
    val dashboardTabs = listOf(strings.lockerGrid, strings.needsAttention)
    val visibleLockers = if (lockerSearch.isBlank()) {
        lockers
    } else {
        lockers.filter { it.id.contains(lockerSearch.trim(), ignoreCase = true) }
    }
    val pendingSyncCount = lockers.count { it.syncState != LockerSyncState.Synced }
    val attentionLockers = visibleLockers.filter { it.needsAttention }

    AppScroll {
        TopHeader(
            eyebrow = strings.pointDeRelai,
            title = strings.hubDashboard,
            subtitle = "Lomé Relay 04",
            status = if (hubBlockedBySequo) {
                strings.blocked
            } else if (pendingSyncCount == 0) {
                strings.synced
            } else {
                "$pendingSyncCount ${strings.pending}"
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
                label = strings.occupied,
                value = "17",
                note = "25 ${strings.locker.lowercase()}s",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = strings.feesDue,
                value = "3",
                note = strings.today,
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = strings.sync,
                value = pendingSyncCount.toString(),
                note = if (pendingSyncCount == 0) strings.clear else strings.pending,
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
            placeholder = { Text(LocalSequoStrings.current.lockerSearchPlaceholder) },
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
        val strings = LocalSequoStrings.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle(strings.lockerGrid)
                Text(
                    text = strings.lockerGridHint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            StatusBadge(
                label = if (hubBlockedBySequo) strings.blocked else "5x5",
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
                            text = strings.blockedBySequo,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = strings.blockedHubNotice,
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
        val strings = LocalSequoStrings.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle(strings.needsAttention)
                Text(
                    text = strings.attentionHint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            StatusBadge(label = lockers.size.toString(), tone = BadgeTone.Fee)
        }

        if (lockers.isEmpty()) {
            Text(
                text = strings.noLockerNeedsAttention,
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
    val status = locker.lockerVisualColors(
        colorScheme = MaterialTheme.colorScheme,
        semanticColors = MaterialTheme.sequoSemanticColors,
    )
    val strings = LocalSequoStrings.current

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
                    text = "${strings.locker} ${locker.id}",
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
            StatusBadge(label = locker.statusLabel(strings), tone = locker.badgeTone)
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
    val strings = LocalSequoStrings.current
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
                            text = "${strings.locker} ${locker.id}",
                            color = colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = locker.packageLabel,
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    StatusBadge(label = locker.cellLabel(strings), tone = locker.state.badgeTone)
                }

                DetailRow(label = strings.packageAge, value = locker.ageLabel)
                DetailRow(label = strings.reference, value = locker.reference)
                DetailRow(label = strings.sync, value = locker.syncState.label)
                DetailRow(label = strings.nextAction, value = locker.nextAction)

                CustodyTimeline(locker = locker, strings = strings)

                if (locker.feeDueCfa > 0) {
                    FeeNotice(amount = locker.feeDueCfa)
                    PrimaryActionButton(label = strings.collectFeeOpenLocker)
                } else {
                    PrimaryActionButton(label = locker.primaryAction)
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onReportProblem(locker) },
                    shape = SequoHubShapes.Small,
                ) {
                    Text(strings.reportProblem)
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss,
                    shape = SequoHubShapes.Small,
                ) {
                    Text(strings.close)
                }
            }
        }
    }
}

@Composable
private fun CustodyTimeline(locker: LockerUi, strings: SequoStrings) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(strings.custodyTimeline)
        locker.custodySteps(strings).forEach { step ->
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
    val strings = LocalSequoStrings.current
    val status = locker.lockerVisualColors(
        colorScheme = MaterialTheme.colorScheme,
        semanticColors = MaterialTheme.sequoSemanticColors,
    )
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
                if (!isMaintenance) {
                    val highlightAlpha = if (hubBlockedBySequo) 0.14f else 0.26f
                    val shadeAlpha = if (hubBlockedBySequo) 0.10f else 0.16f
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = highlightAlpha),
                                    Color.Transparent,
                                ),
                                startY = 0f,
                                endY = size.height * 0.58f,
                            ),
                        )
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = highlightAlpha * 0.68f),
                                    Color.Transparent,
                                ),
                                startX = 0f,
                                endX = size.width * 0.56f,
                            ),
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = shadeAlpha),
                                ),
                                startY = size.height * 0.46f,
                                endY = size.height,
                            ),
                        )
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = shadeAlpha * 0.78f),
                                ),
                                startX = size.width * 0.48f,
                                endX = size.width,
                            ),
                        )
                    }
                }

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
                        contentDescription = strings.underMaintenance,
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
                        text = locker.cellLabel(strings),
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
        val strings = LocalSequoStrings.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LegendItem(
                modifier = Modifier.weight(1f),
                item = LockerLegendItem(label = strings.packageStored, icon = Icons.Filled.Package2, tone = BadgeTone.Active),
            )
            LegendItem(
                modifier = Modifier.weight(1f),
                item = LockerLegendItem(label = strings.maintenance, icon = Icons.Filled.Build, tone = BadgeTone.Hold),
            )
        }
    }
}

@Composable
private fun LegendItem(
    modifier: Modifier,
    item: LockerLegendItem,
) {
    val status = item.tone.badgeColors(
        colorScheme = MaterialTheme.colorScheme,
        semanticColors = MaterialTheme.sequoSemanticColors,
    )

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

private fun LockerUi.custodySteps(strings: SequoStrings): List<CustodyStep> =
    when {
        syncState == LockerSyncState.Failed -> listOf(
            CustodyStep(
                title = strings.sync,
                detail = strings.attentionHint,
                tone = BadgeTone.Hold,
            ),
            CustodyStep(
                title = strings.nextAction,
                detail = nextAction,
                tone = badgeTone,
            ),
        )
        syncState == LockerSyncState.Pending -> listOf(
            CustodyStep(
                title = strings.pending,
                detail = strings.attentionHint,
                tone = BadgeTone.Fee,
            ),
            CustodyStep(
                title = strings.nextAction,
                detail = nextAction,
                tone = badgeTone,
            ),
        )
        state == LockerState.Free -> listOf(
            CustodyStep(
                title = strings.clear,
                detail = strings.temporarilyCloseLockerHint,
                tone = BadgeTone.Neutral,
            ),
            CustodyStep(
                title = strings.intake,
                detail = strings.lockerGridHint,
                tone = BadgeTone.Active,
            ),
        )
        state == LockerState.Maintenance -> listOf(
            CustodyStep(
                title = strings.closeLocker,
                detail = strings.temporarilyCloseLockerHint,
                tone = BadgeTone.Hold,
            ),
            CustodyStep(
                title = strings.nextAction,
                detail = nextAction,
                tone = BadgeTone.Neutral,
            ),
        )
        feeDueCfa > 0 -> listOf(
            CustodyStep(
                title = strings.packageStored,
                detail = "$reference - ${strings.locker} $id",
                tone = BadgeTone.Active,
            ),
            CustodyStep(
                title = strings.feeDue,
                detail = ageLabel,
                tone = BadgeTone.Fee,
            ),
            CustodyStep(
                title = strings.validate,
                detail = strings.collectBeforeRelease,
                tone = BadgeTone.New,
            ),
        )
        else -> listOf(
            CustodyStep(
                title = strings.packageStored,
                detail = "$reference - ${strings.locker} $id",
                tone = BadgeTone.Active,
            ),
            CustodyStep(
                title = strings.notificationInbox,
                detail = ageLabel,
                tone = BadgeTone.Neutral,
            ),
            CustodyStep(
                title = strings.pickupCode,
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

private fun LockerUi.statusLabel(strings: SequoStrings): String =
    when {
        syncState == LockerSyncState.Failed -> strings.sync
        syncState == LockerSyncState.Pending -> strings.pending
        feeDueCfa > 0 -> strings.feeDue
        state == LockerState.Occupied -> strings.packageStored
        else -> cellLabel(strings)
    }

private fun LockerUi.cellLabel(strings: SequoStrings): String =
    when (state) {
        LockerState.Free -> strings.emptyLocker
        LockerState.Occupied -> strings.occupied
        LockerState.Maintenance -> strings.close
    }

private fun LockerUi.lockerVisualColors(
    colorScheme: androidx.compose.material3.ColorScheme,
    semanticColors: SequoSemanticColors,
): StatusColors =
    state.statusColors(colorScheme, semanticColors)

private val LockerSyncState.badgeTone: BadgeTone
    get() = when (this) {
        LockerSyncState.Synced -> BadgeTone.Active
        LockerSyncState.Pending -> BadgeTone.Fee
        LockerSyncState.Failed -> BadgeTone.Hold
    }
