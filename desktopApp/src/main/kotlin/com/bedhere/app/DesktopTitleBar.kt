package com.bedhere.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import com.notecraft.ui.theme.AppSpacing
import com.notecraft.util.Strings

@Composable
fun DesktopTitleBar(
    windowScope: WindowScope,
    windowState: WindowState,
    currentNoteTitle: String?,
    onMinimize: () -> Unit,
    onQuickNote: () -> Unit,
    onSettingsClick: () -> Unit,
    onClose: () -> Unit
) {
    val toggleMaximize = {
        windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
            WindowPlacement.Floating
        } else {
            WindowPlacement.Maximized
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppSpacing.titleBarHeight)
    ) {
        // WindowDraggableArea handles window dragging and double-tap
        with(windowScope) {
            WindowDraggableArea(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(windowState.placement) {
                        detectTapGestures(onDoubleTap = { toggleMaximize() })
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 18.dp, end = 206.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource("notecraft_logo.png"),
                        contentDescription = Strings.appBrandName,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = Strings.appBrandName,
                        modifier = Modifier.widthIn(min = 54.dp, max = 110.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    currentNoteTitle?.let { title ->
                        Text(
                            text = " — ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Window control buttons - outside WindowDraggableArea
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WindowButton(
                label = Strings.quickNote,
                icon = QuickNoteIcon,
                onClick = onQuickNote
            )
            WindowButton(
                label = Strings.settings,
                icon = SettingsIcon,
                onClick = onSettingsClick
            )
            WindowDivider()
            WindowButton(
                label = Strings.minimize,
                icon = MinimizeIcon,
                onClick = onMinimize
            )
            WindowButton(
                label = if (windowState.placement == WindowPlacement.Maximized) Strings.restore else Strings.maximize,
                icon = if (windowState.placement == WindowPlacement.Maximized) RestoreIcon else MaximizeIcon,
                onClick = toggleMaximize
            )
            CloseButton(
                label = Strings.close,
                icon = CloseIcon,
                onClick = onClose
            )
        }
    }
}

@Composable
private fun WindowDivider() {
    Box(
        modifier = Modifier
            .height(18.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    )
}

// ---- Reusable icon composables ----

private val MinimizeIcon: @Composable (Color) -> Unit = { color ->
    Canvas(modifier = Modifier.size(14.dp)) {
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, size.height - 3f),
            size = Size(size.width, 3f),
            cornerRadius = CornerRadius(1f, 1f)
        )
    }
}

private val MaximizeIcon: @Composable (Color) -> Unit = { color ->
    Canvas(modifier = Modifier.size(14.dp)) {
        val inset = 1.5f
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(1.5f, 1.5f),
            style = Stroke(width = 1.8f)
        )
    }
}

private val RestoreIcon: @Composable (Color) -> Unit = { color ->
    Canvas(modifier = Modifier.size(14.dp)) {
        val inset = 1.5f
        // Back layer (larger square offset top-right)
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.35f, inset),
            size = Size(size.width * 0.6f, size.height * 0.6f),
            style = Stroke(width = 1.8f)
        )
        // Front layer (smaller square offset bottom-left)
        drawRect(
            color = color,
            topLeft = Offset(inset, size.height * 0.35f),
            size = Size(size.width * 0.6f, size.height * 0.6f),
            style = Stroke(width = 1.8f)
        )
    }
}

private val SettingsIcon: @Composable (Color) -> Unit = { color ->
    Canvas(modifier = Modifier.size(14.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension / 2f * 0.85f
        val innerR = outerR * 0.6f
        // Gear outer ring
        drawCircle(color = color, radius = outerR, style = Stroke(width = 1.6f))
        // Inner hole
        drawCircle(color = color, radius = innerR * 0.45f, style = Stroke(width = 1.6f))
        // 6 teeth
        val toothOuter = size.minDimension / 2f * 0.98f
        val toothInner = outerR * 0.82f
        for (i in 0 until 6) {
            val angle = (i * 60.0)
            val rad = angle * kotlin.math.PI / 180.0
            val cos = kotlin.math.cos(rad).toFloat()
            val sin = kotlin.math.sin(rad).toFloat()
            drawLine(color = color,
                start = Offset(cx + cos * toothInner, cy + sin * toothInner),
                end = Offset(cx + cos * toothOuter, cy + sin * toothOuter),
                strokeWidth = 2.8f)
        }
    }
}

private val QuickNoteIcon: @Composable (Color) -> Unit = { color ->
    Canvas(modifier = Modifier.size(14.dp)) {
        val inset = 1.5f
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(2.2f, 2.2f),
            style = Stroke(width = 1.6f)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.32f, size.height * 0.42f),
            end = Offset(size.width * 0.78f, size.height * 0.42f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.32f, size.height * 0.62f),
            end = Offset(size.width * 0.66f, size.height * 0.62f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.25f, size.height * 0.18f),
            end = Offset(size.width * 0.25f, size.height * 0.04f),
            strokeWidth = 1.7f
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.75f, size.height * 0.18f),
            end = Offset(size.width * 0.75f, size.height * 0.04f),
            strokeWidth = 1.7f
        )
    }
}

private val CloseIcon: @Composable (Color) -> Unit = { color ->
    Canvas(modifier = Modifier.size(14.dp)) {
        val strokeWidth = 1.8f
        val pad = 2.5f
        drawLine(
            color = color,
            start = Offset(pad, pad),
            end = Offset(size.width - pad, size.height - pad),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width - pad, pad),
            end = Offset(pad, size.height - pad),
            strokeWidth = strokeWidth
        )
    }
}

// ---- Button composables ----

@Composable
private fun WindowButton(
    label: String,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    WindowTooltip(label = label) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        Box(
            modifier = Modifier
                .size(AppSpacing.titleBarButtonSize)
                .hoverable(interactionSource)
                .then(
                    if (isHovered) Modifier.background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp)
                    ) else Modifier
                )
                .pointerInput(onClick) {
                    detectTapGestures(onTap = { onClick() })
                }
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center
        ) {
            icon(MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CloseButton(
    label: String,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    WindowTooltip(label = label) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        Box(
            modifier = Modifier
                .size(AppSpacing.titleBarButtonSize)
                .hoverable(interactionSource)
                .then(
                    if (isHovered) Modifier.background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp)
                    ) else Modifier
                )
                .pointerInput(onClick) {
                    detectTapGestures(onTap = { onClick() })
                }
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center
        ) {
            icon(
                if (isHovered) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WindowTooltip(
    label: String,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(modifier = Modifier.hoverable(interactionSource)) {
        content()
        if (isHovered) {
            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, 8),
                properties = PopupProperties(focusable = false)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

