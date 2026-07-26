package com.bedhere.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.notecraft.domain.model.Note
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.ui.markdown.MarkdownContent
import kotlinx.coroutines.launch
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
