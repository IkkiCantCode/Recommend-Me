package com.ikki.recommendme.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Rose,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = RoseDeep,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = Violet,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = SunsetOrange,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    surfaceContainer = DarkSurfaceHigh,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerLow = DarkSurface,
    outline = DarkSurfaceVariant,
    error = Color(0xFFFF6B6B)
)

private val LightColorScheme = lightColorScheme(
    primary = RoseDeep,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = RoseSoft,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = Violet,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = SunsetOrange,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    surfaceContainer = LightSurfaceHigh,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerLow = LightSurface,
    outline = LightSurfaceVariant,
    error = Color(0xFFD32F2F)
)

/**
 * App theme. Dark/light is decided in MainActivity from the persisted
 * [com.ikki.recommendme.domain.model.ThemeMode] preference, so the in-app
 * toggle controls it instead of the system setting.
 */
@Composable
fun RecommendMeTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
