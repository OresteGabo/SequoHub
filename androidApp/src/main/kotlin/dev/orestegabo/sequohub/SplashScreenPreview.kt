package dev.orestegabo.sequohub

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.orestegabo.sequohub.core.designsystem.theme.SequoHubTheme
import dev.orestegabo.sequohub.feature.splash.SplashScreen

@Preview(
    name = "Splash Screen Light",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
fun AndroidSplashScreenLightPreview() {
    SequoHubTheme(darkTheme = false) {
        SplashScreen(onTimeout = {})
    }
}

@Preview(
    name = "Splash Screen Dark",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
fun AndroidSplashScreenDarkPreview() {
    SequoHubTheme(darkTheme = true) {
        SplashScreen(onTimeout = {})
    }
}
