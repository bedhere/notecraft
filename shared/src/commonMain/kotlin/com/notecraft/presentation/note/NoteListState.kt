package com.notecraft.presentation.note

import com.notecraft.domain.model.NoteMetadata
import com.notecraft.util.NoteUtils

enum class SortMode {
    RECENTLY_UPDATED,
    TITLE
}

data class NoteListState(
    val notes: List<NoteMetadata> = emptyList(),
    val categoryGroups: List<NoteUtils.CategoryGroup> = emptyList(),
    val selectedNoteId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortMode: SortMode = SortMode.RECENTLY_UPDATED,
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val filteredNotes: List<NoteMetadata> = emptyList(),
    val filteredGroups: List<NoteUtils.CategoryGroup> = emptyList()
)
