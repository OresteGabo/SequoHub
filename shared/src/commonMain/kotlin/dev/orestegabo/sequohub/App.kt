package dev.orestegabo.sequohub

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import dev.orestegabo.sequohub.feature.auth.AuthScreen
import dev.orestegabo.sequohub.feature.hub.HubDashboard
import dev.orestegabo.sequohub.feature.hub.LockerDetailOverlay
import dev.orestegabo.sequohub.feature.hub.sampleLockers
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
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Hub) }
    var selectedLockerId by rememberSaveable { mutableStateOf<String?>(null) }
    val strings = LocalSequoStrings.current
    val lockers = sampleLockers(strings)
    val hubBlockedBySequo = false
    val selectedLocker = lockers.firstOrNull { it.id == selectedLockerId }

    if (!isAuthenticated) {
        AuthScreen(
            onLogin = { isAuthenticated = true },
            onGoogleLogin = { isAuthenticated = true },
            onAppleLogin = { isAuthenticated = true },
            onPrivacyTermsClick = {},
        )
        return
    }

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
            MainTab.Activity -> ActivityScreen(
                onNotificationsRefresh = {},
                onNotificationRead = {},
                onNotificationArchive = {},
                onNotificationUnarchive = {},
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
                onSaveOpeningHours = {},
                onRegisterNotifications = {},
                onRevokeNotifications = {},
                onOpenNotificationPreferences = {},
                onMarkLockerUnavailable = { _, _, _ -> },
                onLogout = {
                    selectedTab = MainTab.Hub
                    selectedLockerId = null
                    isAuthenticated = false
                },
                onDeleteAccount = {},
            )
        }

        FloatingActionButton(
            onClick = {},
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 104.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = strings.scanPackageQr,
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
                onReportProblem = {},
            )
        }
    }
}
