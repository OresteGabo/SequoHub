package dev.orestegabo.sequohub.feature.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.BadgeTone
import dev.orestegabo.sequohub.core.designsystem.component.Package2
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.theme.sequoSemanticColors
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings
import dev.orestegabo.sequohub.core.localization.SequoStrings

data class NotificationMessageUi(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean,
    val isArchived: Boolean,
)

enum class NotificationType(
    val label: String,
    val tone: BadgeTone,
    val icon: ImageVector,
) {
    Pickup(label = "Pickup", tone = BadgeTone.New, icon = Icons.Filled.Package2),
    Return(label = "Return", tone = BadgeTone.Active, icon = Icons.Filled.LocalShipping),
    Hold(label = "Hold", tone = BadgeTone.Hold, icon = Icons.Filled.ReportProblem),
    System(label = "System", tone = BadgeTone.Neutral, icon = Icons.Filled.Notifications),
}

@Composable
fun NotificationInboxPanel(
    messages: List<NotificationMessageUi>,
    onMarkRead: (NotificationMessageUi) -> Unit,
    onArchive: (NotificationMessageUi) -> Unit,
    onUnarchive: (NotificationMessageUi) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        messages.forEachIndexed { index, message ->
            NotificationMessageRow(
                message = message,
                onMarkRead = { onMarkRead(message) },
                onArchive = { onArchive(message) },
                onUnarchive = { onUnarchive(message) },
            )
            if (index != messages.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
            }
        }
    }
}

@Composable
private fun NotificationMessageRow(
    message: NotificationMessageUi,
    onMarkRead: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val semanticColors = MaterialTheme.sequoSemanticColors
    val strings = LocalSequoStrings.current
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = if (message.isRead) {
            colorScheme.surface
        } else {
            semanticColors.infoContainer.copy(alpha = 0.42f)
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = message.type.icon,
                contentDescription = null,
                tint = if (message.isRead) colorScheme.onSurfaceVariant else semanticColors.info,
                modifier = Modifier.padding(top = 2.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = message.title,
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (message.isRead) FontWeight.Medium else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = message.time,
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Text(
                    text = message.body,
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(label = if (message.isRead) message.type.localizedLabel else strings.newLabel, tone = message.type.tone)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!message.isRead) {
                            TextButton(onClick = onMarkRead) {
                                Text(strings.markRead)
                            }
                        }
                        TextButton(onClick = if (message.isArchived) onUnarchive else onArchive) {
                            Text(if (message.isArchived) strings.restore else strings.archive)
                        }
                    }
                }
            }
        }
    }
}

fun sampleNotifications(strings: SequoStrings): List<NotificationMessageUi> =
    listOf(
        NotificationMessageUi(
            id = "msg-2048",
            title = "${strings.pickupReady} A04",
            body = strings.collectBeforeRelease,
            time = "09:44",
            type = NotificationType.Pickup,
            isRead = false,
            isArchived = false,
        ),
        NotificationMessageUi(
            id = "msg-2047",
            title = strings.packageStored,
            body = "COL-1042",
            time = "08:20",
            type = NotificationType.System,
            isRead = false,
            isArchived = false,
        ),
        NotificationMessageUi(
            id = "msg-2046",
            title = strings.returnDropOff,
            body = "B03 - ${strings.sealedOk}",
            time = strings.yesterday,
            type = NotificationType.Return,
            isRead = true,
            isArchived = false,
        ),
        NotificationMessageUi(
            id = "msg-2045",
            title = "${strings.needsAttention} D02",
            body = strings.damaged,
            time = strings.yesterday,
            type = NotificationType.Hold,
            isRead = true,
            isArchived = true,
        ),
    )

private val NotificationType.localizedLabel: String
    @Composable get() {
        val strings = LocalSequoStrings.current
        return when (this) {
            NotificationType.Pickup -> strings.pickup
            NotificationType.Return -> strings.returnPackage
            NotificationType.Hold -> strings.blocked
            NotificationType.System -> strings.sync
        }
    }
