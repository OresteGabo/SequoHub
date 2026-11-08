package dev.orestegabo.sequohub.feature.live
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
@Composable actual fun HubBackHandler(enabled: Boolean, onBack: () -> Unit) { BackHandler(enabled, onBack) }
