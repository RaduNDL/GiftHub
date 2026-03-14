package com.example.gifthub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GiftHubColorScheme = lightColorScheme(
    primary = AppPrimary,
    background = AppBackground,
    surface = AppSurface,
    primaryContainer = AppPrimaryContainer,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = AppText,
    onSurface = AppText
)

@Composable
fun GiftHubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GiftHubColorScheme,
        typography = Typography,
        content = content
    )
}