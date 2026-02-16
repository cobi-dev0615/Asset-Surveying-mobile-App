package com.seretail.inventarios.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SERDarkColorScheme = darkColorScheme(
    primary = SERBlue,
    onPrimary = TextPrimary,
    primaryContainer = SERBlueDark,
    onPrimaryContainer = TextPrimary,
    secondary = StatusFound,
    onSecondary = TextPrimary,
    tertiary = StatusTransferred,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = Error,
    onError = TextPrimary,
    inverseSurface = DarkSurfaceVariant,
    inverseOnSurface = TextPrimary,
)

@Composable
fun SERTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SERDarkColorScheme,
        typography = SERTypography,
        content = content,
    )
}
