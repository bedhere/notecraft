package com.notecraft.storage

import com.notecraft.data.storage.NoteStorage
import com.notecraft.data.storage.SettingsStorage
import com.notecraft.domain.model.AppConfig
import com.notecraft.domain.model.Note
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.platform.currentTimeMillis
import com.notecraft.util.IdGenerator
import com.notecraft.util.NoteUtils
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
private const val NOTES_KEY = "notecraft.notes"
private const val CONFIG_KEY = "notecraft.config"

@Serializable
private data class NotesFile(
    val version: Int = 1,
    val notes: List<Note> = emptyList()
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun localStorageGet(key: String): String? = js("localStorage.getItem(key)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun localStorageSet(key: String, value: String): Unit = js("localStorage.setItem(key, value)")

class JsNoteStorage : NoteStorage {
    private fun readNotes(): List<Note> {
        val raw = localStorageGet(NOTES_KEY) ?: return emptyList()
        return json.decodeFromString<NotesFile>(raw).notes
    }

    private fun writeNotes(notes: List<Note>) {
        localStorageSet(NOTES_KEY, json.encodeToString(NotesFile(notes = notes)))
    }

    override suspend fun loadMetadata(): List<NoteMetadata> =
        readNotes().map { it.toMetadata() }

    override suspend fun loadNote(id: String): Note {
        return readNotes().firstOrNull { it.id == id }
            ?: throw NoSuchElementException("Note not found: $id")
    }

    override suspend fun insertNote(request: SaveNoteRequest): Note {
        val id = IdGenerator.newId()
        val now = currentTimeMillis()
        val note = Note(
            id = id,
            title = request.title,
            fileName = NoteUtils.buildFileName(id, request.title),
            category = request.category,
            createdAt = now,
            updatedAt = now,
            wordCount = NoteUtils.countChars(request.content),
            content = request.content
        )
        val all = readNotes().toMutableList()
        all.add(note)
        writeNotes(all)
        return note
    }

    override suspend fun updateNote(id: String, request: SaveNoteRequest): Note {
        val all = readNotes().toMutableList()
        val idx = all.indexOfFirst { it.id == id }
        if (idx < 0) throw NoSuchElementException("Note not found: $id")
        val now = currentTimeMillis()
        val updated = all[idx].copy(
            title = request.title,
            content = request.content,
            category = request.category,
            fileName = NoteUtils.buildFileName(id, request.title),
            updatedAt = now,
            wordCount = NoteUtils.countChars(request.content)
        )
        all[idx] = updated
        writeNotes(all)
        return updated
    }

    override suspend fun deleteNote(id: String) {
        val all = readNotes().toMutableList()
        if (!all.removeAll { it.id == id }) throw NoSuchElementException("Note not found: $id")
        writeNotes(all)
    }

    override suspend fun moveNoteCategory(id: String, newCategory: String): NoteMetadata {
        val all = readNotes().toMutableList()
        val idx = all.indexOfFirst { it.id == id }
        if (idx < 0) throw NoSuchElementException("Note not found: $id")
        val now = currentTimeMillis()
        val updated = all[idx].copy(category = newCategory, updatedAt = now)
        all[idx] = updated
        writeNotes(all)
        return updated.toMetadata()
    }

    override suspend fun listCategories(): List<String> =
        readNotes().map { it.category }.filter { it.isNotEmpty() }.distinct().sorted()

    override suspend fun createCategory(name: String) {}

    override suspend fun renameCategory(oldName: String, newName: String) {
        val all = readNotes().map { if (it.category == oldName) it.copy(category = newName) else it }
        writeNotes(all)
    }

    override suspend fun deleteCategory(name: String) {
        val all = readNotes().map { if (it.category == name) it.copy(category = "") else it }
        writeNotes(all)
    }
}

class JsSettingsStorage : SettingsStorage {
    override suspend fun loadConfig(): AppConfig {
        val raw = localStorageGet(CONFIG_KEY) ?: return AppConfig()
        return json.decodeFromString(raw)
    }

    override suspend fun saveConfig(config: AppConfig): AppConfig {
        localStorageSet(CONFIG_KEY, json.encodeToString(config))
        return config
    }
}
