package com.notecraft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val fileName: String,
    val category: String = "",
    val createdAt: Long,       // epoch milliseconds
    val updatedAt: Long,       // epoch milliseconds
    val wordCount: Int,
    val content: String
) {
    fun toMetadata(): NoteMetadata = NoteMetadata(
        id = id,
        title = title,
        fileName = fileName,
        category = category,
        createdAt = createdAt,
        updatedAt = updatedAt,
        wordCount = wordCount,
        preview = content.take(80).replace(Regex("\\s+"), " ")
    )
}

@Serializable
data class NoteMetadata(
    val id: String,
    val title: String,
    val fileName: String,
    val category: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val wordCount: Int,
    val preview: String
)

@Serializable
data class SaveNoteRequest(
    val title: String,
    val content: String,
    val category: String = ""
)
