package com.bedhere.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.notecraft.data.repository.NoteRepositoryImpl
import com.notecraft.data.repository.SettingsRepositoryImpl
import com.notecraft.domain.model.AppConfig
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.importexport.JvmFileDialogService
import com.notecraft.storage.JvmNoteStorage
import com.notecraft.storage.JvmSettingsStorage
import com.notecraft.ui.screen.NoteApp
import com.notecraft.util.Strings
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val dataDir = remember { System.getProperty("user.home") + File.separator + ".notecraft" }
    File(dataDir).mkdirs()
    val noteRepo = remember(dataDir) { NoteRepositoryImpl(JvmNoteStorage(dataDir)) }
    val settingsRepo = remember(dataDir) { SettingsRepositoryImpl(JvmSettingsStorage(dataDir)) }
    val fileDialog = remember { JvmFileDialogService() }

    var appConfig by remember(settingsRepo) { mutableStateOf<AppConfig?>(null) }
    LaunchedEffect(settingsRepo) {
        appConfig = try { settingsRepo.getConfig() } catch (_: Exception) { null }
    }

    // Window state persistence
    val stateFile = remember(dataDir) { File(dataDir, "window_state.txt") }
    var savedX = 100; var savedY = 100; var savedW = 1477; var savedH = 952
    if (stateFile.exists()) {
        try {
            val parts = stateFile.readText().trim().split(",").map { it.toInt() }
            if (parts.size >= 4) { savedX = parts[0]; savedY = parts[1]; savedW = parts[2]; savedH = parts[3] }
        } catch (_: Exception) { }
    }

    val windowState = remember(savedX, savedY, savedW, savedH) { WindowState(
        position = WindowPosition(savedX.dp, savedY.dp),
        size = DpSize(savedW.dp, savedH.dp)
    ) }
    val windowVisible = remember { mutableStateOf(true) }
    val tileNoteIds = remember { mutableStateListOf<String>() }
    val quickNoteIds = remember { mutableStateListOf<String>() }
    var currentNoteTitle by remember { mutableStateOf<String?>(null) }
    var closeRequested by remember { mutableStateOf(false) }
    val settingsToggleSignal = remember { mutableStateOf(0) }
    val notesRefreshSignal = remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val launchQuickNote: () -> Unit = remember(noteRepo) {
        {
        scope.launch {
            try {
                val note = noteRepo.createNote(SaveNoteRequest(title = "", content = "", category = ""))
                if (note.id !in quickNoteIds) quickNoteIds.add(note.id)
                notesRefreshSignal.value++
            } catch (error: Exception) {
                System.err.println("Failed to open portable note: ${error.message}")
                error.printStackTrace()
            }
        }
    } }

    // Shortcut manager
    val shortcutMgr = remember {
        ShortcutManager(
        onQuickNote = launchQuickNote,
        onToggleVisibility = { windowVisible.value = !windowVisible.value }
    )
    }
    DisposableEffect(shortcutMgr, appConfig) {
        appConfig?.let(shortcutMgr::registerFromConfig)
        val dispatcher = java.awt.KeyEventDispatcher { event ->
            event.id == KeyEvent.KEY_PRESSED && shortcutMgr.handleKeyEvent(event.keyCode, event.modifiersEx)
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
            shortcutMgr.unregisterAll()
        }
    }

    // Tile windows
    for (noteId in tileNoteIds) {
        TileWindow(
            noteId = noteId,
            noteRepository = noteRepo,
            onClose = { tileNoteIds.remove(noteId) }
        )
    }
    for (noteId in quickNoteIds) {
        QuickNoteWindow(
            noteId = noteId,
            noteRepository = noteRepo,
            onNoteSaved = { notesRefreshSignal.value++ },
            onClose = { quickNoteIds.remove(noteId) }
        )
    }

    // Tray setup
    val tray = remember(dataDir) {
        TrayManager(
            dataDir = dataDir,
            onShowMain = { windowVisible.value = true },
            onQuit = { exitApplication() }
        )
    }
    DisposableEffect(tray) {
        tray.init()
        onDispose { tray.dispose() }
    }

    // File drop target setup
    DisposableEffect(noteRepo) {
        val dropHandler = FileDropHandler(noteRepo) { notesRefreshSignal.value++ }
        Thread {
            try {
                Thread.sleep(1000)
                for (w in java.awt.Window.getWindows()) {
                    if (w.isShowing && w is javax.swing.JFrame && w.title == Strings.appDisplayName) {
                        dropHandler.setup(w)
                        break
                    }
                }
            } catch (_: Exception) {}
        }.apply { isDaemon = true }.start()
        onDispose { dropHandler.dispose() }
    }

    val requestClose: () -> Unit = {
        if (!closeRequested) {
            closeRequested = true
            // Remove the window immediately; persistence and shutdown continue off the UI path.
            windowVisible.value = false
            scope.launch {
                val config = try { settingsRepo.getConfig() } catch (_: Exception) { null }
                if (config?.closeToTray == true) {
                    closeRequested = false
                } else {
                    val pos = windowState.position
                    val sz = windowState.size
                    val absX = if (pos is WindowPosition.Absolute) pos.x.value.toInt() else savedX
                    val absY = if (pos is WindowPosition.Absolute) pos.y.value.toInt() else savedY
                    try {
                        stateFile.writeText(
                            absX.toString() + "," + absY.toString() + "," +
                                sz.width.value.toInt().toString() + "," + sz.height.value.toInt()
                        )
                    } catch (_: Exception) {
                        // Closing should still succeed when the state file is not writable.
                    }
                    tray.dispose()
                    exitApplication()
                }
            }
        }
    }

    if (windowVisible.value) {
        Window(
            onCloseRequest = requestClose,
            title = Strings.appDisplayName,
            icon = painterResource("notecraft_logo.png"),
            undecorated = true,
            transparent = true,
            state = windowState
        ) {
            val windowScope = this
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                NoteApp(
                    noteRepository = noteRepo,
                    settingsRepository = settingsRepo,
                    fileDialogService = fileDialog,
                    onCurrentNoteTitleChange = { currentNoteTitle = it },
                    desktopTitleBar = {
                        DesktopTitleBar(
                            windowScope = windowScope,
                            windowState = windowState,
                            currentNoteTitle = currentNoteTitle,
                            onMinimize = { windowState.isMinimized = true },
                            onQuickNote = launchQuickNote,
                            onSettingsClick = { settingsToggleSignal.value++ },
                            onClose = requestClose
                        )
                    },
                    settingsToggleSignal = settingsToggleSignal,
                    externalNotesRefreshSignal = notesRefreshSignal,
                    onToggleTile = { noteId ->
                        if (noteId in tileNoteIds) tileNoteIds.remove(noteId)
                        else tileNoteIds.add(noteId)
                    },
                    isNoteTiled = { it in tileNoteIds }
                )
            }
        }
    }
}


