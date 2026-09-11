package com.softphone.studio.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OledDarkColorScheme = darkColorScheme(
    primary = TextPrimary,
    onPrimary = OledBlack,
    secondary = TextSecondary,
    onSecondary = TextPrimary,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = OledSurface,
    onSurface = TextPrimary,
    surfaceVariant = OledSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = TextPrimary
)

@Composable
fun SoftphoneStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OledDarkColorScheme,
        typography = Typography,
        content = content
    )
}
