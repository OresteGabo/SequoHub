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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.orestegabo.sequohub.core.designsystem.component.PrimaryActionButton
import dev.orestegabo.sequohub.core.designsystem.component.SectionTitle
import dev.orestegabo.sequohub.core.designsystem.component.SelectableChip
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.designsystem.component.StatusBadge
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader
import dev.orestegabo.sequohub.feature.hub.LockerUi

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    lockers: List<LockerUi>,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onQuickScanChange: (Boolean) -> Unit,
    onSoundFeedbackChange: (Boolean) -> Unit,
    onLargeLockerLabelsChange: (Boolean) -> Unit,
    onOpeningHoursChange: (List<HubOpeningDay>) -> Unit,
    onCloseTodayChange: (Boolean) -> Unit,
    onTodayClosingTimeChange: (String) -> Unit,
    onSaveOpeningHours: () -> Unit,
    onRegisterNotifications: () -> Unit,
    onRevokeNotifications: () -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onMarkLockerUnavailable: (LockerUi, String, String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(SettingsPage.Main) }

    when (page) {
        SettingsPage.Main -> SettingsMainPage(
            state = state,
            onThemeModeChange = onThemeModeChange,
            onLanguageChange = onLanguageChange,
            onQuickScanChange = onQuickScanChange,
            onSoundFeedbackChange = onSoundFeedbackChange,
            onLargeLockerLabelsChange = onLargeLockerLabelsChange,
            onRegisterNotifications = onRegisterNotifications,
            onRevokeNotifications = onRevokeNotifications,
            onOpenNotificationPreferences = onOpenNotificationPreferences,
            onOpenLockerClosure = { page = SettingsPage.LockerClosure },
            onOpenOpeningHours = { page = SettingsPage.OpeningHours },
            onLogoutClick = { showLogoutDialog = true },
            onDeleteAccountClick = { showDeleteDialog = true },
        )
        SettingsPage.LockerClosure -> LockerClosurePage(
            lockers = lockers,
            onBack = { page = SettingsPage.Main },
            onConfirm = onMarkLockerUnavailable,
        )
        SettingsPage.OpeningHours -> OpeningHoursPage(
            state = state,
            onBack = { page = SettingsPage.Main },
            onOpeningHoursChange = onOpeningHoursChange,
            onCloseTodayChange = onCloseTodayChange,
            onTodayClosingTimeChange = onTodayClosingTimeChange,
            onSave = onSaveOpeningHours,
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?") },
            text = { Text("This will end the staff session on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = { Text("This requests removal of the staff profile from SequoHub. Hub audit records may be retained for operations history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                ) {
                    Text("Request deletion", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingsMainPage(
    state: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onQuickScanChange: (Boolean) -> Unit,
    onSoundFeedbackChange: (Boolean) -> Unit,
    onLargeLockerLabelsChange: (Boolean) -> Unit,
    onRegisterNotifications: () -> Unit,
    onRevokeNotifications: () -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenLockerClosure: () -> Unit,
    onOpenOpeningHours: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
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

        SettingsSection(title = "Locker controls") {
            SettingsRow(
                icon = Icons.Filled.Lock,
                title = "Temporarily close a locker",
                subtitle = "Mark a broken or unusable locker as unavailable.",
                showChevron = true,
                onClick = onOpenLockerClosure,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
            SettingsRow(
                icon = Icons.Filled.CalendarMonth,
                title = "Opening hours",
                subtitle = openingHoursSummary(state),
                showChevron = true,
                onClick = onOpenOpeningHours,
            )
        }

        SettingsSection(title = "Notifications") {
            SettingsRow(
                icon = Icons.Filled.Notifications,
                title = "Push alerts",
                subtitle = "Register this phone for hub pickup, return, and collection alerts.",
                trailingText = "On",
                onClick = onRegisterNotifications,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
            SettingsRow(
                icon = Icons.Filled.Notifications,
                title = "Notification channels",
                subtitle = "Push, in-app, SMS, and quiet hours for hub events.",
                showChevron = true,
                onClick = onOpenNotificationPreferences,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
            SettingsRow(
                icon = Icons.Filled.Notifications,
                title = "Stop alerts on this phone",
                subtitle = "Revoke this device when it is no longer used at the counter.",
                tone = SettingsRowTone.Danger,
                onClick = onRevokeNotifications,
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
                onClick = onLogoutClick,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
            SettingsRow(
                icon = Icons.Filled.DeleteForever,
                title = "Delete account",
                subtitle = "Request account removal for this staff profile.",
                tone = SettingsRowTone.Danger,
                onClick = onDeleteAccountClick,
            )
        }
    }
}

@Composable
private fun LockerClosurePage(
    lockers: List<LockerUi>,
    onBack: () -> Unit,
    onConfirm: (LockerUi, String, String) -> Unit,
) {
    var selectedLockerId by remember { mutableStateOf(lockers.firstOrNull()?.id.orEmpty()) }
    var reason by remember { mutableStateOf("Broken door") }
    var expectedBackAt by remember { mutableStateOf("Tomorrow 09:00") }
    val selectedLocker = lockers.firstOrNull { it.id == selectedLockerId }

    AppScroll {
        BackHeader(
            eyebrow = "Locker controls",
            title = "Close locker",
            subtitle = "Choose the locker and recovery note",
            onBack = onBack,
        )

        MinimalCard {
            SectionTitle("Select locker")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lockers.chunked(5).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { locker ->
                            SelectableChip(
                                modifier = Modifier.weight(1f),
                                label = locker.id,
                                selected = selectedLockerId == locker.id,
                                onClick = { selectedLockerId = locker.id },
                            )
                        }
                    }
                }
            }
        }

        MinimalCard {
            SectionTitle("Closure details")
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = reason,
                onValueChange = { reason = it },
                singleLine = true,
                label = { Text("Reason") },
                shape = SequoHubShapes.Small,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = expectedBackAt,
                onValueChange = { expectedBackAt = it },
                singleLine = true,
                label = { Text("Expected available again") },
                shape = SequoHubShapes.Small,
            )
            PrimaryActionButton(
                label = "Close ${selectedLocker?.id ?: "locker"} temporarily",
                enabled = selectedLocker != null && reason.isNotBlank(),
                onClick = {
                    selectedLocker?.let { locker ->
                        onConfirm(locker, reason.trim(), expectedBackAt.trim())
                    }
                },
            )
        }
    }
}

@Composable
private fun OpeningHoursPage(
    state: SettingsUiState,
    onBack: () -> Unit,
    onOpeningHoursChange: (List<HubOpeningDay>) -> Unit,
    onCloseTodayChange: (Boolean) -> Unit,
    onTodayClosingTimeChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    AppScroll {
        BackHeader(
            eyebrow = "Hub timetable",
            title = "Opening hours",
            subtitle = "Keep pickup promises aligned with shop hours",
            onBack = onBack,
        )

        MinimalCard {
            SectionTitle("Today")
            SettingsInlineSwitch(
                title = "Closed for the day",
                subtitle = "Use for holidays, stock count, or emergency closure.",
                checked = state.closeToday,
                onCheckedChange = onCloseTodayChange,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.todayClosingTime,
                onValueChange = { onTodayClosingTimeChange(it.take(5)) },
                singleLine = true,
                label = { Text("Close earlier today") },
                shape = SequoHubShapes.Small,
            )
        }

        MinimalCard {
            SectionTitle("Usual week")
            state.openingHours.forEachIndexed { index, day ->
                OpeningDayRow(
                    day = day,
                    onChange = { updated ->
                        onOpeningHoursChange(
                            state.openingHours.mapIndexed { dayIndex, current ->
                                if (dayIndex == index) updated else current
                            },
                        )
                    },
                )
                if (index != state.openingHours.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
                }
            }
            PrimaryActionButton(
                label = "Save timetable",
                onClick = onSave,
            )
        }
    }
}

@Composable
private fun OpeningDayRow(
    day: HubOpeningDay,
    onChange: (HubOpeningDay) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = day.isOpen,
            onCheckedChange = { onChange(day.copy(isOpen = it)) },
        )
        Text(
            text = day.day,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.8f),
        )
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = day.opensAt,
            onValueChange = { onChange(day.copy(opensAt = it.take(5))) },
            singleLine = true,
            enabled = day.isOpen,
            label = { Text("Open") },
            shape = SequoHubShapes.Small,
        )
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = day.closesAt,
            onValueChange = { onChange(day.copy(closesAt = it.take(5))) },
            singleLine = true,
            enabled = day.isOpen,
            label = { Text("Close") },
            shape = SequoHubShapes.Small,
        )
    }
}

@Composable
private fun SettingsInlineSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BackHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    MinimalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = eyebrow.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
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

private enum class SettingsPage {
    Main,
    LockerClosure,
    OpeningHours,
}

private fun openingHoursSummary(state: SettingsUiState): String {
    if (state.closeToday) return "Closed today"
    val openDays = state.openingHours.filter { it.isOpen }
    val weekdayHours = openDays.firstOrNull { it.day == "Mon" }
    val weekendHours = openDays.firstOrNull { it.day == "Sat" }
    return buildString {
        if (weekdayHours != null) {
            append("Weekdays ")
            append(weekdayHours.opensAt)
            append("-")
            append(weekdayHours.closesAt)
        }
        if (weekendHours != null) {
            if (isNotEmpty()) append(", ")
            append("Sat ")
            append(weekendHours.opensAt)
            append("-")
            append(weekendHours.closesAt)
        }
    }.ifBlank { "Closed all week" }
}
