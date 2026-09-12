package dev.orestegabo.sequohub.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import dev.orestegabo.sequohub.feature.notifications.NotificationInboxPanel
import dev.orestegabo.sequohub.feature.notifications.NotificationMessageUi
import dev.orestegabo.sequohub.feature.notifications.sampleNotifications

@Composable
fun ActivityScreen(
    onNotificationsRefresh: () -> Unit,
    onNotificationRead: (NotificationMessageUi) -> Unit,
    onNotificationArchive: (NotificationMessageUi) -> Unit,
    onNotificationUnarchive: (NotificationMessageUi) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Audit", "Inbox")
    var notifications by remember { mutableStateOf(sampleNotifications()) }
    val unreadCount = notifications.count { !it.isRead && !it.isArchived }

    AppScroll {
        TopHeader(
            eyebrow = "Audit",
            title = "Activity",
            subtitle = "Hub events and notification inbox",
            status = "$unreadCount new",
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
            0 -> AuditTimeline()
            else -> MinimalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SectionTitle("Notification inbox")
                        Text(
                            text = "Push and in-app alerts for this SequoHub device.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(label = "$unreadCount unread", tone = BadgeTone.New)
                        TextButton(onClick = onNotificationsRefresh) {
                            Text("Refresh")
                        }
                    }
                }
                NotificationInboxPanel(
                    messages = notifications,
                    onMarkRead = { message ->
                        notifications = notifications.map {
                            if (it.id == message.id) it.copy(isRead = true) else it
                        }
                        onNotificationRead(message)
                    },
                    onArchive = { message ->
                        notifications = notifications.map {
                            if (it.id == message.id) it.copy(isArchived = true) else it
                        }
                        onNotificationArchive(message)
                    },
                    onUnarchive = { message ->
                        notifications = notifications.map {
                            if (it.id == message.id) it.copy(isArchived = false) else it
                        }
                        onNotificationUnarchive(message)
                    },
                )
            }
        }
    }
}

@Composable
private fun AuditTimeline() {
    MinimalCard {
        ActivityItem(
            time = "09:42",
            title = "Pickup released",
            subtitle = "A04 opened after fee collection",
            tone = BadgeTone.Active,
        )
        ActivityItem(
            time = "09:18",
            title = "Return received",
            subtitle = "B03 assigned, condition sealed ok",
            tone = BadgeTone.New,
        )
        ActivityItem(
            time = "08:55",
            title = "Support hold",
            subtitle = "D02 blocked after damaged packaging report",
            tone = BadgeTone.Hold,
        )
        ActivityItem(
            time = "08:20",
            title = "Sequo collection",
            subtitle = "3 parcels released to manifest COL-1042",
            tone = BadgeTone.Neutral,
        )
    }
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
