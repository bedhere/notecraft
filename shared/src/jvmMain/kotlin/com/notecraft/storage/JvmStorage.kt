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

class JvmNoteStorage(private val dataDir: String) : NoteStorage {
    private val notesFile: File get() = File(dataDir, "notes.json")
    private val jsonEngine = json

    override suspend fun loadMetadata(): List<NoteMetadata> = withContext(Dispatchers.IO) {
        if (!notesFile.exists()) return@withContext emptyList()
        val file = jsonEngine.decodeFromString<NotesFile>(notesFile.readText())
        file.notes.map { it.toMetadata() }
    }

    override suspend fun loadNote(id: String): Note = withContext(Dispatchers.IO) {
        val all = loadAllNotes()
        all.firstOrNull { it.id == id } ?: throw NoSuchElementException("Note not found: $id")
    }

    override suspend fun insertNote(request: SaveNoteRequest): Note = withContext(Dispatchers.IO) {
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
        val all = loadAllNotes().toMutableList()
        all.add(note)
        writeAllNotes(all)
        note
    }

    override suspend fun updateNote(id: String, request: SaveNoteRequest): Note = withContext(Dispatchers.IO) {
        val all = loadAllNotes().toMutableList()
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
        writeAllNotes(all)
        updated
    }

    override suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        val all = loadAllNotes().toMutableList()
        if (all.removeAll { it.id == id }) {
            writeAllNotes(all)
        } else {
            throw NoSuchElementException("Note not found: $id")
        }
    }

    override suspend fun moveNoteCategory(id: String, newCategory: String): NoteMetadata = withContext(Dispatchers.IO) {
        val all = loadAllNotes().toMutableList()
        val idx = all.indexOfFirst { it.id == id }
        if (idx < 0) throw NoSuchElementException("Note not found: $id")
        val now = currentTimeMillis()
        val updated = all[idx].copy(category = newCategory, updatedAt = now)
        all[idx] = updated
        writeAllNotes(all)
        updated.toMetadata()
    }

    override suspend fun listCategories(): List<String> = withContext(Dispatchers.IO) {
        loadAllNotes().map { it.category }.filter { it.isNotEmpty() }.distinct().sorted()
    }

    override suspend fun createCategory(name: String) = withContext(Dispatchers.IO) {
        // Categories are implicit via notes; no separate storage needed
    }

    override suspend fun renameCategory(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        val all = loadAllNotes().map { note ->
            if (note.category == oldName) note.copy(category = newName) else note
        }
        writeAllNotes(all)
    }

    override suspend fun deleteCategory(name: String) = withContext(Dispatchers.IO) {
        val all = loadAllNotes().map { note ->
            if (note.category == name) note.copy(category = "") else note
        }
        writeAllNotes(all)
    }

    private fun loadAllNotes(): List<Note> {
        if (!notesFile.exists()) return emptyList()
        return jsonEngine.decodeFromString<NotesFile>(notesFile.readText()).notes
    }

    private fun writeAllNotes(notes: List<Note>) {
        notesFile.parentFile?.mkdirs()
        notesFile.writeText(jsonEngine.encodeToString(NotesFile(notes = notes)))
    }
}

class JvmSettingsStorage(private val dataDir: String) : SettingsStorage {
    private val configFile: File get() = File(dataDir, "config.json")

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
