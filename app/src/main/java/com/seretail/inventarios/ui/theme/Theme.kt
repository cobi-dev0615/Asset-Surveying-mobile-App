package com.seretail.inventarios.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

private val SERLightColorScheme = lightColorScheme(
    primary = SERBlue,
    onPrimary = Color.White,
    primaryContainer = SERBlueLight,
    onPrimaryContainer = Color.White,
    secondary = StatusFound,
    onSecondary = Color.White,
    tertiary = StatusTransferred,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = Error,
    onError = Color.White,
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkSurface,
)

@Composable
fun SERTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SERLightColorScheme,
        typography = SERTypography,
        content = content,
    )
}
