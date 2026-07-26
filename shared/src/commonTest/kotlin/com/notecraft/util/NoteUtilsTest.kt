package com.notecraft.util

import com.notecraft.domain.model.NoteMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoteUtilsTest {

    @Test
    fun `buildPreview returns first 80 chars`() {
        val content = "Hello World " + "A".repeat(100)
        val preview = NoteUtils.buildPreview(content)
        assertTrue(preview.length <= 80)
    }

    @Test
    fun `countChars counts non-whitespace characters`() {
        assertEquals(5, NoteUtils.countChars("Hello"))
        assertEquals(5, NoteUtils.countChars("He llo"))
        assertEquals(0, NoteUtils.countChars("   "))
        assertEquals(0, NoteUtils.countChars(""))
    }

    @Test
    fun `buildFileName produces safe file names`() {
        val name = NoteUtils.buildFileName("id123", "My Note")
        assertEquals("id123_My_Note.md", name)
    }

    @Test
    fun `buildFileName sanitizes special characters`() {
        val name = NoteUtils.buildFileName("id1", "a<b>c:d")
        assertFalse(name.contains("<"))
        assertFalse(name.contains(">"))
        assertFalse(name.contains(":"))
        assertTrue(name.endsWith(".md"))
    }

    @Test
    fun `getDisplayTitle returns title if not empty`() {
        val note = NoteMetadata("1", "My Title", "f.md", "", 0L, 0L, 0, "preview")
        assertEquals("My Title", NoteUtils.getDisplayTitle(note))
    }

    @Test
    fun `getDisplayTitle falls back to preview`() {
        val note = NoteMetadata("1", "", "f.md", "", 0L, 0L, 0, "some preview text")
        assertEquals("some preview text", NoteUtils.getDisplayTitle(note))
    }

    @Test
    fun `getDisplayTitle returns untitled for empty`() {
        val note = NoteMetadata("1", "", "f.md", "", 0L, 0L, 0, "")
        assertEquals("untitled", NoteUtils.getDisplayTitle(note))
    }

    @Test
    fun `filterNotes matches title`() {
        val notes = listOf(
            NoteMetadata("1", "Meeting Notes", "1.md", "", 0L, 0L, 0, ""),
            NoteMetadata("2", "Shopping List", "2.md", "", 0L, 0L, 0, "")
        )
        val result = NoteUtils.filterNotes(notes, "meeting")
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }

    @Test
    fun `filterNotes is case-insensitive`() {
        val notes = listOf(NoteMetadata("1", "Meeting", "1.md", "", 0L, 0L, 0, ""))
        assertEquals(1, NoteUtils.filterNotes(notes, "MEETING").size)
        assertEquals(1, NoteUtils.filterNotes(notes, "meeting").size)
    }

    @Test
    fun `filterNotes returns all if query is empty`() {
        val notes = listOf(
            NoteMetadata("1", "A", "1.md", "", 0L, 0L, 0, ""),
            NoteMetadata("2", "B", "2.md", "", 0L, 0L, 0, "")
        )
        assertEquals(2, NoteUtils.filterNotes(notes, "").size)
    }

    @Test
    fun `filterNotes returns empty for no match`() {
        val notes = listOf(NoteMetadata("1", "Meeting", "1.md", "", 0L, 0L, 0, ""))
        assertEquals(0, NoteUtils.filterNotes(notes, "xyz").size)
    }
}
