package com.notecraft.data.repository

import com.notecraft.domain.model.Note
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.util.IdGenerator
import com.notecraft.util.NoteUtils

class InMemoryNoteRepository : NoteRepository {
    private val notes = mutableMapOf<String, Note>()

    override suspend fun listNotes(): List<NoteMetadata> =
        notes.values.toList().map { it.toMetadata() }

    override suspend fun getNote(id: String): Note =
        notes[id] ?: throw NoSuchElementException("Note not found: $id")

    override suspend fun createNote(request: SaveNoteRequest): Note {
        val id = IdGenerator.newId()
        val now = currentTime()
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
        notes[id] = note
        return note
    }

    override suspend fun updateNote(id: String, request: SaveNoteRequest): Note {
        val existing = notes[id] ?: throw NoSuchElementException("Note not found: $id")
        val now = currentTime()
        val updated = existing.copy(
            title = request.title,
            content = request.content,
            category = request.category,
            fileName = NoteUtils.buildFileName(id, request.title),
            updatedAt = now,
            wordCount = NoteUtils.countChars(request.content)
        )
        notes[id] = updated
        return updated
    }

    override suspend fun deleteNote(id: String) {
        if (notes.remove(id) == null) {
            throw NoSuchElementException("Note not found: $id")
        }
    }

    private fun currentTime(): Long = com.notecraft.platform.currentTimeMillis()

    override suspend fun listCategories(): List<String> =
        notes.values.map { it.category }.filter { it.isNotEmpty() }.distinct().sorted()

    override suspend fun createCategory(name: String) {
        // Categories are implicit; no action needed
    }

    override suspend fun renameCategory(oldName: String, newName: String) {
        val toUpdate = notes.values.filter { it.category == oldName }
        for (note in toUpdate) {
            notes[note.id] = note.copy(category = newName)
        }
    }

    override suspend fun deleteCategory(name: String) {
        val toUpdate = notes.values.filter { it.category == name }
        for (note in toUpdate) {
            notes[note.id] = note.copy(category = "")
        }
    }

}