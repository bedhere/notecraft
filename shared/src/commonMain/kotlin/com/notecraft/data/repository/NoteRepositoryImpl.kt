package com.notecraft.data.repository

import com.notecraft.domain.model.Note
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.data.storage.NoteStorage

class NoteRepositoryImpl(
    private val storage: NoteStorage
) : NoteRepository {

    override suspend fun listNotes(): List<NoteMetadata> =
        storage.loadMetadata()

    override suspend fun getNote(id: String): Note =
        storage.loadNote(id)

    override suspend fun createNote(request: SaveNoteRequest): Note =
        storage.insertNote(request)

    override suspend fun updateNote(id: String, request: SaveNoteRequest): Note =
        storage.updateNote(id, request)

    override suspend fun deleteNote(id: String) =
        storage.deleteNote(id)


    override suspend fun listCategories(): List<String> =
        storage.listCategories()

    override suspend fun createCategory(name: String) =
        storage.createCategory(name)

    override suspend fun renameCategory(oldName: String, newName: String) =
        storage.renameCategory(oldName, newName)

    override suspend fun deleteCategory(name: String) =
        storage.deleteCategory(name)

}