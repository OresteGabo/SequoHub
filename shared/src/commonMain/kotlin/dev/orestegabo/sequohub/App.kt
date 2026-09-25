package dev.orestegabo.sequohub

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.util.fastFirstOrNull
import dev.orestegabo.sequohub.core.auth.AuthApiClient
import dev.orestegabo.sequohub.core.auth.AuthRepository
import dev.orestegabo.sequohub.core.auth.GoogleSignInResult
import dev.orestegabo.sequohub.core.auth.rememberAuthSessionStore
import dev.orestegabo.sequohub.core.network.ApiHealthClient
import dev.orestegabo.sequohub.core.network.ApiHealthUiState
import dev.orestegabo.sequohub.core.designsystem.theme.SequoHubTheme
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings
import dev.orestegabo.sequohub.core.localization.stringsFor
import dev.orestegabo.sequohub.feature.activity.ActivityScreen
import dev.orestegabo.sequohub.feature.auth.AuthScreen
import dev.orestegabo.sequohub.feature.hub.HubDashboard
import dev.orestegabo.sequohub.feature.hub.LockerDetailOverlay
import dev.orestegabo.sequohub.feature.hub.sampleLockers
import dev.orestegabo.sequohub.feature.legal.LegalScreen
import dev.orestegabo.sequohub.feature.settings.SettingsScreen
import dev.orestegabo.sequohub.feature.settings.SettingsUiState
import dev.orestegabo.sequohub.feature.settings.ThemeMode
import dev.orestegabo.sequohub.feature.splash.SplashScreen
import dev.orestegabo.sequohub.navigation.MainTab
import dev.orestegabo.sequohub.navigation.SequoBottomNavigation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
@Preview
fun App(
    onGoogleSignIn: suspend () -> GoogleSignInResult = {
        GoogleSignInResult.Failure("Google sign-in is not configured on this platform yet.")
    },
) {
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
                onGoogleSignIn = onGoogleSignIn,
            )
        }
    }
}

@Composable
private fun SequoHubApp(
    settings: SettingsUiState,
    onSettingsChange: (SettingsUiState) -> Unit,
    onGoogleSignIn: suspend () -> GoogleSignInResult,
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var showLegalScreen by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Hub) }
    var selectedLockerId by rememberSaveable { mutableStateOf<String?>(null) }
    var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var googleSignInInProgress by rememberSaveable { mutableStateOf(false) }
    var apiHealthState by remember { mutableStateOf<ApiHealthUiState>(ApiHealthUiState.Idle) }
    val authSessionStore = rememberAuthSessionStore()
    val authRepository = remember(authSessionStore) {
        AuthRepository(
            authApiClient = AuthApiClient(),
            sessionStore = authSessionStore,
        )
    }
    val apiHealthClient = remember { ApiHealthClient() }
    val scope = rememberCoroutineScope()
    val strings = LocalSequoStrings.current
    val lockers = sampleLockers(strings)
    val hubBlockedBySequo = false
    val selectedLocker = lockers.fastFirstOrNull { it.id == selectedLockerId }

    DisposableEffect(authRepository, apiHealthClient) {
        onDispose {
            authRepository.close()
            apiHealthClient.close()
        }
    }

    LaunchedEffect(authRepository) {
        isAuthenticated = authRepository.getSavedSession() != null
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    if (!isAuthenticated) {
        if (showLegalScreen) {
            LegalScreen(onBack = { showLegalScreen = false })
        } else {
            AuthScreen(
                language = settings.language,
                onLanguageChange = { onSettingsChange(settings.copy(language = it)) },
                onLogin = { isAuthenticated = true },
                googleSignInInProgress = googleSignInInProgress,
                onGoogleLogin = {
                    if (googleSignInInProgress) return@AuthScreen
                    scope.launch {
                        googleSignInInProgress = true
                        authErrorMessage = null
                        try {
                            val result = runCatching {
                                onGoogleSignIn()
                            }.getOrElse { error ->
                                if (error is CancellationException) throw error
                                println("Google sign-in failed before backend login: ${error.message}")
                                GoogleSignInResult.Failure(error.message ?: "Google sign-in failed.")
                            }

                            when (result) {
                                is GoogleSignInResult.Success -> {
                                    println("Google sign-in succeeded locally; sending token to backend.")
                                    runCatching {
                                        authRepository.loginWithGoogle(result.idToken)
                                    }.onSuccess {
                                        authErrorMessage = null
                                        isAuthenticated = true
                                    }.onFailure { error ->
                                        if (error is CancellationException) throw error
                                        println("Google backend login failed: ${error.message}")
                                        authErrorMessage = error.message ?: "Backend Google login failed."
                                    }
                                }
                                GoogleSignInResult.Cancelled -> {
                                    println("Google sign-in cancelled by the user or provider.")
                                }
                                is GoogleSignInResult.Failure -> {
                                    println("Google sign-in failed: ${result.message}")
                                    authErrorMessage = result.message
                                }
                            }
                        } finally {
                            googleSignInInProgress = false
                        }
                    }
                },
                onAppleLogin = { isAuthenticated = true },
                onPrivacyTermsClick = { showLegalScreen = true },
            )
        }
        authErrorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { authErrorMessage = null },
                title = { Text("Google sign-in") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { authErrorMessage = null }) {
                        Text("OK")
                    }
                },
            )
        }
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
                apiHealthState = apiHealthState,
                onCheckApiHealth = {
                    apiHealthState = ApiHealthUiState.Checking
                    scope.launch {
                        val result = apiHealthClient.checkReachability()
                        apiHealthState = if (result.requestReachedBackend) {
                            ApiHealthUiState.Online(result)
                        } else {
                            ApiHealthUiState.Offline(result)
                        }
                    }
                },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        selectedTab = MainTab.Hub
                        selectedLockerId = null
                        isAuthenticated = false
                    }
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
