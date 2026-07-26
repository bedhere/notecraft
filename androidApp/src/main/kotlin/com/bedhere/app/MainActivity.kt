package com.bedhere.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.notecraft.data.repository.NoteRepositoryImpl
import com.notecraft.data.repository.SettingsRepositoryImpl
import com.notecraft.importexport.AndroidFileDialogService
import com.notecraft.storage.AndroidNoteStorage
import com.notecraft.storage.AndroidSettingsStorage
import com.notecraft.ui.screen.NoteApp

class MainActivity : ComponentActivity() {
    private var fileDialog: AndroidFileDialogService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val noteRepo = NoteRepositoryImpl(AndroidNoteStorage(filesDir.absolutePath))
        val settingsRepo = SettingsRepositoryImpl(AndroidSettingsStorage(filesDir.absolutePath))
        fileDialog = AndroidFileDialogService(this)

        setContent {
            NoteApp(
                noteRepository = noteRepo,
                settingsRepository = settingsRepo,
                fileDialogService = fileDialog
            )
        }
    }

    @Deprecated("Use ActivityResultLauncher instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fileDialog?.handleResult(requestCode, resultCode, data)
    }
}
