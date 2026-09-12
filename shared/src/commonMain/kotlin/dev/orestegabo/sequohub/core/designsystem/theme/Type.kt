package dev.orestegabo.sequohub.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge,
    displayMedium = baseline.displayMedium,
    displaySmall = baseline.displaySmall,
    headlineLarge = baseline.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleLarge = baseline.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleMedium = baseline.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleSmall = baseline.titleSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    bodyLarge = baseline.bodyLarge.copy(letterSpacing = 0.sp),
    bodyMedium = baseline.bodyMedium.copy(letterSpacing = 0.sp),
    bodySmall = baseline.bodySmall.copy(letterSpacing = 0.sp),
    labelLarge = baseline.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    labelMedium = baseline.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    labelSmall = baseline.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
)

val CounterNumberTextStyle = TextStyle(
    fontSize = 28.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.sp,
)
