package com.ikki.recommendme.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/*
 * "Cinema Pop" palette - warm rose/orange gradient on deep indigo,
 * inspired by Netflix (dark, bold red) + MyAnimeList (colorful, playful).
 */

// Brand core
val Rose = Color(0xFFFF3D5A)
val RoseDeep = Color(0xFFE02040)
val RoseSoft = Color(0xFFFF6B81)
val SunsetOrange = Color(0xFFFF8A3D)
val GoldenStar = Color(0xFFFFC93D)
val Violet = Color(0xFF8B5CF6)
val Cyan = Color(0xFF22D3EE)
val Mint = Color(0xFF34D399)

// Dark theme surfaces
val DarkBackground = Color(0xFF0E1016)
val DarkSurface = Color(0xFF171A23)
val DarkSurfaceHigh = Color(0xFF1F2330)
val DarkSurfaceVariant = Color(0xFF272C3B)
val DarkOnSurface = Color(0xFFF2F3F7)
val DarkOnSurfaceMuted = Color(0xFF9AA0B0)

// Light theme surfaces
val LightBackground = Color(0xFFF8F7FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFEFEEF4)
val LightSurfaceVariant = Color(0xFFE6E4EE)
val LightOnSurface = Color(0xFF16171C)
val LightOnSurfaceMuted = Color(0xFF5C6070)

// Gradients
val BrandGradient: Brush
    get() = Brush.horizontalGradient(listOf(Rose, SunsetOrange))

val BrandGradientVertical: Brush
    get() = Brush.verticalGradient(listOf(Rose, SunsetOrange))

val PosterGradients: List<List<Color>> = listOf(
    listOf(Color(0xFF6D28D9), Color(0xFFDB2777)),
    listOf(Color(0xFF0F766E), Color(0xFF22D3EE)),
    listOf(Color(0xFFB45309), Color(0xFFFBBF24)),
    listOf(Color(0xFFBE123C), Color(0xFFFF8A3D)),
    listOf(Color(0xFF1D4ED8), Color(0xFF8B5CF6)),
    listOf(Color(0xFF047857), Color(0xFF34D399)),
    listOf(Color(0xFF7C2D12), Color(0xFFEF4444)),
    listOf(Color(0xFF334155), Color(0xFF22D3EE))
)

fun posterGradientFor(seed: String): Brush {
    val index = (seed.hashCode().let { if (it < 0) -it else it }) % PosterGradients.size
    val colors = PosterGradients[index]
    return Brush.linearGradient(colors)
}
