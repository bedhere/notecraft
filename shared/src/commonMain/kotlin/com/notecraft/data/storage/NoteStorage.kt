package com.notecraft.data.storage

import com.notecraft.domain.model.Note
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.model.SaveNoteRequest

interface NoteStorage {
    suspend fun loadMetadata(): List<NoteMetadata>
    suspend fun loadNote(id: String): Note
    suspend fun insertNote(request: SaveNoteRequest): Note
    suspend fun updateNote(id: String, request: SaveNoteRequest): Note
    suspend fun deleteNote(id: String)
    suspend fun moveNoteCategory(id: String, newCategory: String): NoteMetadata
    suspend fun listCategories(): List<String>
    suspend fun createCategory(name: String)
    suspend fun renameCategory(oldName: String, newName: String)
    suspend fun deleteCategory(name: String)
}
