package dev.orestegabo.sequohub.feature.live
import androidx.compose.runtime.Composable
@Composable expect fun HubBackHandler(enabled: Boolean, onBack: () -> Unit)
