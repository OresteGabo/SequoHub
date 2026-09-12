package dev.orestegabo.sequohub.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.AppScroll
import dev.orestegabo.sequohub.core.designsystem.component.BadgeTone
import dev.orestegabo.sequohub.core.designsystem.component.MinimalCard
import dev.orestegabo.sequohub.core.designsystem.component.SelectableChip
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onQuickScanChange: (Boolean) -> Unit,
    onSoundFeedbackChange: (Boolean) -> Unit,
    onLargeLockerLabelsChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    AppScroll {
        TopHeader(
            eyebrow = "Settings",
            title = "Hub settings",
            subtitle = "Account, language, appearance, and counter flow",
            status = "Local",
        )

        OperatorCard(state = state)

        SettingsSection(title = "Appearance") {
            Text(
                text = "Theme",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    SelectableChip(
                        modifier = Modifier.weight(1f),
                        label = mode.label,
                        selected = state.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                    )
                }
            }
            SettingsRow(
                icon = Icons.Filled.TextFields,
                title = "Large locker labels",
                subtitle = "Bigger A01-E05 labels for busy counters.",
                trailing = {
                    Switch(
                        checked = state.largeLockerLabels,
                        onCheckedChange = onLargeLockerLabelsChange,
                    )
                },
            )
        }

        SettingsSection(title = "Language") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppLanguage.entries.forEach { language ->
                    SelectableChip(
                        modifier = Modifier.weight(1f),
                        label = language.code,
                        selected = state.language == language,
                        onClick = { onLanguageChange(language) },
                    )
                }
            }
            SettingsRow(
                icon = Icons.Filled.Language,
                title = "Display language",
                subtitle = state.language.label,
                trailingText = state.language.code,
            )
        }

        SettingsSection(title = "Counter workflow") {
            SettingsRow(
                icon = Icons.Filled.QrCodeScanner,
                title = "Quick scan on open",
                subtitle = "Start from the fastest scan-first intake flow.",
                trailing = {
                    Switch(
                        checked = state.quickScanOnOpen,
                        onCheckedChange = onQuickScanChange,
                    )
                },
            )
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Sound feedback",
                subtitle = "Play soft confirmations after validation.",
                trailing = {
                    Switch(
                        checked = state.soundFeedback,
                        onCheckedChange = onSoundFeedbackChange,
                    )
                },
            )
            SettingsRow(
                icon = Icons.Filled.Notifications,
                title = "Operational alerts",
                subtitle = "Overdue, support hold, and collection reminders.",
                trailingText = "On",
            )
        }

        SettingsSection(title = "Support and privacy") {
            SettingsRow(
                icon = Icons.Filled.Policy,
                title = "Data and privacy",
                subtitle = "Identity checks, audit records, and device tokens.",
                showChevron = true,
            )
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Help and counter guide",
                subtitle = "Pickup, return, and damaged parcel instructions.",
                showChevron = true,
            )
        }

        SettingsSection(title = "Account") {
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Logout",
                subtitle = "End this staff session on the device.",
                tone = SettingsRowTone.Primary,
                onClick = onLogout,
            )
            SettingsRow(
                icon = Icons.Filled.DeleteForever,
                title = "Delete account",
                subtitle = "Requires a backend account deletion workflow.",
                tone = SettingsRowTone.Danger,
                onClick = onDeleteAccount,
            )
        }
    }
}

@Composable
private fun OperatorCard(state: SettingsUiState) {
    MinimalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBubble(
                    icon = Icons.Filled.Storefront,
                    large = true,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = state.hubName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = state.staffRole,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            StatusBadge(label = "Hub", tone = BadgeTone.Active)
        }
        SettingsRow(
            icon = Icons.Filled.AccountCircle,
            title = state.staffName,
            subtitle = "Operator profile and permissions",
            showChevron = true,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    MinimalCard {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    showChevron: Boolean = false,
    tone: SettingsRowTone = SettingsRowTone.Neutral,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = when (tone) {
        SettingsRowTone.Neutral -> colorScheme.onSurface
        SettingsRowTone.Primary -> colorScheme.primary
        SettingsRowTone.Danger -> colorScheme.error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = SequoHubShapes.Small,
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBubble(icon = icon, contentColor = contentColor)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    color = contentColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            when {
                trailing != null -> trailing()
                trailingText != null -> Text(
                    text = trailingText,
                    color = colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                showChevron -> Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun IconBubble(
    icon: ImageVector,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    large: Boolean = false,
) {
    Surface(
        modifier = Modifier.size(if (large) 54.dp else 40.dp),
        shape = SequoHubShapes.Small,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.64f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(if (large) 28.dp else 21.dp),
            )
        }
    }
}

private enum class SettingsRowTone {
    Neutral,
    Primary,
    Danger,
}
