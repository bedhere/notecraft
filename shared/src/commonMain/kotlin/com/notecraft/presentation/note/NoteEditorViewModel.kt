package com.notecraft.presentation.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.util.Strings
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
    private val undoStack = ArrayDeque<EditSnapshot>()
    private val redoStack = ArrayDeque<EditSnapshot>()

    val isDirty: Boolean get() = _state.value.saveState == SaveState.Dirty

    fun loadNote(id: String) {
        loadEpoch++
        val currentEpoch = loadEpoch
        clearHistory()
        viewModelScope.launch {
            try {
                val note = noteRepository.getNote(id)
                if (currentEpoch != loadEpoch) return@launch
                _state.value = NoteEditorState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    updatedAt = note.updatedAt,
                    wordCount = note.wordCount
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saveState = SaveState.Error(e.message ?: Strings.loadFailed)
                )
            }
        }
    }

    fun clearEditor() {
        autoSaveJob?.cancel()
        loadEpoch++
        saveQueue?.cancel()
        clearHistory()
        _state.value = NoteEditorState()
    }

    fun updateTitle(title: String) {
        if (title == _state.value.title) return
        recordEdit()
        _state.value = _state.value.copy(
            title = title,
            saveState = SaveState.Dirty,
            canUndo = undoStack.isNotEmpty(),
            canRedo = false
        )
        scheduleAutoSave()
    }

    fun updateContent(content: String) {
        if (content == _state.value.content) return
        recordEdit()
        _state.value = _state.value.copy(
            content = content,
            wordCount = NoteUtils.countChars(content),
            saveState = SaveState.Dirty,
            canUndo = undoStack.isNotEmpty(),
            canRedo = false
        )
        scheduleAutoSave()
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(EditSnapshot(_state.value.title, _state.value.content))
        applySnapshot(previous)
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(EditSnapshot(_state.value.title, _state.value.content))
        applySnapshot(next)
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
                val updatedNote = noteRepository.updateNote(noteId, SaveNoteRequest(
                    title = s.title,
                    content = s.content,
                    category = ""
                ))
                _state.value = _state.value.copy(
                    updatedAt = updatedNote.updatedAt,
                    saveState = SaveState.Saved
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saveState = SaveState.Error(e.message ?: Strings.saveFailed)
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
                    val updatedNote = noteRepository.updateNote(noteId, SaveNoteRequest(s.title, s.content, ""))
                    _state.value = _state.value.copy(
                        updatedAt = updatedNote.updatedAt,
                        saveState = SaveState.Saved
                    )
                } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saveState = SaveState.Error(e.message ?: Strings.saveFailed)
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

    private fun recordEdit() {
        undoStack.addLast(EditSnapshot(_state.value.title, _state.value.content))
        redoStack.clear()
    }

    private fun applySnapshot(snapshot: EditSnapshot) {
        _state.value = _state.value.copy(
            title = snapshot.title,
            content = snapshot.content,
            wordCount = NoteUtils.countChars(snapshot.content),
            saveState = SaveState.Dirty,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
        scheduleAutoSave()
    }

    private fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }

    private data class EditSnapshot(
        val title: String,
        val content: String
    )
}
