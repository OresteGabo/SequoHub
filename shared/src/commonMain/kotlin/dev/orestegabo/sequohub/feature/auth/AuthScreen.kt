package dev.orestegabo.sequohub.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.Package2
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sequohub.shared.generated.resources.Res
import sequohub.shared.generated.resources.onboarding_history
import sequohub.shared.generated.resources.onboarding_pickup_flow
import sequohub.shared.generated.resources.onboarding_scan_arrivals

@Composable
fun AuthScreen(
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEmailFallback by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer.copy(alpha = 0.86f),
                        colorScheme.background,
                        colorScheme.surfaceContainerLow,
                    ),
                ),
            ),
    ) {
        AuthBackgroundIcons()

        if (showEmailFallback) {
            EmailFallbackPage(
                onBack = { showEmailFallback = false },
                onLogin = onLogin,
                onPrivacyTermsClick = onPrivacyTermsClick,
            )
        } else {
            SocialAuthPage(
                onAppleLogin = onAppleLogin,
                onGoogleLogin = onGoogleLogin,
                onEmailFallback = { showEmailFallback = true },
                onPrivacyTermsClick = onPrivacyTermsClick,
            )
        }
    }
}

@Composable
private fun SocialAuthPage(
    onAppleLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onEmailFallback: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BrandMark()

        OnboardingPager()

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AppleButton(onClick = onAppleLogin)
            GoogleButton(onClick = onGoogleLogin)

            Text(
                text = "Use email instead",
                color = colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEmailFallback)
                    .padding(top = 2.dp, bottom = 6.dp),
            )

            TermsLine(onPrivacyTermsClick = onPrivacyTermsClick)
        }
    }
}

@Composable
private fun OnboardingPager() {
    val slides = listOf(
        OnboardingSlide(
            title = "Scan every colis",
            subtitle = "New seller drop-offs, livreur arrivals, customer pickups, and returns all start from one QR scan.",
            illustration = Res.drawable.onboarding_scan_arrivals,
        ),
        OnboardingSlide(
            title = "Know the next action",
            subtitle = "See the right flow immediately: store a package, collect fees, open a locker, or receive a return.",
            illustration = Res.drawable.onboarding_pickup_flow,
        ),
        OnboardingSlide(
            title = "Keep the hub traceable",
            subtitle = "Follow locker status, sync state, pickups, and returns in one simple history.",
            illustration = Res.drawable.onboarding_history,
        ),
    )
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val colorScheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 14.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            OnboardingCard(slide = slides[page])
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slides.indices.forEach { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (selected) 22.dp else 8.dp, height = 8.dp)
                        .background(
                            color = if (selected) colorScheme.primary else colorScheme.outlineVariant,
                            shape = SequoHubShapes.IconCapsule,
                        ),
                )
            }
        }
    }
}

@Composable
private fun OnboardingCard(slide: OnboardingSlide) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(slide.illustration),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(166.dp),
            contentScale = ContentScale.Fit,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = slide.title,
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = slide.subtitle,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val illustration: DrawableResource,
)

@Composable
private fun EmailFallbackPage(
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.onBackground,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                text = "Email sign in",
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Only use this if Apple or Google is unavailable.",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = SequoHubShapes.Card,
            color = colorScheme.surface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.64f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    label = { Text("Email address") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AlternateEmail,
                            contentDescription = null,
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                    ),
                    shape = SequoHubShapes.Small,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password,
                    ),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    shape = SequoHubShapes.Small,
                )

                Button(
                    onClick = onLogin,
                    enabled = email.isNotBlank() && password.length >= 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = SequoHubShapes.Small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        TermsLine(onPrivacyTermsClick = onPrivacyTermsClick)
    }
}

@Composable
private fun BrandMark() {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = SequoHubShapes.Card,
            color = colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Package2,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column {
            Text(
                text = "SequoHub",
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Relay counter",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun AppleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = SequoHubShapes.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF090A0C),
            contentColor = Color.White,
        ),
    ) {
        Icon(
            imageVector = AppleIcon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Continue with Apple",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GoogleButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = SequoHubShapes.Small,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.72f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colorScheme.surface.copy(alpha = 0.9f),
            contentColor = colorScheme.onSurface,
        ),
    ) {
        Icon(
            imageVector = GoogleIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Continue with Google",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TermsLine(onPrivacyTermsClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "By continuing you accept ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Privacy & Terms",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onPrivacyTermsClick),
        )
    }
}

@Composable
private fun BoxScope.AuthBackgroundIcons() {
    val colorScheme = MaterialTheme.colorScheme
    BackgroundIcon(
        icon = Icons.Filled.Package2,
        contentDescription = null,
        tint = colorScheme.primary.copy(alpha = 0.11f),
        size = 132,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 28.dp, y = 88.dp)
            .rotate(14f),
    )
    BackgroundIcon(
        icon = Icons.Filled.QrCodeScanner,
        contentDescription = null,
        tint = colorScheme.tertiary.copy(alpha = 0.13f),
        size = 92,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = (-26).dp, y = (-40).dp)
            .rotate(-12f),
    )
    BackgroundIcon(
        icon = Icons.Filled.Lock,
        contentDescription = null,
        tint = colorScheme.primary.copy(alpha = 0.08f),
        size = 116,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 30.dp, y = (-130).dp)
            .rotate(-10f),
    )
}

@Composable
private fun BackgroundIcon(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    size: Int,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size.dp),
    )
}

private val GoogleIcon: ImageVector
    get() {
        if (_googleIcon != null) {
            return _googleIcon!!
        }
        _googleIcon = ImageVector.Builder(
            name = "Brand.Google",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF4285F4))) {
                moveTo(23.49f, 12.27f)
                curveTo(23.49f, 11.48f, 23.42f, 10.73f, 23.3f, 10f)
                horizontalLineTo(12f)
                verticalLineTo(14.51f)
                horizontalLineTo(18.47f)
                curveTo(18.19f, 15.96f, 17.36f, 17.19f, 16.11f, 18.02f)
                verticalLineTo(20.95f)
                horizontalLineTo(19.89f)
                curveTo(22.09f, 18.92f, 23.49f, 15.93f, 23.49f, 12.27f)
                close()
            }
            path(fill = SolidColor(Color(0xFF34A853))) {
                moveTo(12f, 24f)
                curveTo(15.24f, 24f, 17.95f, 22.93f, 19.89f, 20.95f)
                lineTo(16.11f, 18.02f)
                curveTo(15.06f, 18.72f, 13.72f, 19.13f, 12f, 19.13f)
                curveTo(8.87f, 19.13f, 6.22f, 17.02f, 5.27f, 14.18f)
                horizontalLineTo(1.36f)
                verticalLineTo(17.2f)
                curveTo(3.29f, 21.04f, 7.26f, 24f, 12f, 24f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFBBC05))) {
                moveTo(5.27f, 14.18f)
                curveTo(5.03f, 13.48f, 4.9f, 12.74f, 4.9f, 12f)
                curveTo(4.9f, 11.26f, 5.03f, 10.52f, 5.27f, 9.82f)
                verticalLineTo(6.8f)
                horizontalLineTo(1.36f)
                curveTo(0.57f, 8.38f, 0.11f, 10.14f, 0.11f, 12f)
                curveTo(0.11f, 13.86f, 0.57f, 15.62f, 1.36f, 17.2f)
                lineTo(5.27f, 14.18f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEA4335))) {
                moveTo(12f, 4.87f)
                curveTo(13.76f, 4.87f, 15.35f, 5.48f, 16.59f, 6.67f)
                lineTo(19.97f, 3.29f)
                curveTo(17.94f, 1.4f, 15.23f, 0f, 12f, 0f)
                curveTo(7.26f, 0f, 3.29f, 2.96f, 1.36f, 6.8f)
                lineTo(5.27f, 9.82f)
                curveTo(6.22f, 6.98f, 8.87f, 4.87f, 12f, 4.87f)
                close()
            }
        }.build()
        return _googleIcon!!
    }

private var _googleIcon: ImageVector? = null

private val AppleIcon: ImageVector
    get() {
        if (_appleIcon != null) {
            return _appleIcon!!
        }
        _appleIcon = ImageVector.Builder(
            name = "Brand.Apple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.05f, 12.54f)
                curveTo(17.02f, 9.95f, 19.16f, 8.71f, 19.25f, 8.65f)
                curveTo(18.04f, 6.88f, 16.17f, 6.64f, 15.53f, 6.62f)
                curveTo(13.96f, 6.46f, 12.44f, 7.56f, 11.64f, 7.56f)
                curveTo(10.83f, 7.56f, 9.61f, 6.64f, 8.29f, 6.67f)
                curveTo(6.59f, 6.7f, 5.0f, 7.69f, 4.13f, 9.25f)
                curveTo(2.33f, 12.36f, 3.67f, 16.94f, 5.39f, 19.45f)
                curveTo(6.25f, 20.67f, 7.25f, 22.04f, 8.55f, 21.99f)
                curveTo(9.82f, 21.94f, 10.3f, 21.18f, 11.84f, 21.18f)
                curveTo(13.37f, 21.18f, 13.82f, 21.99f, 15.15f, 21.96f)
                curveTo(16.52f, 21.94f, 17.38f, 20.73f, 18.21f, 19.49f)
                curveTo(19.2f, 18.08f, 19.6f, 16.68f, 19.62f, 16.61f)
                curveTo(19.58f, 16.59f, 17.08f, 15.63f, 17.05f, 12.54f)
                close()
                moveTo(14.5f, 4.95f)
                curveTo(15.2f, 4.09f, 15.67f, 2.93f, 15.54f, 1.75f)
                curveTo(14.53f, 1.8f, 13.27f, 2.45f, 12.54f, 3.29f)
                curveTo(11.89f, 4.04f, 11.31f, 5.25f, 11.47f, 6.38f)
                curveTo(12.61f, 6.47f, 13.77f, 5.79f, 14.5f, 4.95f)
                close()
            }
        }.build()
        return _appleIcon!!
    }

private var _appleIcon: ImageVector? = null
