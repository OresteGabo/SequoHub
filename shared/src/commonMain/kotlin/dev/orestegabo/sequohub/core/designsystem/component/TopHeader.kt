package dev.orestegabo.sequohub.core.designsystem.component

import androidx.compose.runtime.Composable
import dev.orestegabo.sequo.design.SequoPageHeader

@Composable
fun TopHeader(eyebrow: String, title: String, subtitle: String, status: String) {
    SequoPageHeader(
        title = title,
        subtitle = listOf(eyebrow, subtitle).filter(String::isNotBlank).joinToString(" · "),
        actions = { StatusBadge(label = status, tone = BadgeTone.Active) },
    )
}
