package com.notecraft.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = AppColors.primaryLight,
    onPrimary = AppColors.onPrimaryLight,
    primaryContainer = AppColors.primaryContainerLight,
    secondary = AppColors.secondaryLight,
    tertiary = AppColors.tertiaryLight,
    surface = AppColors.surfaceLight,
    background = AppColors.backgroundLight,
    onSurface = AppColors.onSurfaceLight,
    onBackground = AppColors.onBackgroundLight,
    surfaceVariant = AppColors.surfaceVariantLight,
    outline = AppColors.outlineLight,
    error = AppColors.errorLight
)

private val DarkColors = darkColorScheme(
    primary = AppColors.primaryDark,
    onPrimary = AppColors.onPrimaryDark,
    primaryContainer = AppColors.primaryContainerDark,
    secondary = AppColors.secondaryDark,
    tertiary = AppColors.tertiaryDark,
    surface = AppColors.surfaceDark,
    background = AppColors.backgroundDark,
    onSurface = AppColors.onSurfaceDark,
    onBackground = AppColors.onBackgroundDark,
    surfaceVariant = AppColors.surfaceVariantDark,
    outline = AppColors.outlineDark,
    error = AppColors.errorDark
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
)

@Composable
fun NotecraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontSize: Int = 14,
    content: @Composable () -> Unit
) {
    val typography = remember(fontSize) { AppTypography.toMaterial3(fontSize) }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        typography = typography,
        content = content
    )
}
