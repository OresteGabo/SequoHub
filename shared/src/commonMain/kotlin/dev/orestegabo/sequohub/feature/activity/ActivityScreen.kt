package dev.orestegabo.sequohub.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.AppScroll
import dev.orestegabo.sequohub.core.designsystem.component.BadgeTone
import dev.orestegabo.sequohub.core.designsystem.component.MinimalCard
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader

@Composable
fun ActivityScreen() {
    AppScroll {
        TopHeader(
            eyebrow = "Audit",
            title = "Activity",
            subtitle = "Latest handovers and locker events",
            status = "Today",
        )

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
