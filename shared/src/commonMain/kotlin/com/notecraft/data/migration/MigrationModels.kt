package com.notecraft.data.migration

import kotlinx.serialization.Serializable

@Serializable
data class LegacyMetadataFile(
    val notes: List<LegacyNoteMetadata> = emptyList()
)

@Serializable
data class LegacyNoteMetadata(
    val id: String = "",
    val title: String = "",
    val fileName: String = "",
    val category: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val wordCount: Int = 0,
    val preview: String = ""
)

data class MigrationResult(
    val totalNotes: Int = 0,
    val importedNotes: Int = 0,
    val skippedNotes: Int = 0,
    val errors: List<String> = emptyList()
)
