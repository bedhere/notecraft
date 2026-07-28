package com.bedhere.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onClose: () -> Unit
) {
    val toggleMaximize = {
        windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
            WindowPlacement.Floating
        } else {
            WindowPlacement.Maximized
        }
    }

    with(windowScope) {
        WindowDraggableArea(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSpacing.titleBarHeight)
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(windowState.placement) {
                    detectTapGestures(onDoubleTap = { toggleMaximize() })
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpacing.titleBarHeight)
                    .padding(start = 18.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.appDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                currentNoteTitle?.let { title ->
                    Row(
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = " - ",
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
                } ?: Spacer(Modifier.weight(1f))
                TitleBarButton(label = "Minimize", glyph = "_", onClick = onMinimize)
                TitleBarButton(
                    label = if (windowState.placement == WindowPlacement.Maximized) "Restore" else "Maximize",
                    glyph = if (windowState.placement == WindowPlacement.Maximized) "><" else "[]",
                    onClick = toggleMaximize
                )
                TitleBarButton(
                    label = "Close",
                    glyph = "X",
                    onClick = onClose,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TitleBarButton(
    label: String,
    glyph: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp).semantics { contentDescription = label }
    ) {
        Text(text = glyph, color = tint, style = MaterialTheme.typography.bodyLarge)
    }
}
