package com.bedhere.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.notecraft.data.repository.NoteRepositoryImpl
import com.notecraft.importexport.JsFileDialogService
import com.notecraft.storage.JsNoteStorage
import com.notecraft.ui.screen.NoteApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val noteRepo = NoteRepositoryImpl(JsNoteStorage())
     val settingsRepo = SettingsRepositoryImpl(JsSettingsStorage())
     val fileDialog = JsFileDialogService()

    ComposeViewport {
        NoteApp(
            noteRepository = noteRepo,
             settingsRepository = settingsRepo,
             fileDialogService = fileDialog
        )
    }
}
 import com.notecraft.data.repository.SettingsRepositoryImpl
 import com.notecraft.storage.JsSettingsStorage
