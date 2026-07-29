package com.notecraft.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private val LightColors = lightColorScheme(
    primary = AppColors.primaryLight,
    onPrimary = AppColors.onPrimaryLight,
    primaryContainer = AppColors.primaryContainerLight,
    secondary = AppColors.secondaryLight,
    onSecondary = AppColors.onPrimaryLight,
    tertiary = AppColors.tertiaryLight,
    onTertiary = AppColors.onPrimaryLight,
    surface = AppColors.surfaceLight,
    background = AppColors.backgroundLight,
    onSurface = AppColors.onSurfaceLight,
    onSurfaceVariant = AppColors.secondaryLight,
    onBackground = AppColors.onBackgroundLight,
    surfaceVariant = AppColors.surfaceVariantLight,
    outline = AppColors.outlineLight,
    outlineVariant = AppColors.dividerLight,
    inverseSurface = AppColors.onSurfaceLight,
    inverseOnSurface = AppColors.surfaceLight,
    inversePrimary = AppColors.primaryDark,
    surfaceContainerLowest = AppColors.editorLight,
    surfaceContainerLow = AppColors.backgroundLight,
    surfaceContainer = AppColors.surfaceVariantLight,
    surfaceContainerHigh = AppColors.hoverLight,
    surfaceContainerHighest = AppColors.primaryContainerLight,
    error = AppColors.errorLight
)

private val DarkColors = darkColorScheme(
    primary = AppColors.primaryDark,
    onPrimary = AppColors.onPrimaryDark,
    primaryContainer = AppColors.primaryContainerDark,
    secondary = AppColors.secondaryDark,
    onSecondary = AppColors.onPrimaryDark,
    tertiary = AppColors.tertiaryDark,
    onTertiary = AppColors.onPrimaryDark,
    surface = AppColors.surfaceDark,
    background = AppColors.backgroundDark,
    onSurface = AppColors.onSurfaceDark,
    onSurfaceVariant = AppColors.secondaryDark,
    onBackground = AppColors.onBackgroundDark,
    surfaceVariant = AppColors.surfaceVariantDark,
    outline = AppColors.outlineDark,
    outlineVariant = AppColors.dividerDark,
    inverseSurface = AppColors.onSurfaceDark,
    inverseOnSurface = AppColors.surfaceDark,
    inversePrimary = AppColors.primaryLight,
    surfaceContainerLowest = AppColors.editorDark,
    surfaceContainerLow = AppColors.backgroundDark,
    surfaceContainer = AppColors.surfaceVariantDark,
    surfaceContainerHigh = AppColors.hoverDark,
    surfaceContainerHighest = AppColors.primaryContainerDark,
    error = AppColors.errorDark
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
        shapes = AppShapes.material,
        typography = typography,
        content = content
    )
}
