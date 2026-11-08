package dev.orestegabo.sequohub.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.orestegabo.sequo.design.SequoDarkColors
import dev.orestegabo.sequo.design.SequoLightColors
import dev.orestegabo.sequo.design.SequoThemeShapes
import dev.orestegabo.sequo.design.SequoTypography

val LightColorScheme = SequoLightColors
val DarkColorScheme = SequoDarkColors

@Composable
fun SequoHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    content: @Composable () -> Unit,
) {
    val semanticColors = SequoSemanticColors(
        success = colorScheme.primary, onSuccess = colorScheme.onPrimary,
        successContainer = colorScheme.primaryContainer, onSuccessContainer = colorScheme.onPrimaryContainer,
        warning = colorScheme.tertiary, onWarning = colorScheme.onTertiary,
        warningContainer = colorScheme.tertiaryContainer, onWarningContainer = colorScheme.onTertiaryContainer,
        info = colorScheme.secondary, onInfo = colorScheme.onSecondary,
        infoContainer = colorScheme.secondaryContainer, onInfoContainer = colorScheme.onSecondaryContainer,
        danger = colorScheme.error, onDanger = colorScheme.onError,
        dangerContainer = colorScheme.errorContainer, onDangerContainer = colorScheme.onErrorContainer,
    )
    CompositionLocalProvider(LocalSequoSemanticColors provides semanticColors) {
        MaterialTheme(colorScheme = colorScheme, typography = SequoTypography,
            shapes = SequoThemeShapes, content = content)
    }
}
