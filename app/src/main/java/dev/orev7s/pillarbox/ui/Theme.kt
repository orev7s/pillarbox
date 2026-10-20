package dev.orev7s.pillarbox.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFF8B7CF6)
val AccentSoft = Color(0xFF232045)
val Mint = Color(0xFF3DDC97)
val Amber = Color(0xFFF0B45C)
val Danger = Color(0xFFF87171)
val Ink = Color(0xFF0A0C11)
val CardBg = Color(0xFF12161E)
val CardAlt = Color(0xFF181D27)
val Line = Color(0xFF232A36)
val TextMain = Color(0xFFE9ECF3)
val TextDim = Color(0xFF8B94A6)

private val Scheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF0A0C11),
    primaryContainer = AccentSoft,
    onPrimaryContainer = Color(0xFFE3DDFF),
    secondary = Mint,
    onSecondary = Color(0xFF04170F),
    background = Ink,
    onBackground = TextMain,
    surface = CardBg,
    onSurface = TextMain,
    surfaceVariant = CardAlt,
    onSurfaceVariant = TextDim,
    outline = Line,
    error = Danger
)

@Composable
fun PillarBoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = Typography(),
        content = content
    )
}
