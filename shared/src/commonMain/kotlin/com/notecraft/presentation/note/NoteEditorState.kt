package com.notecraft.presentation.note

import com.notecraft.domain.model.ViewMode

sealed interface SaveState {
    data object Idle : SaveState
    data object Dirty : SaveState
    data object Saving : SaveState
    data object Saved : SaveState
    data class Error(val message: String) : SaveState
}

data class NoteEditorState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val saveState: SaveState = SaveState.Idle,
    val wordCount: Int = 0,
    val viewMode: ViewMode = ViewMode.EDIT
)