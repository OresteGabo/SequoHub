package dev.orestegabo.sequohub

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.theme.SequoHubTheme
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings
import dev.orestegabo.sequohub.core.localization.stringsFor
import dev.orestegabo.sequohub.feature.activity.ActivityScreen
import dev.orestegabo.sequohub.feature.handover.HandoverScreen
import dev.orestegabo.sequohub.feature.hub.HubDashboard
import dev.orestegabo.sequohub.feature.hub.LockerDetailOverlay
import dev.orestegabo.sequohub.feature.hub.sampleLockers
import dev.orestegabo.sequohub.feature.scan.ScanReceiveScreen
import dev.orestegabo.sequohub.feature.settings.SettingsScreen
import dev.orestegabo.sequohub.feature.settings.SettingsUiState
import dev.orestegabo.sequohub.feature.settings.ThemeMode
import dev.orestegabo.sequohub.navigation.MainTab
import dev.orestegabo.sequohub.navigation.SequoBottomNavigation

@Composable
@Preview
fun App() {
    var settings by remember { mutableStateOf(SettingsUiState()) }
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (settings.themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    SequoHubTheme(darkTheme = useDarkTheme) {
        CompositionLocalProvider(LocalSequoStrings provides stringsFor(settings.language)) {
            SequoHubApp(
                settings = settings,
                onSettingsChange = { settings = it },
            )
        }
    }
}

@Composable
private fun SequoHubApp(
    settings: SettingsUiState,
    onSettingsChange: (SettingsUiState) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Hub) }
    var selectedLockerId by rememberSaveable { mutableStateOf<String?>(null) }
    val strings = LocalSequoStrings.current
    val lockers = sampleLockers(strings)
    val hubBlockedBySequo = false
    val selectedLocker = lockers.firstOrNull { it.id == selectedLockerId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (selectedTab) {
            MainTab.Hub -> HubDashboard(
                lockers = lockers,
                hubBlockedBySequo = hubBlockedBySequo,
                onLockerTap = { selectedLockerId = it.id },
            )
            MainTab.Scan -> ScanReceiveScreen(
                onStartCameraScan = {
                    missingFeature("Camera scanner")
                },
                onResolveManualCode = {
                    missingFeature("Manual code resolution")
                },
                onAssignLocker = {
                    missingFeature("Locker assignment")
                },
            )
            MainTab.Handover -> HandoverScreen(
                onValidatePickup = {
                    missingFeature("Pickup validation")
                },
                onCollectFeeAndOpen = {
                    missingFeature("Fee collection and locker opening")
                },
                onValidateReturn = {
                    missingFeature("Return validation")
                },
                onReceiveReturn = {
                    missingFeature("Return receipt")
                },
            )
            MainTab.Activity -> ActivityScreen(
                onNotificationsRefresh = {
                    missingFeature("Notification inbox refresh")
                },
                onNotificationRead = {
                    missingFeature("Mark notification as read")
                },
                onNotificationArchive = {
                    missingFeature("Archive notification")
                },
                onNotificationUnarchive = {
                    missingFeature("Restore notification")
                },
            )
            MainTab.Settings -> SettingsScreen(
                state = settings,
                lockers = lockers,
                onThemeModeChange = { onSettingsChange(settings.copy(themeMode = it)) },
                onLanguageChange = { onSettingsChange(settings.copy(language = it)) },
                onQuickScanChange = { onSettingsChange(settings.copy(quickScanOnOpen = it)) },
                onSoundFeedbackChange = { onSettingsChange(settings.copy(soundFeedback = it)) },
                onLargeLockerLabelsChange = { onSettingsChange(settings.copy(largeLockerLabels = it)) },
                onOpeningHoursChange = { onSettingsChange(settings.copy(openingHours = it)) },
                onCloseTodayChange = { onSettingsChange(settings.copy(closeToday = it)) },
                onTodayClosingTimeChange = { onSettingsChange(settings.copy(todayClosingTime = it)) },
                onSaveOpeningHours = {
                    missingFeature("Hub timetable save")
                },
                onRegisterNotifications = {
                    missingFeature("Push notification registration")
                },
                onRevokeNotifications = {
                    missingFeature("Push notification revocation")
                },
                onOpenNotificationPreferences = {
                    missingFeature("Notification preferences")
                },
                onMarkLockerUnavailable = { _, _, _ ->
                    missingFeature("Temporary locker closure")
                },
                onLogout = {
                    missingFeature("Logout")
                },
                onDeleteAccount = {
                    missingFeature("Account deletion")
                },
            )
        }

        SequoBottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )

        selectedLocker?.let { locker ->
            LockerDetailOverlay(
                locker = locker,
                onDismiss = { selectedLockerId = null },
                onReportProblem = {
                    missingFeature("Relay problem reporting")
                },
            )
        }
    }
}

private fun missingFeature(name: String): Nothing =
    error("Missing implementation: $name")
