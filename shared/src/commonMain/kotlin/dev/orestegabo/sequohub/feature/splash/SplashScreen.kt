package dev.orestegabo.sequohub.feature.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import dev.orestegabo.sequohub.core.designsystem.theme.SequoHubTheme
import kotlinx.coroutines.delay
import kotlin.math.max

private const val SplashTimeoutMillis = 2_000L

@Composable
fun SplashScreen(
    onTimeout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        delay(SplashTimeoutMillis)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colorScheme.primary,
                        colorScheme.primaryContainer,
                        colorScheme.surfaceVariant,
                    ),
                )
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val primary = colorScheme.primary
            val primaryContainer = colorScheme.primaryContainer
            val surfaceVariant = colorScheme.surfaceVariant

            drawDiagonalGrid(
                width = w,
                height = h,
                color = surfaceVariant.copy(alpha = 0.14f),
                spacing = max(w, h) / 8f,
                strokeWidth = max(w, h) / 420f,
                rising = true,
            )
            drawDiagonalGrid(
                width = w,
                height = h,
                color = primary.copy(alpha = 0.10f),
                spacing = max(w, h) / 6f,
                strokeWidth = max(w, h) / 520f,
                rising = false,
            )

            val largeStroke = max(w, h) / 16f
            val mediumStroke = max(w, h) / 30f
            val fineStroke = max(w, h) / 90f

            drawChevronPath(
                points = listOf(
                    Offset(-0.18f * w, 0.22f * h),
                    Offset(0.24f * w, 0.47f * h),
                    Offset(0.68f * w, 0.19f * h),
                    Offset(1.18f * w, 0.50f * h),
                ),
                color = primaryContainer.copy(alpha = 0.24f),
                strokeWidth = largeStroke,
            )
            drawChevronPath(
                points = listOf(
                    Offset(-0.10f * w, 0.72f * h),
                    Offset(0.22f * w, 0.54f * h),
                    Offset(0.52f * w, 0.74f * h),
                    Offset(0.88f * w, 0.50f * h),
                    Offset(1.12f * w, 0.64f * h),
                ),
                color = primary.copy(alpha = 0.16f),
                strokeWidth = mediumStroke,
            )
            drawChevronPath(
                points = listOf(
                    Offset(0.08f * w, -0.08f * h),
                    Offset(0.42f * w, 0.19f * h),
                    Offset(0.76f * w, 0.02f * h),
                    Offset(1.10f * w, 0.22f * h),
                ),
                color = surfaceVariant.copy(alpha = 0.18f),
                strokeWidth = mediumStroke,
            )
            drawChevronPath(
                points = listOf(
                    Offset(0.02f * w, 1.04f * h),
                    Offset(0.34f * w, 0.78f * h),
                    Offset(0.66f * w, 0.94f * h),
                    Offset(1.02f * w, 0.70f * h),
                ),
                color = primaryContainer.copy(alpha = 0.18f),
                strokeWidth = mediumStroke,
            )

            drawAngularFacet(
                points = listOf(
                    Offset(0.04f * w, 0.10f * h),
                    Offset(0.30f * w, 0.26f * h),
                    Offset(0.18f * w, 0.43f * h),
                    Offset(-0.04f * w, 0.28f * h),
                ),
                color = surfaceVariant.copy(alpha = 0.10f),
            )
            drawAngularFacet(
                points = listOf(
                    Offset(0.78f * w, 0.12f * h),
                    Offset(1.06f * w, 0.30f * h),
                    Offset(0.90f * w, 0.48f * h),
                    Offset(0.62f * w, 0.31f * h),
                ),
                color = primary.copy(alpha = 0.08f),
            )
            drawAngularFacet(
                points = listOf(
                    Offset(0.42f * w, 0.62f * h),
                    Offset(0.78f * w, 0.82f * h),
                    Offset(0.55f * w, 1.08f * h),
                    Offset(0.20f * w, 0.86f * h),
                ),
                color = primaryContainer.copy(alpha = 0.12f),
            )

            drawChevronPath(
                points = listOf(
                    Offset(-0.06f * w, 0.40f * h),
                    Offset(0.30f * w, 0.58f * h),
                    Offset(0.62f * w, 0.40f * h),
                    Offset(1.06f * w, 0.58f * h),
                ),
                color = surfaceVariant.copy(alpha = 0.22f),
                strokeWidth = fineStroke,
            )
            drawChevronPath(
                points = listOf(
                    Offset(0.16f * w, 0.02f * h),
                    Offset(0.50f * w, 0.22f * h),
                    Offset(0.84f * w, 0.02f * h),
                ),
                color = primary.copy(alpha = 0.14f),
                strokeWidth = fineStroke,
            )
        }
    }
}

private fun DrawScope.drawDiagonalGrid(
    width: Float,
    height: Float,
    color: Color,
    spacing: Float,
    strokeWidth: Float,
    rising: Boolean,
) {
    val count = ((width + height) / spacing).toInt() + 2
    for (index in -1..count) {
        val startX = index * spacing
        val start = if (rising) {
            Offset(startX, height)
        } else {
            Offset(startX, 0f)
        }
        val end = if (rising) {
            Offset(startX - height, 0f)
        } else {
            Offset(startX + height, height)
        }
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawChevronPath(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

private fun DrawScope.drawAngularFacet(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 3) return

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }

    drawPath(
        path = path,
        color = color,
    )
}

@Preview(name = "Splash Light")
@Composable
fun SplashScreenLightPreview() {
    SequoHubTheme(darkTheme = false) {
        SplashScreen()
    }
}

@Preview(name = "Splash Dark")
@Composable
fun SplashScreenDarkPreview() {
    SequoHubTheme(darkTheme = true) {
        SplashScreen()
    }
}
