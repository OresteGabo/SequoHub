package dev.orestegabo.sequohub.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class SequoSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val danger: Color,
    val onDanger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
)

val LightSequoSemanticColors = SequoSemanticColors(
    success = Color(0xFF0E7A53),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD2F3DF),
    onSuccessContainer = Color(0xFF003821),
    warning = Color(0xFF8A6200),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE4A6),
    onWarningContainer = Color(0xFF2A1B00),
    info = Color(0xFF006A6C),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFBDEDEF),
    onInfoContainer = Color(0xFF002021),
    danger = Color(0xFFBA1A1A),
    onDanger = Color(0xFFFFFFFF),
    dangerContainer = Color(0xFFFFDAD6),
    onDangerContainer = Color(0xFF410002),
)

val DarkSequoSemanticColors = SequoSemanticColors(
    success = Color(0xFF74D8A4),
    onSuccess = Color(0xFF003822),
    successContainer = Color(0xFF005235),
    onSuccessContainer = Color(0xFFBFF2D5),
    warning = Color(0xFFF4B400),
    onWarning = Color(0xFF3A2A00),
    warningContainer = Color(0xFF654700),
    onWarningContainer = Color(0xFFFFE4A6),
    info = Color(0xFF7CD1D7),
    onInfo = Color(0xFF00363A),
    infoContainer = Color(0xFF00535A),
    onInfoContainer = Color(0xFFBDEDEF),
    danger = Color(0xFFFFB4AB),
    onDanger = Color(0xFF690005),
    dangerContainer = Color(0xFF93000A),
    onDangerContainer = Color(0xFFFFDAD6),
)

val LocalSequoSemanticColors = staticCompositionLocalOf { LightSequoSemanticColors }

val MaterialTheme.sequoSemanticColors: SequoSemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSequoSemanticColors.current
