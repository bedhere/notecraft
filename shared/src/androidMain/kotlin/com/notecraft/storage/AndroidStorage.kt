package com.notecraft.storage

import com.notecraft.data.storage.NoteStorage
import com.notecraft.data.storage.SettingsStorage
import com.notecraft.domain.model.*
import com.notecraft.platform.currentTimeMillis
import com.notecraft.util.IdGenerator
import com.notecraft.util.NoteUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

@Serializable
private data class NotesFile(
    val version: Int = 1,
    val notes: List<Note> = emptyList()
)

class AndroidNoteStorage(private val filesDir: String) : NoteStorage {
    private val notesFile: File get() = File(filesDir, "notes.json")

    override suspend fun loadMetadata(): List<NoteMetadata> = withContext(Dispatchers.IO) {
        if (!notesFile.exists()) return@withContext emptyList()
        json.decodeFromString<NotesFile>(notesFile.readText()).notes.map { it.toMetadata() }
    }

    override suspend fun loadNote(id: String): Note = withContext(Dispatchers.IO) {
        val all = readAll()
        all.firstOrNull { it.id == id } ?: throw NoSuchElementException("Note not found: $id")
    }

    override suspend fun insertNote(request: SaveNoteRequest): Note = withContext(Dispatchers.IO) {
        val id = IdGenerator.newId()
        val now = currentTimeMillis()
        val note = Note(id, request.title, NoteUtils.buildFileName(id, request.title),
            request.category, now, now, NoteUtils.countChars(request.content), request.content)
        val all = readAll().toMutableList()
        all.add(note)
        writeAll(all)
        note
    }

    override suspend fun updateNote(id: String, request: SaveNoteRequest): Note = withContext(Dispatchers.IO) {
        val all = readAll().toMutableList()
        val idx = all.indexOfFirst { it.id == id }
        if (idx < 0) throw NoSuchElementException("Note not found: $id")
        val now = currentTimeMillis()
        val updated = all[idx].copy(title = request.title, content = request.content,
            category = request.category, fileName = NoteUtils.buildFileName(id, request.title),
            updatedAt = now, wordCount = NoteUtils.countChars(request.content))
        all[idx] = updated
        writeAll(all)
        updated
    }

    override suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        val all = readAll().toMutableList()
        if (!all.removeAll { it.id == id }) throw NoSuchElementException("Note not found: $id")
        writeAll(all)
    }

    override suspend fun moveNoteCategory(id: String, newCategory: String): NoteMetadata = withContext(Dispatchers.IO) {
        val all = readAll().toMutableList()
        val idx = all.indexOfFirst { it.id == id }
        if (idx < 0) throw NoSuchElementException("Note not found: $id")
        val now = currentTimeMillis()
        val updated = all[idx].copy(category = newCategory, updatedAt = now)
        all[idx] = updated
        writeAll(all)
        updated.toMetadata()
    }

    override suspend fun listCategories(): List<String> = withContext(Dispatchers.IO) {
        readAll().map { it.category }.filter { it.isNotEmpty() }.distinct().sorted()
    }

    override suspend fun createCategory(name: String) {}
    override suspend fun renameCategory(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        writeAll(readAll().map { if (it.category == oldName) it.copy(category = newName) else it })
    }

    override suspend fun deleteCategory(name: String) = withContext(Dispatchers.IO) {
        writeAll(readAll().map { if (it.category == name) it.copy(category = "") else it })
    }

    private fun readAll(): List<Note> {
        if (!notesFile.exists()) return emptyList()
        return json.decodeFromString<NotesFile>(notesFile.readText()).notes
    }

    private fun writeAll(notes: List<Note>) {
        notesFile.parentFile?.mkdirs()
        notesFile.writeText(json.encodeToString(NotesFile(notes = notes)))
    }
}

class AndroidSettingsStorage(private val filesDir: String) : SettingsStorage {
    private val configFile: File get() = File(filesDir, "config.json")

    override suspend fun loadConfig(): AppConfig = withContext(Dispatchers.IO) {
        if (!configFile.exists()) return@withContext AppConfig()
        json.decodeFromString(configFile.readText())
    }

    override suspend fun saveConfig(config: AppConfig): AppConfig = withContext(Dispatchers.IO) {
        configFile.parentFile?.mkdirs()
        configFile.writeText(json.encodeToString(config))
        config
    }
}
