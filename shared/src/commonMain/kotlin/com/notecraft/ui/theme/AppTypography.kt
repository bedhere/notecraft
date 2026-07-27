package com.notecraft.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Notecraft typography tokens.
 * Uses system default font family for broad compatibility.
 */
object AppTypography {
    // Display
    val displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )
    val displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )

    // Headings
    val headingLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    val headingMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )
    val headingSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )

    // Body
    val bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    // Labels
    val labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    val labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )

    // Code (monospace)
    val codeFont = FontFamily.Monospace
    val codeStyle = TextStyle(
        fontFamily = codeFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    )

    /** Convert to Material 3 Typography for use with MaterialTheme */
    fun toMaterial3(fontSize: Int = 14): Typography {
        val scale = fontSize / 14f
        return Typography(
            displayLarge = displayLarge.copy(fontSize = (displayLarge.fontSize.value * scale).sp),
            displayMedium = displayMedium.copy(fontSize = (displayMedium.fontSize.value * scale).sp),
            headlineLarge = headingLarge.copy(fontSize = (headingLarge.fontSize.value * scale).sp),
            headlineMedium = headingMedium.copy(fontSize = (headingMedium.fontSize.value * scale).sp),
            headlineSmall = headingSmall.copy(fontSize = (headingSmall.fontSize.value * scale).sp),
            bodyLarge = bodyLarge.copy(fontSize = (bodyLarge.fontSize.value * scale).sp),
            bodyMedium = bodyMedium.copy(fontSize = (bodyMedium.fontSize.value * scale).sp),
            bodySmall = bodySmall.copy(fontSize = (bodySmall.fontSize.value * scale).sp),
            labelLarge = labelLarge.copy(fontSize = (labelLarge.fontSize.value * scale).sp),
            labelMedium = labelMedium.copy(fontSize = (labelMedium.fontSize.value * scale).sp),
            labelSmall = labelSmall.copy(fontSize = (labelSmall.fontSize.value * scale).sp)
        )
    }
}
