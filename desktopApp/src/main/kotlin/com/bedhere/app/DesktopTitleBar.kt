package com.bedhere.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.sp
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
                        .padding(start = 18.dp, end = 160.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.appDisplayName,
                        modifier = Modifier.widthIn(min = 84.dp, max = 170.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    currentNoteTitle?.let { title ->
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
                }
            }
        }

        // Window control buttons - placed outside WindowDraggableArea
        // so they don't interfere with window dragging
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMinimize,
                modifier = Modifier
                    .size(46.dp)
                    .semantics { contentDescription = "Minimize" }
            ) {
                Text("_", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = toggleMaximize,
                modifier = Modifier
                    .size(46.dp)
                    .semantics {
                        contentDescription = if (windowState.placement == WindowPlacement.Maximized) "Restore" else "Maximize"
                    }
            ) {
                if (windowState.placement == WindowPlacement.Maximized) {
                    Text("\u2039\u203A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("\u25A1", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(46.dp)
                    .semantics { contentDescription = "Close" }
            ) {
                Text("\u2715", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
