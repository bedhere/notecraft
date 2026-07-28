package com.bedhere.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.notecraft.data.repository.NoteRepositoryImpl
import com.notecraft.data.repository.SettingsRepositoryImpl
import com.notecraft.importexport.JvmFileDialogService
import com.notecraft.storage.JvmNoteStorage
import com.notecraft.storage.JvmSettingsStorage
import com.notecraft.ui.screen.NoteApp
import com.notecraft.util.Strings
import java.io.File
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val dataDir = System.getProperty("user.home") + File.separator + ".notecraft"
    File(dataDir).mkdirs()
    val noteRepo = NoteRepositoryImpl(JvmNoteStorage(dataDir))
    val settingsRepo = SettingsRepositoryImpl(JvmSettingsStorage(dataDir))
    val fileDialog = JvmFileDialogService()

    val appConfig = runBlocking {
        try { settingsRepo.getConfig() } catch (_: Exception) { null }
    }

    // Window state persistence
    val stateFile = File(dataDir, "window_state.txt")
    var savedX = 100; var savedY = 100; var savedW = 1100; var savedH = 720
    if (stateFile.exists()) {
        try {
            val parts = stateFile.readText().trim().split(",").map { it.toInt() }
            if (parts.size >= 4) { savedX = parts[0]; savedY = parts[1]; savedW = parts[2]; savedH = parts[3] }
        } catch (_: Exception) { }
    }

    val windowState = WindowState(
        position = WindowPosition(savedX.dp, savedY.dp),
        size = DpSize(savedW.dp, savedH.dp)
    )
    val windowVisible = mutableStateOf(true)
    val tileNoteIds = mutableStateListOf<String>()
    var currentNoteTitle by remember { mutableStateOf<String?>(null) }

    // Shortcut manager
    val shortcutMgr = ShortcutManager(
        onQuickNote = { },
        onToggleVisibility = { windowVisible.value = !windowVisible.value }
    )
    if (appConfig != null) shortcutMgr.registerFromConfig(appConfig)

    // Tile windows
    for (noteId in tileNoteIds) {
        TileWindow(
            noteId = noteId,
            noteRepository = noteRepo,
            onClose = { tileNoteIds.remove(noteId) }
        )
    }

    // Tray setup
    lateinit var tray: TrayManager
    tray = TrayManager(
        dataDir = dataDir,
        onShowMain = { windowVisible.value = true },
        onQuickNote = { },
        onQuit = {
            tray.dispose()
            exitApplication()
        }
    )
    tray.init()

    // File drop target setup
    val dropHandler = FileDropHandler(noteRepo) { /* refresh handled by NoteApp */ }
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

    val requestClose = {
        val config = runBlocking {
            try { settingsRepo.getConfig() } catch (_: Exception) { null }
        }
        if (config?.closeToTray == true) {
            windowVisible.value = false
        } else {
            val pos = windowState.position
            val sz = windowState.size
            val absX = if (pos is WindowPosition.Absolute) pos.x.value.toInt() else savedX
            val absY = if (pos is WindowPosition.Absolute) pos.y.value.toInt() else savedY
            stateFile.writeText(absX.toString() + "," + absY.toString() + "," + sz.width.value.toInt().toString() + "," + sz.height.value.toInt().toString())
            tray.dispose()
            exitApplication()
        }
    }

    if (windowVisible.value) {
        Window(
            onCloseRequest = requestClose,
            title = Strings.appDisplayName,
            undecorated = true,
            transparent = true,
            state = windowState
        ) {
            val windowScope = this
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
                        onClose = requestClose
                    )
                },
                onToggleTile = { noteId ->
                    if (noteId in tileNoteIds) tileNoteIds.remove(noteId)
                    else tileNoteIds.add(noteId)
                }
            )
        }
    }
}
