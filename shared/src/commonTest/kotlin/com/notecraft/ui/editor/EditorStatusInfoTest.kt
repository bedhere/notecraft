package com.notecraft.ui.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class EditorStatusInfoTest {

    @Test
    fun `cursor position is one based at document start`() {
        assertEquals(CursorPosition(line = 1, column = 1), EditorStatusInfo.cursorPosition("hello", 0))
    }

    @Test
    fun `cursor position advances across lines`() {
        assertEquals(CursorPosition(line = 3, column = 3), EditorStatusInfo.cursorPosition("one\ntwo\n\u4e09\u56db", 10))
    }

    @Test
    fun `cursor position clamps out of range cursor`() {
        assertEquals(CursorPosition(line = 2, column = 4), EditorStatusInfo.cursorPosition("abc\ndef", 99))
    }

    @Test
    fun `utf8 size counts actual encoded bytes`() {
        assertEquals("9 B", EditorStatusInfo.utf8SizeLabel("abc\u4e2d\u6587"))
    }

    @Test
    fun `utf8 size formats kilobytes with one decimal place`() {
        assertEquals("1.5 KB", EditorStatusInfo.utf8SizeLabel("a".repeat(1536)))
    }
}
