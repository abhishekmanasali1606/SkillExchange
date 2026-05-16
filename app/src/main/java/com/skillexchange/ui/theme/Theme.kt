package com.skillexchange.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green800 = Color(0xFF2E7D32)
val Green600 = Color(0xFF43A047)
val Green100 = Color(0xFFE8F5E9)
val Amber600  = Color(0xFFFF8F00)
val Amber100  = Color(0xFFFFF3E0)
val Beige     = Color(0xFFF5F0E8)
val Gray100   = Color(0xFFF5F5F5)
val Gray400   = Color(0xFF9E9E9E)
val Gray700   = Color(0xFF616161)
val Border    = Color(0xFFE0D8CC)
val White     = Color(0xFFFFFFFF)
val Black     = Color(0xFF1C1B1F)

private val LightColors = lightColorScheme(
    primary = Green800,
    onPrimary = White,
    primaryContainer = Green100,
    onPrimaryContainer = Green800,
    secondary = Amber600,
    onSecondary = White,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber600,
    background = Beige,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Border
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF388E3C),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFFE65100),
    secondaryContainer = Color(0xFFF57C00),
    onSecondaryContainer = Color(0xFFFFE0B2),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE1E1E1),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE1E1E1),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFBBBBBB),
    outline = Color(0xFF444444)
)

@Composable
fun SkillExchangeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
