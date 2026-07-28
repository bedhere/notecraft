package com.notecraft.presentation.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notecraft.domain.model.NoteMetadata
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.util.NoteUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteListViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState())
    val state: StateFlow<NoteListState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                refreshNotes(preferredSelectedId = _state.value.selectedNoteId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectNote(id: String) {
        _state.value = _state.value.copy(selectedNoteId = id)
    }

    fun createNote() {
        viewModelScope.launch {
            try {
                val note = noteRepository.createNote(
                    SaveNoteRequest(title = "", content = "", category = "")
                )
                refreshNotes(preferredSelectedId = note.id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                val fallback = NoteSelectionPolicy.afterDelete(id, _state.value.filteredNotes)
                noteRepository.deleteNote(id)
                val preferred = if (_state.value.selectedNoteId == id) fallback else _state.value.selectedNoteId
                refreshNotes(preferredSelectedId = preferred)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun setSearchQuery(query: String) {
        if (query == _state.value.searchQuery) return
        val s = _state.value
        _state.value = s.copy(searchQuery = query)
        applyFilters(s.notes, s.categories, s.sortMode, query, s.selectedNoteId)
    }

    fun setSortMode(mode: SortMode) {
        val s = _state.value
        _state.value = s.copy(sortMode = mode)
        applyFilters(s.notes, s.categories, mode, s.searchQuery, s.selectedNoteId)
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            try {
                noteRepository.createCategory(name)
                loadAll()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            try {
                noteRepository.renameCategory(oldName, newName)
                loadAll()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            try {
                noteRepository.deleteCategory(name)
                loadAll()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    private fun applyFilters(
        notes: List<NoteMetadata>,
        categories: List<String>,
        sortMode: SortMode,
        query: String,
        preferredSelectedId: String? = _state.value.selectedNoteId
    ) {
        val filtered = if (query.isBlank()) notes
            else NoteUtils.filterNotes(notes, query)
        val sorted = when (sortMode) {
            SortMode.RECENTLY_UPDATED -> filtered
            SortMode.TITLE -> filtered.sortedBy { it.title.lowercase() }
        }
        val groups = NoteUtils.groupByCategory(sorted, categories)
        val selectedNoteId = NoteSelectionPolicy.resolve(preferredSelectedId, sorted)
        _state.value = _state.value.copy(
            notes = notes,
            categories = categories,
            filteredNotes = sorted,
            filteredGroups = groups,
            isLoading = false,
            searchQuery = query,
            selectedNoteId = selectedNoteId
        )
    }

    private suspend fun refreshNotes(preferredSelectedId: String?) {
        val notes = noteRepository.listNotes()
        val cats = noteRepository.listCategories()
        applyFilters(notes, cats, _state.value.sortMode, _state.value.searchQuery, preferredSelectedId)
    }
}

internal object NoteSelectionPolicy {
    fun resolve(preferredSelectedId: String?, notes: List<NoteMetadata>): String? =
        preferredSelectedId?.takeIf { id -> notes.any { it.id == id } }
            ?: notes.firstOrNull()?.id

    fun afterDelete(deletedId: String, notes: List<NoteMetadata>): String? {
        val deletedIndex = notes.indexOfFirst { it.id == deletedId }
        if (deletedIndex == -1) return resolve(null, notes)
        return notes.getOrNull(deletedIndex + 1)?.id
            ?: notes.getOrNull(deletedIndex - 1)?.id
    }
}
