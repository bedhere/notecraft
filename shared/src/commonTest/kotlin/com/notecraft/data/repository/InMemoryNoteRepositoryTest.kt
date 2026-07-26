package com.notecraft.data.repository

import com.notecraft.domain.model.SaveNoteRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class InMemoryNoteRepositoryTest {

    @Test
    fun `create and list notes`() = runTest {
        val repo = InMemoryNoteRepository()
        val note = repo.createNote(SaveNoteRequest("Test", "Hello World", ""))
        assertEquals("Test", note.title)
        assertEquals("Hello World", note.content)

        val notes = repo.listNotes()
        assertEquals(1, notes.size)
        assertEquals("Test", notes[0].title)
    }

    @Test
    fun `create note with empty title`() = runTest {
        val repo = InMemoryNoteRepository()
        val note = repo.createNote(SaveNoteRequest("", "content only", ""))
        assertNotNull(note.id)
        assertEquals("", note.title)
    }

    @Test
    fun `read note by id`() = runTest {
        val repo = InMemoryNoteRepository()
        val created = repo.createNote(SaveNoteRequest("Read Test", "content", ""))
        val read = repo.getNote(created.id)
        assertEquals(created.id, read.id)
        assertEquals("Read Test", read.title)
    }

    @Test
    fun `read non-existent note throws`() = runTest {
        val repo = InMemoryNoteRepository()
        assertFailsWith<NoSuchElementException> { repo.getNote("nonexistent") }
    }

    @Test
    fun `update existing note`() = runTest {
        val repo = InMemoryNoteRepository()
        val created = repo.createNote(SaveNoteRequest("Original", "content", ""))
        val updated = repo.updateNote(created.id, SaveNoteRequest("Updated", "new content", ""))
        assertEquals("Updated", updated.title)
        assertEquals("new content", updated.content)
    }

    @Test
    fun `update non-existent note throws`() = runTest {
        val repo = InMemoryNoteRepository()
        assertFailsWith<NoSuchElementException> {
            repo.updateNote("nonexistent", SaveNoteRequest("", "", ""))
        }
    }

    @Test
    fun `delete existing note`() = runTest {
        val repo = InMemoryNoteRepository()
        val created = repo.createNote(SaveNoteRequest("Delete Me", "content", ""))
        repo.deleteNote(created.id)
        assertEquals(0, repo.listNotes().size)
    }

    @Test
    fun `delete non-existent note throws`() = runTest {
        val repo = InMemoryNoteRepository()
        assertFailsWith<NoSuchElementException> { repo.deleteNote("nonexistent") }
    }

    @Test
    fun `empty repository returns empty list`() = runTest {
        val repo = InMemoryNoteRepository()
        assertEquals(0, repo.listNotes().size)
    }

    @Test
    fun `multiple creates generate unique IDs`() = runTest {
        val repo = InMemoryNoteRepository()
        val ids = (1..5).map { repo.createNote(SaveNoteRequest("N$it", "", "")).id }
        assertEquals(5, ids.toSet().size)
    }
}
