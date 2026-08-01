package com.bedhere.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.model.Note
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.ui.markdown.MarkdownContent
import com.notecraft.util.NoteUtils
import com.notecraft.util.Strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

data class TileInfo(
    val noteId: String,
    val visible: Boolean = true
)

@Composable
fun ApplicationScope.TileWindow(
    noteId: String,
    noteRepository: NoteRepository,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var note by remember { mutableStateOf<Note?>(null) }
    val stateFile = remember { File(System.getProperty("user.home") + "/.notecraft/tile_" + noteId + ".txt") }

    // Load saved state
    val (sx, sy, sw, sh) = remember {
        if (stateFile.exists()) {
            try {
                val p = stateFile.readText().trim().split(",").map { it.toInt() }
                if (p.size >= 4) listOf(p[0], p[1], p[2], p[3]) else listOf(200, 200, 300, 250)
            } catch (_: Exception) { listOf(200, 200, 300, 250) }
        } else listOf(200, 200, 300, 250)
    }

    // Load note
    LaunchedEffect(noteId) {
        scope.launch {
            try { note = noteRepository.getNote(noteId) } catch (_: Exception) {}
        }
    }

    val windowState = remember {
        WindowState(
            position = WindowPosition(sx.dp, sy.dp),
            size = DpSize(sw.dp, sh.dp)
        )
    }

    Window(
        onCloseRequest = {
            val pos = windowState.position
            val sz = windowState.size
            val absX = if (pos is WindowPosition.Absolute) pos.x.value.toInt() else sx
            val absY = if (pos is WindowPosition.Absolute) pos.y.value.toInt() else sy
            stateFile.writeText(absX.toString() + "," + absY.toString() + "," + sz.width.value.toInt().toString() + "," + sz.height.value.toInt().toString())
            onClose()
        },
        title = "Tile",
        state = windowState,
        alwaysOnTop = true
    ) {
        TileContent(note = note, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ApplicationScope.QuickNoteWindow(
    noteId: String,
    noteRepository: NoteRepository,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loaded by remember(noteId) { mutableStateOf(false) }
    var title by remember(noteId) { mutableStateOf("") }
    var content by remember(noteId) { mutableStateOf("") }
    var pinned by remember(noteId) { mutableStateOf(true) }
    val stateFile = remember { File(System.getProperty("user.home") + "/.notecraft/quick_note_" + noteId + ".txt") }

    val (sx, sy, sw, sh) = remember {
        if (stateFile.exists()) {
            try {
                val p = stateFile.readText().trim().split(",").map { it.toInt() }
                if (p.size >= 4) listOf(p[0], p[1], p[2], p[3]) else listOf(260, 160, 736, 570)
            } catch (_: Exception) { listOf(260, 160, 736, 570) }
        } else listOf(260, 160, 736, 570)
    }

    LaunchedEffect(noteId) {
        scope.launch {
            try {
                val note = noteRepository.getNote(noteId)
                title = note.title
                content = note.content
                loaded = true
            } catch (_: Exception) {
                loaded = true
            }
        }
    }

    LaunchedEffect(noteId, title, content, loaded) {
        if (!loaded) return@LaunchedEffect
        delay(650)
        try {
            noteRepository.updateNote(
                noteId,
                SaveNoteRequest(
                    title = title,
                    content = content,
                    category = ""
                )
            )
        } catch (_: Exception) {
        }
    }

    val windowState = remember {
        WindowState(
            position = WindowPosition(sx.dp, sy.dp),
            size = DpSize(sw.dp, sh.dp)
        )
    }

    val saveNow: () -> Unit = {
        if (loaded) {
            scope.launch {
                try {
                    noteRepository.updateNote(noteId, SaveNoteRequest(title, content, ""))
                } catch (_: Exception) {
                }
            }
        }
    }

    val persistAndClose = {
        val pos = windowState.position
        val sz = windowState.size
        val absX = if (pos is WindowPosition.Absolute) pos.x.value.toInt() else sx
        val absY = if (pos is WindowPosition.Absolute) pos.y.value.toInt() else sy
        stateFile.writeText(absX.toString() + "," + absY.toString() + "," + sz.width.value.toInt().toString() + "," + sz.height.value.toInt().toString())
        if (loaded) {
            runBlocking {
                try {
                    noteRepository.updateNote(noteId, SaveNoteRequest(title, content, ""))
                } catch (_: Exception) {
                }
            }
        }
        onClose()
    }

    Window(
        onCloseRequest = persistAndClose,
        title = Strings.quickNote,
        state = windowState,
        undecorated = true,
        transparent = true,
        alwaysOnTop = pinned
    ) {
        val windowScope = this
        QuickNoteContent(
            windowScope = windowScope,
            loaded = loaded,
            title = title,
            content = content,
            pinned = pinned,
            onTitleChange = { title = it },
            onContentChange = { content = it },
            onPinToggle = { pinned = !pinned },
            onClear = {
                title = ""
                content = ""
            },
            onSave = saveNow,
            onClose = persistAndClose,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun QuickNoteContent(
    windowScope: WindowScope,
    loaded: Boolean,
    title: String,
    content: String,
    pinned: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPinToggle: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        if (!loaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    with(windowScope) {
                        WindowDraggableArea(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 92.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(28.dp)
                            ) {
                                Text(
                                    text = "新建",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .height(34.dp)
                                        .padding(top = 7.dp)
                                )
                                Text(
                                    text = "打开",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .padding(top = 7.dp)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (pinned) "●" else "○",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { onPinToggle() }
                        )
                        Text(
                            text = "×",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.clickable { onClose() }
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))

                Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp, vertical = 18.dp)) {
                    BasicTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (title.isBlank()) {
                                    Text(
                                        text = "标题(可选)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.54f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    Spacer(Modifier.height(14.dp))
                    BasicTextField(
                        value = content,
                        onValueChange = onContentChange,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        decorationBox = { inner ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (content.isBlank()) {
                                    Text(
                                        text = "写点什么……",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
                Row(
                    modifier = Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${NoteUtils.countChars(content)} 字 · 空",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onClear) {
                        Text("清空", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F6B49),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp)
                    ) {
                        Text("保存", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TileContent(note: Note?, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface) {
        if (note == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = note.title.ifEmpty { "untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                MarkdownContent(
                    content = note.content,
                    fontSize = 12,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
