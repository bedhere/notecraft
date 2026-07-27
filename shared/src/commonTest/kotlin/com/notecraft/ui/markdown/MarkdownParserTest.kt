package com.notecraft.ui.markdown

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownParserTest {

    @Test
    fun `parse heading levels 1 to 6`() {
        for (level in 1..6) {
            val md = "#".repeat(level) + " Heading $level"
            val blocks = MarkdownParser.parse(md)
            assertTrue(blocks.isNotEmpty(), "No blocks for H$level")
            assertTrue(blocks[0] is MdBlock.Heading, "Block 0 is not Heading for H$level, got ${blocks[0]::class.simpleName}")
            val heading = blocks[0] as MdBlock.Heading
            assertEquals(level, heading.level, "Heading level $level failed")
            assertEquals("Heading $level", heading.text.joinToString("") { it.text })
        }
    }

    @Test
    fun `parse bold text`() {
        val blocks = MarkdownParser.parse("Hello **world**")
        assertTrue(blocks.isNotEmpty())
        assertTrue(blocks[0] is MdBlock.Paragraph, "Expected Paragraph, got ${blocks[0]::class.simpleName}")
        val spans = (blocks[0] as MdBlock.Paragraph).spans
        assertTrue(spans.size >= 2, "Expected at least 2 spans, got ${spans.size}")
        val boldSpan = spans.find { it.bold }
        assertTrue(boldSpan != null, "No bold span found in $spans")
        assertEquals("world", boldSpan?.text)
    }

    @Test
    fun `parse italic text`() {
        val blocks = MarkdownParser.parse("Hello *world*")
        val spans = (blocks[0] as MdBlock.Paragraph).spans
        assertEquals(true, spans[1].italic)
        assertEquals("world", spans[1].text)
    }

    @Test
    fun `parse bold and italic`() {
        val blocks = MarkdownParser.parse("Hello ***world***")
        val spans = (blocks[0] as MdBlock.Paragraph).spans
        assertEquals(true, spans[1].bold)
        assertEquals(true, spans[1].italic)
    }

    @Test
    fun `parse inline code`() {
        val blocks = MarkdownParser.parse("Use `code` here")
        val spans = (blocks[0] as MdBlock.Paragraph).spans
        assertTrue(spans.any { it.code && it.text == "code" })
    }

    @Test
    fun `parse code block`() {
        val blocks = MarkdownParser.parse("""```kotlin
fun main() {}
```""")
        val codeBlock = blocks[0] as? MdBlock.CodeBlock
        assertEquals("kotlin", codeBlock?.language)
        assertTrue(codeBlock?.code?.contains("fun main") == true)
    }

    @Test
    fun `parse unordered list`() {
        val blocks = MarkdownParser.parse("- Item 1\n- Item 2")
        val list = blocks[0] as? MdBlock.BulletList
        assertEquals(2, list?.items?.size)
    }

    @Test
    fun `parse ordered list`() {
        val blocks = MarkdownParser.parse("1. First\n2. Second")
        val list = blocks[0] as? MdBlock.BulletList
        assertEquals(true, list?.ordered)
        assertEquals(2, list?.items?.size)
    }

    @Test
    fun `parse task list`() {
        val blocks = MarkdownParser.parse("- [x] Done\n- [ ] Todo")
        val tasks = blocks[0] as? MdBlock.TaskList
        assertEquals(2, tasks?.items?.size)
        assertEquals(true, tasks?.items?.get(0)?.checked)
        assertEquals(false, tasks?.items?.get(1)?.checked)
    }

    @Test
    fun `parse blockquote`() {
        val blocks = MarkdownParser.parse("> Quote text")
        val quote = blocks[0] as? MdBlock.Blockquote
        assertEquals("Quote text", quote?.text?.joinToString("") { it.text })
    }

    @Test
    fun `parse thematic break`() {
        listOf("---", "***", "___").forEach { sep ->
            val blocks = MarkdownParser.parse(sep)
            assertTrue(blocks[0] is MdBlock.ThematicBreak, "Failed for $sep")
        }
    }

    @Test
    fun `parse link`() {
        val blocks = MarkdownParser.parse("[text](https://example.com)")
        val spans = (blocks[0] as MdBlock.Paragraph).spans
        assertEquals("text", spans[0].text)
        assertEquals("https://example.com", spans[0].link?.url)
    }

    @Test
    fun `parse Chinese text`() {
        val blocks = MarkdownParser.parse("# 中文标题\n这是一段中文内容。")
        assertTrue(blocks.isNotEmpty())
        assertTrue(blocks[0] is MdBlock.Heading, "Expected Heading, got ${blocks[0]::class.simpleName}")
        val heading = blocks[0] as? MdBlock.Heading
        assertEquals("中文标题", heading?.text?.joinToString("") { it.text })
        assertTrue(blocks.size > 1, "Expected at least 2 blocks for heading + paragraph")
        val para = blocks[1] as? MdBlock.Paragraph
        assertEquals("这是一段中文内容。", para?.spans?.joinToString("") { it.text })
    }

    @Test
    fun `parse emoji`() {
        val blocks = MarkdownParser.parse("Hello 😊 World 🌍")
        val spans = (blocks[0] as MdBlock.Paragraph).spans
        val text = spans.joinToString("") { it.text }
        assertTrue(text.contains("😊"))
        assertTrue(text.contains("🌍"))
    }

    @Test
    fun `parse table`() {
        val md = "| H1 | H2 |\n|---|---|\n| A | B |"
        val blocks = MarkdownParser.parse(md)
        val table = blocks[0] as? MdBlock.Table
        assertEquals(listOf("H1", "H2"), table?.headers)
        assertEquals("A", table?.rows?.get(0)?.get(0))
    }

    @Test
    fun `parse empty content returns empty`() {
        val blocks = MarkdownParser.parse("")
        assertTrue(blocks.isEmpty())
    }

    @Test
    fun `parse mixed content`() {
        val md = """# Title

This is a **bold** paragraph with *italic* and `code`.

- Item 1
- Item 2

> A quote

---

```python
print("hello")
```"""
        val blocks = MarkdownParser.parse(md)
        assertTrue(blocks.isNotEmpty(), "Expected multiple blocks")
        assertTrue(blocks[0] is MdBlock.Heading)
        assertTrue(blocks.any { it is MdBlock.Paragraph })
        assertTrue(blocks.any { it is MdBlock.BulletList })
        assertTrue(blocks.any { it is MdBlock.Blockquote })
        assertTrue(blocks.any { it is MdBlock.ThematicBreak })
        assertTrue(blocks.any { it is MdBlock.CodeBlock })
    }
}
