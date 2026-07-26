package com.notecraft.presentation.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.util.NoteUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val noteRepository: NoteRepository,
    private val autoSaveDelayMs: Long = 800
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorState())
    val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    private var autoSaveJob: Job? = null
    private var saveQueue: Job? = null
    private var loadEpoch = 0

    val isDirty: Boolean get() = _state.value.saveState == SaveState.Dirty

    fun loadNote(id: String) {
        loadEpoch++
        val currentEpoch = loadEpoch
        viewModelScope.launch {
            try {
                val note = noteRepository.getNote(id)
                if (currentEpoch != loadEpoch) return@launch
                _state.value = NoteEditorState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    wordCount = note.wordCount
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saveState = SaveState.Error(e.message ?: "Failed to load note")
                )
            }
        }
    }

    fun clearEditor() {
        autoSaveJob?.cancel()
        loadEpoch++
        saveQueue?.cancel()
        _state.value = NoteEditorState()
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title, saveState = SaveState.Dirty)
        scheduleAutoSave()
    }

    fun updateContent(content: String) {
        _state.value = _state.value.copy(
            content = content,
            wordCount = NoteUtils.countChars(content),
            saveState = SaveState.Dirty
        )
        scheduleAutoSave()
    }

    fun save() {
        val s = _state.value
        val noteId = s.noteId ?: return
        val currentEpoch = loadEpoch
        saveQueue = viewModelScope.launch {
            saveQueue?.join()
            if (currentEpoch != loadEpoch) return@launch
            _state.value = s.copy(saveState = SaveState.Saving)
            try {
                noteRepository.updateNote(noteId, SaveNoteRequest(
                    title = s.title,
                    content = s.content,
                    category = ""
                ))
                _state.value = _state.value.copy(saveState = SaveState.Saved)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saveState = SaveState.Error(e.message ?: "Save failed")
                )
            }
        }
    }

    fun saveAndContinue(block: suspend () -> Unit) {
        val s = _state.value
        val noteId = s.noteId
        val currentEpoch = loadEpoch
        saveQueue = viewModelScope.launch {
            saveQueue?.join()
            if (s.saveState == SaveState.Dirty) {
                if (noteId == null || currentEpoch != loadEpoch) {
                    block()
                    return@launch
                }
                _state.value = s.copy(saveState = SaveState.Saving)
                try {
                    noteRepository.updateNote(noteId, SaveNoteRequest(s.title, s.content, ""))
                    _state.value = _state.value.copy(saveState = SaveState.Saved)
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        saveState = SaveState.Error(e.message ?: "Save failed before switching note")
                    )
                }
            }
            block()
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(autoSaveDelayMs)
            save()
        }
    }

    fun setViewMode(mode: com.notecraft.domain.model.ViewMode) {
        _state.value = _state.value.copy(viewMode = mode)
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
        saveQueue?.cancel()
    }
}
