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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.model.Note
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.ui.markdown.MarkdownContent
import com.notecraft.ui.theme.NotecraftTheme
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
        icon = painterResource("notecraft_logo.png"),
        state = windowState,
        alwaysOnTop = true
    ) {
        NotecraftTheme {
            TileContent(note = note, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun ApplicationScope.QuickNoteWindow(
    noteId: String,
    noteRepository: NoteRepository,
    onNoteSaved: () -> Unit = {},
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var activeNoteId by remember(noteId) { mutableStateOf(noteId) }
    var loaded by remember(noteId) { mutableStateOf(false) }
    var title by remember(noteId) { mutableStateOf("") }
    var content by remember(noteId) { mutableStateOf("") }
    var pinned by remember(noteId) { mutableStateOf(false) }
    var showOpenNotes by remember(noteId) { mutableStateOf(false) }
    var openNotes by remember(noteId) { mutableStateOf<List<NoteMetadata>>(emptyList()) }
    val stateFile = remember { File(System.getProperty("user.home") + "/.notecraft/quick_note_" + noteId + ".txt") }

    val (sx, sy, sw, sh) = remember {
        if (stateFile.exists()) {
            try {
                val p = stateFile.readText().trim().split(",").map { it.toInt() }
                if (p.size >= 4) listOf(p[0], p[1], p[2], p[3]) else listOf(320, 180, 420, 360)
            } catch (_: Exception) { listOf(320, 180, 420, 360) }
        } else listOf(320, 180, 420, 360)
    }

    LaunchedEffect(activeNoteId) {
        loaded = false
        title = ""
        content = ""
        try {
            val note = noteRepository.getNote(activeNoteId)
            title = note.title
            content = note.content
        } catch (_: Exception) {
        } finally {
            loaded = true
        }
    }

    LaunchedEffect(activeNoteId, title, content, loaded) {
        if (!loaded) return@LaunchedEffect
        delay(650)
        try {
            noteRepository.updateNote(
                activeNoteId,
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
                    noteRepository.updateNote(activeNoteId, SaveNoteRequest(title, content, ""))
                    onNoteSaved()
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
                    noteRepository.updateNote(activeNoteId, SaveNoteRequest(title, content, ""))
                    onNoteSaved()
                } catch (_: Exception) {
                }
            }
        }
        onClose()
    }

    Window(
        onCloseRequest = persistAndClose,
        title = Strings.quickNote,
        icon = painterResource("notecraft_logo.png"),
        state = windowState,
        undecorated = true,
        transparent = true,
        alwaysOnTop = pinned
    ) {
        val windowScope = this
        NotecraftTheme {
            QuickNoteContent(
                windowScope = windowScope,
                loaded = loaded,
                title = title,
                content = content,
                pinned = pinned,
                onTitleChange = { title = it },
                onContentChange = { content = it },
                onPinToggle = { pinned = !pinned },
                onNew = {
                    scope.launch {
                        try {
                            noteRepository.updateNote(activeNoteId, SaveNoteRequest(title, content, ""))
                            val note = noteRepository.createNote(SaveNoteRequest(title = "", content = "", category = ""))
                            activeNoteId = note.id
                            onNoteSaved()
                        } catch (_: Exception) {
                        }
                    }
                },
                openNotes = openNotes,
                showOpenNotes = showOpenNotes,
                onOpenClick = {
                    scope.launch {
                        openNotes = try {
                            noteRepository.listNotes().filter { it.id != activeNoteId }
                        } catch (_: Exception) {
                            emptyList()
                        }
                        showOpenNotes = true
                    }
                },
                onOpenDismiss = { showOpenNotes = false },
                onOpenNote = { selectedNoteId ->
                    showOpenNotes = false
                    activeNoteId = selectedNoteId
                },
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
    onNew: () -> Unit,
    openNotes: List<NoteMetadata>,
    showOpenNotes: Boolean,
    onOpenClick: () -> Unit,
    onOpenDismiss: () -> Unit,
    onOpenNote: (String) -> Unit,
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
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(start = 20.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickNoteTab(text = "新建", selected = true, onClick = onNew)
                    Box {
                        QuickNoteTab(text = "打开", selected = false, onClick = onOpenClick)
                        DropdownMenu(
                            expanded = showOpenNotes,
                            onDismissRequest = onOpenDismiss,
                            modifier = Modifier.widthIn(min = 220.dp, max = 300.dp)
                        ) {
                            if (openNotes.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("没有可打开的笔记") },
                                    onClick = onOpenDismiss,
                                    enabled = false
                                )
                            } else {
                                openNotes.forEach { note ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = note.title.ifBlank { "无标题笔记" },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        onClick = { onOpenNote(note.id) }
                                    )
                                }
                            }
                        }
                    }
                    with(windowScope) {
                        WindowDraggableArea(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                    QuickNoteHeaderButton(
                        label = if (pinned) "取消置顶" else "置顶",
                        text = "⌖",
                        active = pinned,
                        onClick = onPinToggle
                    )
                    QuickNoteHeaderButton(
                        label = "关闭",
                        text = "×",
                        onClick = onClose
                    )
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
                    Spacer(Modifier.height(12.dp))
                    BasicTextField(
                        value = content,
                        onValueChange = onContentChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
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
private fun QuickNoteTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(60.dp)
            .width(70.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF2F6B49) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(30.dp)
                    .height(2.dp)
                    .background(Color(0xFF2F6B49), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
private fun QuickNoteHeaderButton(
    label: String,
    text: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (active) Color(0xFF2F6B49) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
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
