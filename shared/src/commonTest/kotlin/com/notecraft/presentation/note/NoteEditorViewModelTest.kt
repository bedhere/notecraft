package com.notecraft.presentation.note

import com.notecraft.data.repository.InMemoryNoteRepository
import com.notecraft.domain.model.SaveNoteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {
    @Test
    fun `undo and redo restore edited content`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = InMemoryNoteRepository()
            val note = repository.createNote(SaveNoteRequest(title = "Note", content = "Initial"))
            val viewModel = NoteEditorViewModel(repository, autoSaveDelayMs = 1_000)

            viewModel.loadNote(note.id)
            advanceUntilIdle()
            viewModel.updateContent("First edit")
            viewModel.updateContent("Second edit")

            assertTrue(viewModel.state.value.canUndo)
            assertFalse(viewModel.state.value.canRedo)

            viewModel.undo()
            assertEquals("First edit", viewModel.state.value.content)
            assertTrue(viewModel.state.value.canRedo)

            viewModel.redo()
            assertEquals("Second edit", viewModel.state.value.content)
            assertFalse(viewModel.state.value.canRedo)
            advanceUntilIdle()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
