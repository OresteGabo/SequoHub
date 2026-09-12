package dev.orestegabo.sequohub

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import dev.orestegabo.sequohub.feature.notifications.NotificationMessageUi
import dev.orestegabo.sequohub.feature.scan.ScanReceiveScreen
import dev.orestegabo.sequohub.feature.settings.SettingsScreen
import dev.orestegabo.sequohub.feature.settings.SettingsUiState
import dev.orestegabo.sequohub.feature.settings.ThemeMode
import dev.orestegabo.sequohub.navigation.MainTab
import dev.orestegabo.sequohub.navigation.SequoBottomNavigation
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
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
            MainTab.Scan -> ScanReceiveScreen(
                onStartCameraScan = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Camera scan will resolve through POST /api/hub/scan/resolve when the API exists.")
                    }
                },
                onResolveManualCode = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for POST /api/hub/scan/resolve with the typed credential.")
                    }
                },
                onAssignLocker = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for POST /api/relay/parcels to reserve a free locker.")
                    }
                },
            )
            MainTab.Handover -> HandoverScreen(
                onValidatePickup = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for POST /api/hub/scan/resolve before parcel release.")
                    }
                },
                onCollectFeeAndOpen = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for POST /api/relay/parcels/{parcelId}/release.")
                    }
                },
                onValidateReturn = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for GET /api/returns/{returnId} and 72-hour validation.")
                    }
                },
                onReceiveReturn = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for POST /api/returns/{returnId}/relay-dropoff.")
                    }
                },
            )
            MainTab.Activity -> ActivityScreen(
                onNotificationsRefresh = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for GET /api/notifications/inbox?includeArchived=true&limit=20.")
                    }
                },
                onNotificationRead = { message ->
                    showApiSnackbar(
                        snackbarHostState = snackbarHostState,
                        message = message,
                        action = "PATCH /api/notifications/inbox/${message.id}/read",
                        coroutineScope = coroutineScope,
                    )
                },
                onNotificationArchive = { message ->
                    showApiSnackbar(
                        snackbarHostState = snackbarHostState,
                        message = message,
                        action = "POST /api/notifications/inbox/${message.id}/archive",
                        coroutineScope = coroutineScope,
                    )
                },
                onNotificationUnarchive = { message ->
                    showApiSnackbar(
                        snackbarHostState = snackbarHostState,
                        message = message,
                        action = "DELETE /api/notifications/inbox/${message.id}/archive",
                        coroutineScope = coroutineScope,
                    )
                },
            )
            MainTab.Settings -> SettingsScreen(
                state = settings,
                onThemeModeChange = { onSettingsChange(settings.copy(themeMode = it)) },
                onLanguageChange = { onSettingsChange(settings.copy(language = it)) },
                onQuickScanChange = { onSettingsChange(settings.copy(quickScanOnOpen = it)) },
                onSoundFeedbackChange = { onSettingsChange(settings.copy(soundFeedback = it)) },
                onLargeLockerLabelsChange = { onSettingsChange(settings.copy(largeLockerLabels = it)) },
                onRegisterNotifications = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for POST /api/notifications/devices/fcm with appFamily SEQUO_HUB.")
                    }
                },
                onRevokeNotifications = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for DELETE /api/notifications/devices/SEQUO_HUB/{deviceId}.")
                    }
                },
                onOpenNotificationPreferences = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for GET/PUT /api/notifications/preferences/SEQUO_HUB.")
                    }
                },
                onLogout = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Logout will be wired to the auth API.")
                    }
                },
                onDeleteAccount = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Delete account needs the backend contract in MOBILE_API_TODO.md.")
                    }
                },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 104.dp, start = 16.dp, end = 16.dp),
        )

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
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ready for POST /api/relay/parcels/{parcelId}/problem.")
                    }
                },
            )
        }
    }
}

private fun showApiSnackbar(
    snackbarHostState: SnackbarHostState,
    message: NotificationMessageUi,
    action: String,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    coroutineScope.launch {
        snackbarHostState.showSnackbar("${message.title}: ready for $action")
    }
}
