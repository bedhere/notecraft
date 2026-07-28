package com.notecraft.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object AppShapes {
    val compact = RoundedCornerShape(4.dp)
    val control = RoundedCornerShape(6.dp)
    val panel = RoundedCornerShape(8.dp)
    val pill = RoundedCornerShape(999.dp)

    val material = Shapes(
        small = compact,
        medium = control,
        large = panel
    )
}
