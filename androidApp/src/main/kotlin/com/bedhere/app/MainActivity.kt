package com.bedhere.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.activity.compose.BackHandler
import com.notecraft.data.repository.NoteRepositoryImpl
import com.notecraft.data.repository.SettingsRepositoryImpl
import com.notecraft.importexport.AndroidFileDialogService
import com.notecraft.storage.AndroidNoteStorage
import com.notecraft.storage.AndroidSettingsStorage
import com.notecraft.ui.screen.MobileNoteApp

class MainActivity : ComponentActivity() {
    private var fileDialog: AndroidFileDialogService? = null
    private var backCallback: (() -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val dataDir = filesDir.absolutePath
        val noteRepo = NoteRepositoryImpl(AndroidNoteStorage(dataDir))
        val settingsRepo = SettingsRepositoryImpl(AndroidSettingsStorage(dataDir))
        fileDialog = AndroidFileDialogService(this)

        setContent {
            val configuration = LocalConfiguration.current
            val isWideScreen = configuration.screenWidthDp >= 600
            var backSignal by remember { mutableIntStateOf(0) }

            BackHandler(enabled = true) {
                backSignal++
            }

            MobileNoteApp(
                noteRepository = noteRepo,
                settingsRepository = settingsRepo,
                fileDialogService = fileDialog,
                isWideScreen = isWideScreen,
                backSignal = backSignal,
                onExitApp = { finish() }
            )
        }
    }

    @Deprecated("Use ActivityResultLauncher instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fileDialog?.handleResult(requestCode, resultCode, data)
    }
}
