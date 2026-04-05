package com.z2a.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Z2aDarkColorScheme = darkColorScheme(
    primary = Z2aGreen,
    onPrimary = Z2aBackground,
    primaryContainer = Z2aGreenSurface,
    onPrimaryContainer = Z2aGreen,
    secondary = Z2aBlue,
    onSecondary = Z2aBackground,
    secondaryContainer = Z2aSurfaceVariant,
    onSecondaryContainer = Z2aBlue,
    tertiary = Z2aAmber,
    onTertiary = Z2aBackground,
    error = Z2aRed,
    onError = Z2aBackground,
    errorContainer = Z2aRedSurface,
    onErrorContainer = Z2aRed,
    background = Z2aBackground,
    onBackground = Z2aOnBackground,
    surface = Z2aSurface,
    onSurface = Z2aOnSurface,
    surfaceVariant = Z2aSurfaceVariant,
    onSurfaceVariant = Z2aOnSurfaceDim,
    outline = Z2aOutline,
    outlineVariant = Z2aOutlineVariant
)

@Composable
fun Z2aTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Z2aDarkColorScheme,
        typography = Z2aTypography,
        content = content
    )
}
