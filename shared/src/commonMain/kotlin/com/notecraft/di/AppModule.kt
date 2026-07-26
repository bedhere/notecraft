package com.notecraft.di

import com.notecraft.data.storage.NoteStorage
import com.notecraft.data.storage.SettingsStorage

object AppModule {
    private var noteStorage: NoteStorage? = null
    private var settingsStorage: SettingsStorage? = null

    fun init(
        noteStorage: NoteStorage,
        settingsStorage: SettingsStorage
    ) {
        this.noteStorage = noteStorage
        this.settingsStorage = settingsStorage
    }

    fun getNoteStorage(): NoteStorage =
        noteStorage ?: throw IllegalStateException("AppModule not initialized")

    fun getSettingsStorage(): SettingsStorage =
        settingsStorage ?: throw IllegalStateException("AppModule not initialized")
}
