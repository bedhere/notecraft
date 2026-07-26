package com.notecraft.domain.repository

import com.notecraft.domain.model.Note
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.model.SaveNoteRequest

interface NoteRepository {
    suspend fun listNotes(): List<NoteMetadata>
    suspend fun getNote(id: String): Note
    suspend fun createNote(request: SaveNoteRequest): Note
    suspend fun updateNote(id: String, request: SaveNoteRequest): Note
    suspend fun deleteNote(id: String)


    suspend fun listCategories(): List<String>
    suspend fun createCategory(name: String)
    suspend fun renameCategory(oldName: String, newName: String)
    suspend fun deleteCategory(name: String)
}
