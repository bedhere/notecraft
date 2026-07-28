package com.notecraft.ui.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownFormattingTest {

    @Test
    fun `bold wraps selected text and selects wrapped result`() {
        val result = MarkdownFormatting.apply("hello world", 6, 11, MarkdownFormat.BOLD)

        assertEquals("hello **world**", result.text)
        assertEquals(6, result.selectionStart)
        assertEquals(15, result.selectionEnd)
    }

    @Test
    fun `bold inserts editable placeholder at cursor`() {
        val result = MarkdownFormatting.apply("hello", 5, 5, MarkdownFormat.BOLD)

        assertEquals("hello****", result.text)
        assertEquals(7, result.selectionStart)
        assertEquals(7, result.selectionEnd)
    }

    @Test
    fun `selected lines become a bullet list`() {
        val result = MarkdownFormatting.apply("one\ntwo\nthree", 0, 7, MarkdownFormat.BULLET_LIST)

        assertEquals("- one\n- two\nthree", result.text)
        assertEquals(0, result.selectionStart)
        assertEquals(11, result.selectionEnd)
    }

    @Test
    fun `ordered list toggles existing prefixes`() {
        val result = MarkdownFormatting.apply("1. one\n2. two", 0, 14, MarkdownFormat.ORDERED_LIST)

        assertEquals("one\ntwo", result.text)
    }

    @Test
    fun `code block keeps selected content selected`() {
        val result = MarkdownFormatting.apply("text", 0, 4, MarkdownFormat.CODE_BLOCK)

        assertEquals("```\ntext\n```", result.text)
        assertEquals(4, result.selectionStart)
        assertEquals(8, result.selectionEnd)
    }

    @Test
    fun `heading and quote operate on current line without selection`() {
        val heading = MarkdownFormatting.apply("Title", 2, 2, MarkdownFormat.HEADING)
        val quote = MarkdownFormatting.apply("Title", 2, 2, MarkdownFormat.BLOCKQUOTE)

        assertEquals("# Title", heading.text)
        assertEquals(4, heading.selectionStart)
        assertEquals("> Title", quote.text)
        assertEquals(4, quote.selectionStart)
    }

    @Test
    fun `thematic break inserts at cursor without replacing the note`() {
        val result = MarkdownFormatting.apply("before\nafter", 7, 7, MarkdownFormat.THEMATIC_BREAK)

        assertEquals("before\n---\nafter", result.text)
        assertEquals(11, result.selectionStart)
    }
}
