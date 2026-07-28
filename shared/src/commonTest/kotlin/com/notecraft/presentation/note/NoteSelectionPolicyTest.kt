package com.notecraft.presentation.note

import com.notecraft.domain.model.NoteMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NoteSelectionPolicyTest {
    @Test
    fun `resolve keeps valid selection`() {
        val notes = listOf(note("first"), note("second"))

        assertEquals("second", NoteSelectionPolicy.resolve("second", notes))
    }

    @Test
    fun `resolve selects first note when preferred selection is missing`() {
        val notes = listOf(note("first"), note("second"))

        assertEquals("first", NoteSelectionPolicy.resolve("missing", notes))
    }

    @Test
    fun `resolve clears selection for empty note list`() {
        assertNull(NoteSelectionPolicy.resolve("missing", emptyList()))
    }

    @Test
    fun `after delete selects next note first`() {
        val notes = listOf(note("first"), note("second"), note("third"))

        assertEquals("third", NoteSelectionPolicy.afterDelete("second", notes))
    }

    @Test
    fun `after delete selects previous note when deleted note is last`() {
        val notes = listOf(note("first"), note("second"))

        assertEquals("first", NoteSelectionPolicy.afterDelete("second", notes))
    }

    @Test
    fun `after delete clears selection when last remaining note is deleted`() {
        val notes = listOf(note("only"))

        assertNull(NoteSelectionPolicy.afterDelete("only", notes))
    }

    private fun note(id: String) = NoteMetadata(
        id = id,
        title = id,
        fileName = "$id.md",
        createdAt = 1L,
        updatedAt = 1L,
        wordCount = 0,
        preview = ""
    )
}
