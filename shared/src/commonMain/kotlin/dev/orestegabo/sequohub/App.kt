package dev.orestegabo.sequohub

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
        SequoHubApp(
            settings = settings,
            onSettingsChange = { settings = it },
        )
    }
}

@Composable
private fun SequoHubApp(
    settings: SettingsUiState,
    onSettingsChange: (SettingsUiState) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Hub) }
    var selectedLockerId by rememberSaveable { mutableStateOf<String?>(null) }
    val lockers = sampleLockers()
    val selectedLocker = lockers.firstOrNull { it.id == selectedLockerId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (selectedTab) {
            MainTab.Hub -> HubDashboard(
                lockers = lockers,
                onLockerTap = { selectedLockerId = it.id },
            )
            MainTab.Scan -> ScanReceiveScreen()
            MainTab.Handover -> HandoverScreen()
            MainTab.Activity -> ActivityScreen()
            MainTab.Settings -> SettingsScreen(
                state = settings,
                onThemeModeChange = { onSettingsChange(settings.copy(themeMode = it)) },
                onLanguageChange = { onSettingsChange(settings.copy(language = it)) },
                onQuickScanChange = { onSettingsChange(settings.copy(quickScanOnOpen = it)) },
                onSoundFeedbackChange = { onSettingsChange(settings.copy(soundFeedback = it)) },
                onLargeLockerLabelsChange = { onSettingsChange(settings.copy(largeLockerLabels = it)) },
                onLogout = {},
                onDeleteAccount = {},
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
            )
        }
    }
}
