package dev.orestegabo.sequohub.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    ),
    displayMedium = baseline.displayMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    ),
    displaySmall = baseline.displaySmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    headlineLarge = baseline.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = baseline.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = baseline.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = baseline.titleSmall.copy(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = baseline.bodyLarge.copy(
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = baseline.bodyMedium.copy(
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = baseline.bodySmall.copy(
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = baseline.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = baseline.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = baseline.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
)

val CounterNumberTextStyle = TextStyle(
    fontSize = 28.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.sp,
)
