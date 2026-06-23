package com.gainsmaxxing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = BgBase,
    surface = Surface1,
    surfaceVariant = Surface2,
    surfaceContainer = Surface3,
    surfaceContainerHigh = Surface4,
    primary = Green500,
    onPrimary = TextPrimary,
    secondary = Amber500,
    tertiary = Blue500,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = BorderSubtle,
    error = Red500,
)

@Composable
fun GainsmaxxingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content,
    )
}
