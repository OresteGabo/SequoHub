package dev.orestegabo.sequohub.core.designsystem.component

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import dev.orestegabo.sequohub.core.designsystem.theme.SequoSemanticColors
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

fun LockerState.statusColors(
    colorScheme: ColorScheme,
    semanticColors: SequoSemanticColors,
): StatusColors =
    when (this) {
        LockerState.Free -> StatusColors(
            background = semanticColors.successContainer.copy(alpha = 0.72f),
            text = semanticColors.onSuccessContainer,
            border = semanticColors.success.copy(alpha = 0.32f),
        )
        LockerState.Occupied -> StatusColors(
            background = semanticColors.warningContainer.copy(alpha = 0.76f),
            text = semanticColors.onWarningContainer,
            border = semanticColors.warning.copy(alpha = 0.30f),
        )
        LockerState.Maintenance -> StatusColors(
            background = semanticColors.dangerContainer.copy(alpha = 0.42f),
            text = semanticColors.danger,
            border = semanticColors.danger.copy(alpha = 0.54f),
        )
    }

fun BadgeTone.badgeColors(
    colorScheme: ColorScheme,
    semanticColors: SequoSemanticColors,
): StatusColors =
    when (this) {
        BadgeTone.Active -> StatusColors(
            background = semanticColors.successContainer.copy(alpha = 0.72f),
            text = semanticColors.onSuccessContainer,
            border = semanticColors.success.copy(alpha = 0.32f),
        )
        BadgeTone.New -> StatusColors(
            background = semanticColors.infoContainer.copy(alpha = 0.72f),
            text = semanticColors.onInfoContainer,
            border = semanticColors.info.copy(alpha = 0.32f),
        )
        BadgeTone.Fee -> StatusColors(
            background = semanticColors.warningContainer.copy(alpha = 0.76f),
            text = semanticColors.onWarningContainer,
            border = semanticColors.warning.copy(alpha = 0.36f),
        )
        BadgeTone.Hold -> StatusColors(
            background = semanticColors.dangerContainer.copy(alpha = 0.72f),
            text = semanticColors.onDangerContainer,
            border = semanticColors.danger.copy(alpha = 0.36f),
        )
        BadgeTone.Neutral -> StatusColors(
            background = colorScheme.surfaceContainerHigh,
            text = colorScheme.onSurfaceVariant,
            border = colorScheme.outlineVariant,
        )
    }
