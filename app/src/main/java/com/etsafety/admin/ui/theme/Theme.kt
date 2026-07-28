package com.etsafety.admin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = White,
    primaryContainer = Blue80,
    onPrimaryContainer = Blue10,
    secondary = DarkGray,
    onSecondary = White,
    secondaryContainer = LightGray,
    onSecondaryContainer = NearBlack,
    tertiary = Green50,
    onTertiary = White,
    background = OffWhite,
    onBackground = NearBlack,
    surface = White,
    onSurface = NearBlack,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    error = Red50,
    onError = White,
    outline = MediumGray,
    outlineVariant = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue40,
    onPrimaryContainer = Blue80,
    secondary = MediumGray,
    onSecondary = NearBlack,
    secondaryContainer = DarkGray,
    onSecondaryContainer = White,
    tertiary = Green50,
    onTertiary = White,
    background = NearBlack,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = MediumGray,
    error = Red50,
    onError = White,
    outline = MediumGray,
    outlineVariant = DarkGray
)

@Composable
fun ETSafetyAdminTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
