package com.notecraft.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B8C5A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EAD3),
    secondary = Color(0xFF8B7D6B),
    surface = Color(0xFFFEFCF5),
    background = Color(0xFFFEFCF5),
    onSurface = Color(0xFF2C2C2C),
    onBackground = Color(0xFF2C2C2C),
    outline = Color(0xFFD4C9B8)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DB07A),
    onPrimary = Color(0xFF1B3A1A),
    primaryContainer = Color(0xFF2D5A2C),
    secondary = Color(0xFFA89880),
    surface = Color(0xFF1E1E1E),
    background = Color(0xFF121212),
    onSurface = Color(0xFFE4E0D9),
    onBackground = Color(0xFFE4E0D9),
    outline = Color(0xFF3E3E3E)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun NotecraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
