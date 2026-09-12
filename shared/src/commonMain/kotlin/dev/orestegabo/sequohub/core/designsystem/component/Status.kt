package dev.orestegabo.sequohub.core.designsystem.component

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import dev.orestegabo.sequohub.feature.hub.LockerState

enum class BadgeTone(val label: String) {
    Active(label = "Active"),
    New(label = "New"),
    Fee(label = "Fee due"),
    Hold(label = "Hold"),
    Neutral(label = "Log"),
}

data class StatusColors(
    val background: Color,
    val text: Color,
    val border: Color,
)

fun LockerState.statusColors(colorScheme: ColorScheme): StatusColors =
    when (this) {
        LockerState.Free -> StatusColors(
            background = colorScheme.primaryContainer.copy(alpha = 0.64f),
            text = colorScheme.onPrimaryContainer,
            border = colorScheme.primary.copy(alpha = 0.28f),
        )
        LockerState.Occupied -> StatusColors(
            background = colorScheme.tertiaryContainer.copy(alpha = 0.72f),
            text = colorScheme.onTertiaryContainer,
            border = colorScheme.tertiary.copy(alpha = 0.28f),
        )
        LockerState.Maintenance -> StatusColors(
            background = colorScheme.errorContainer.copy(alpha = 0.34f),
            text = colorScheme.error,
            border = colorScheme.error.copy(alpha = 0.54f),
        )
    }

fun BadgeTone.badgeColors(colorScheme: ColorScheme): StatusColors =
    when (this) {
        BadgeTone.Active -> StatusColors(
            background = colorScheme.primaryContainer.copy(alpha = 0.64f),
            text = colorScheme.onPrimaryContainer,
            border = colorScheme.primary.copy(alpha = 0.28f),
        )
        BadgeTone.New -> StatusColors(
            background = colorScheme.secondaryContainer.copy(alpha = 0.72f),
            text = colorScheme.onSecondaryContainer,
            border = colorScheme.secondary.copy(alpha = 0.28f),
        )
        BadgeTone.Fee -> StatusColors(
            background = colorScheme.tertiaryContainer.copy(alpha = 0.72f),
            text = colorScheme.onTertiaryContainer,
            border = colorScheme.tertiary.copy(alpha = 0.32f),
        )
        BadgeTone.Hold -> StatusColors(
            background = colorScheme.errorContainer.copy(alpha = 0.72f),
            text = colorScheme.onErrorContainer,
            border = colorScheme.error.copy(alpha = 0.32f),
        )
        BadgeTone.Neutral -> StatusColors(
            background = colorScheme.surfaceContainerHigh,
            text = colorScheme.onSurfaceVariant,
            border = colorScheme.outlineVariant,
        )
    }
