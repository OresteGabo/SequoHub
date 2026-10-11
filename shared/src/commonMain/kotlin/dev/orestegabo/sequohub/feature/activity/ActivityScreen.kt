package dev.orestegabo.sequohub.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.AppScroll
import dev.orestegabo.sequohub.core.designsystem.component.BadgeTone
import dev.orestegabo.sequohub.core.designsystem.component.MinimalCard
import dev.orestegabo.sequohub.core.designsystem.component.SectionTitle
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader
import dev.orestegabo.sequohub.core.hub.HubAuditItem
import dev.orestegabo.sequohub.core.hub.HubAuditTone
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings
import dev.orestegabo.sequohub.core.localization.SequoStrings
import dev.orestegabo.sequohub.feature.notifications.NotificationInboxPanel
import dev.orestegabo.sequohub.feature.notifications.NotificationMessageUi
import dev.orestegabo.sequohub.feature.notifications.sampleNotifications

@Composable
fun ActivityScreen(
    auditItems: List<HubAuditItem> = emptyList(),
    onNotificationsRefresh: () -> Unit,
    onNotificationRead: (NotificationMessageUi) -> Unit,
    onNotificationArchive: (NotificationMessageUi) -> Unit,
    onNotificationUnarchive: (NotificationMessageUi) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val strings = LocalSequoStrings.current
    val tabs = listOf(strings.audit, strings.inbox)
    var notifications by remember(strings) { mutableStateOf(sampleNotifications(strings)) }
    val unreadCount = notifications.count { !it.isRead && !it.isArchived }

    AppScroll {
        TopHeader(
            eyebrow = strings.audit,
            title = strings.activity,
            subtitle = strings.activitySubtitle,
            status = "$unreadCount ${strings.newLabel}",
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> AuditTimeline(strings, auditItems)
            else -> MinimalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SectionTitle(strings.notificationInbox)
                        Text(
                            text = strings.inboxHint,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(label = "$unreadCount ${strings.unread}", tone = BadgeTone.New)
                        IconButton(onClick = onNotificationsRefresh) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = strings.refreshInbox,
                            )
                        }
                    }
                }
                NotificationInboxPanel(
                    messages = notifications,
                    onMarkRead = { message ->
                        onNotificationRead(message)
                        notifications = notifications.map {
                            if (it.id == message.id) it.copy(isRead = true) else it
                        }
                    },
                    onArchive = { message ->
                        onNotificationArchive(message)
                        notifications = notifications.map {
                            if (it.id == message.id) it.copy(isArchived = true) else it
                        }
                    },
                    onUnarchive = { message ->
                        onNotificationUnarchive(message)
                        notifications = notifications.map {
                            if (it.id == message.id) it.copy(isArchived = false) else it
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AuditTimeline(strings: SequoStrings, auditItems: List<HubAuditItem>) {
    val visibleItems = auditItems.ifEmpty {
        listOf(
            HubAuditItem("09:42", strings.pickupReady, "A04 - ${strings.collectBeforeRelease}", HubAuditTone.Active),
            HubAuditItem("09:18", strings.receiveReturn, "B03 - ${strings.sealedOk}", HubAuditTone.New),
            HubAuditItem("08:55", strings.needsAttention, "D02 - ${strings.damaged}", HubAuditTone.Hold),
            HubAuditItem("08:20", strings.packageStored, "COL-1042", HubAuditTone.Neutral),
        )
    }
    MinimalCard {
        visibleItems.forEach { item ->
            ActivityItem(
                time = item.timeLabel,
                title = item.title,
                subtitle = item.subtitle,
                tone = item.tone.toBadgeTone(),
            )
        }
    }
}

private fun HubAuditTone.toBadgeTone(): BadgeTone = when (this) {
    HubAuditTone.Active -> BadgeTone.Active
    HubAuditTone.New -> BadgeTone.New
    HubAuditTone.Hold -> BadgeTone.Hold
    HubAuditTone.Neutral -> BadgeTone.Neutral
}

@Composable
private fun ActivityItem(
    time: String,
    title: String,
    subtitle: String,
    tone: BadgeTone,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = time,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.74f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(44.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        StatusBadge(label = tone.label, tone = tone)
    }
}
