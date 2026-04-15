package com.example.gifthub.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val FuturisticColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = ElectricBlue,
    background = VoidBackground,
    surface = CyberSurface,
    onPrimary = VoidBackground,
    onSecondary = TextHoloWhite,
    onBackground = TextHoloWhite,
    onSurface = TextHoloWhite,
    onSurfaceVariant = TextMutedBlue,
    error = ErrorNeonRed,
    errorContainer = ErrorNeonRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorNeonRed
)

@Composable
fun GiftHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FuturisticColorScheme,
        content = content
    )
}