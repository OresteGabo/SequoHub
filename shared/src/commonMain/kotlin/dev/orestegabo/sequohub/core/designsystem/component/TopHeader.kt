package dev.orestegabo.sequohub.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    status: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = eyebrow.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                lineHeight = 35.sp,
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        StatusBadge(label = status, tone = BadgeTone.Active)
    }
}
