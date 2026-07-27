package com.notecraft.ui.markdown

data class MdSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: MdLink? = null
)

data class MdLink(val url: String, val text: String)

sealed class MdBlock {
    data class Heading(val level: Int, val text: List<MdSpan>) : MdBlock()
    data class Paragraph(val spans: List<MdSpan>) : MdBlock()
    data class CodeBlock(val language: String, val code: String) : MdBlock()
    data class BulletList(val items: List<List<MdSpan>>, val ordered: Boolean = false) : MdBlock()
    data class TaskList(val items: List<MdTaskItem>) : MdBlock()
    data class Blockquote(val text: List<MdSpan>) : MdBlock()
    data object ThematicBreak : MdBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MdBlock()
}

data class MdTaskItem(val checked: Boolean, val spans: List<MdSpan>)

object MarkdownParser {

    fun parse(content: String): List<MdBlock> {
        val lines = content.split("\n")
        val blocks = mutableListOf<MdBlock>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            when {
                // Thematic break
                line.matches(Regex("^[-*_]{3,}\\s*$")) -> {
                    blocks.add(MdBlock.ThematicBreak)
                    i++
                }
                // Heading
                Regex("^#{1,6}\\s").containsMatchIn(line) -> {
                    val level = line.takeWhile { it == '#' }.length
                    val text = parseInline(line.drop(level).trim())
                    blocks.add(MdBlock.Heading(level, text))
                    i++
                }
                // Code block
                line.trimStart().startsWith("```") -> {
                    val lang = line.trimStart().drop(3).trim()
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    i++ // skip closing ```
                    blocks.add(MdBlock.CodeBlock(lang, codeLines.joinToString("\n")))
                }
                // Blockquote
                line.trimStart().startsWith(">") -> {
                    val text = parseInline(line.trimStart().drop(1).trim())
                    blocks.add(MdBlock.Blockquote(text))
                    i++
                }
                // Task list
                line.trimStart().matches(Regex("^[-*+] \\[[ xX]\\].*")) -> {
                    val items = mutableListOf<MdTaskItem>()
                    while (i < lines.size) {
                        val trimmed = lines[i].trimStart()
                        val match = Regex("^[-*+] \\[([ xX])\\] (.*)").find(trimmed)
                        if (match == null) break
                        val checked = match.groupValues[1].lowercase() == "x"
                        val text = parseInline(match.groupValues[2])
                        items.add(MdTaskItem(checked, text))
                        i++
                    }
                    blocks.add(MdBlock.TaskList(items))
                }
                // Unordered list
                line.trimStart().matches(Regex("^[-*+]\\s.*")) -> {
                    val items = mutableListOf<List<MdSpan>>()
                    while (i < lines.size) {
                        val trimmed = lines[i].trimStart()
                        val match = Regex("^[-*+]\\s+(.*)").find(trimmed)
                        if (match == null) break
                        val text = parseInline(match.groupValues[1])
                        items.add(text)
                        i++
                    }
                    blocks.add(MdBlock.BulletList(items))
                }
                // Ordered list
                line.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val items = mutableListOf<List<MdSpan>>()
                    while (i < lines.size) {
                        val trimmed = lines[i].trimStart()
                        val match = Regex("^\\d+\\.\\s+(.*)").find(trimmed)
                        if (match == null) break
                        val text = parseInline(match.groupValues[1])
                        items.add(text)
                        i++
                    }
                    blocks.add(MdBlock.BulletList(items, ordered = true))
                }
                // Table
                line.contains("|") && line.matches(Regex(".*\\|.*")) -> {
                    if (i + 1 < lines.size && lines[i + 1].matches(Regex("[| :\\-]+"))) {
                        val header = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        val rows = mutableListOf<List<String>>()
                        i += 2
                        while (i < lines.size && lines[i].contains("|")) {
                            val row = lines[i].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                            rows.add(row)
                            i++
                        }
                        blocks.add(MdBlock.Table(header, rows))
                    } else {
                        i++
                    }
                }
                // Empty line
                line.isBlank() -> i++
                // Paragraph (accumulate until blank line)
                else -> {
                    val paraLines = mutableListOf(line)
                    i++
                    while (i < lines.size && lines[i].isNotBlank() &&
                           !Regex("^#{1,6}\\s").containsMatchIn(lines[i]) &&
                           !lines[i].trimStart().startsWith("```") &&
                           !lines[i].trimStart().startsWith(">") &&
                           !lines[i].trimStart().matches(Regex("^[-*+]\\s.*")) &&
                           !lines[i].trimStart().matches(Regex("^\\d+\\.\\s.*")) &&
                           !lines[i].matches(Regex("^[-*_]{3,}\\s*$"))) {
                        paraLines.add(lines[i])
                        i++
                    }
                    val spans = parseInline(paraLines.joinToString(" "))
                    if (spans.isNotEmpty()) {
                        blocks.add(MdBlock.Paragraph(spans))
                    }
                }
            }
        }
        return blocks
    }

    fun parseInline(text: String): List<MdSpan> {
        val spans = mutableListOf<MdSpan>()
        var i = 0
        val chars = text.toList()

        while (i < chars.size) {
            when {
                // Code span
                i + 1 < chars.size && chars[i] == '`' && chars[i + 1] == '`' -> {
                    val end = text.indexOf("``", i + 2)
                    if (end >= 0) {
                        spans.add(MdSpan(text = text.substring(i + 2, end), code = true))
                        i = end + 2
                    } else { spans.add(MdSpan(text = chars[i].toString())); i++ }
                }
                chars[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end >= 0) {
                        spans.add(MdSpan(text = text.substring(i + 1, end), code = true))
                        i = end + 1
                    } else { spans.add(MdSpan(text = "`")); i++ }
                }
                // Link
                chars[i] == '[' -> {
                    val close = text.indexOf(']', i)
                    val paren = if (close >= 0 && close + 1 < chars.size && chars[close + 1] == '(') text.indexOf(')', close + 2) else -1
                    if (close >= 0 && paren >= 0) {
                        val linkText = text.substring(i + 1, close)
                        val url = text.substring(close + 2, paren)
                        spans.add(MdSpan(text = linkText, link = MdLink(url, linkText)))
                        i = paren + 1
                    } else { spans.add(MdSpan(text = chars[i].toString())); i++ }
                }
                // Bold + Italic ***
                i + 2 < chars.size && chars[i] == '*' && chars[i + 1] == '*' && chars[i + 2] == '*' -> {
                    val end = text.indexOf("***", i + 3)
                    if (end >= 0) {
                        val inner = text.substring(i + 3, end)
                        spans.add(MdSpan(text = inner, bold = true, italic = true))
                        i = end + 3
                    } else { spans.add(MdSpan(text = "***")); i += 3 }
                }
                // Bold **
                i + 1 < chars.size && chars[i] == '*' && chars[i + 1] == '*' -> {
                    val end = text.indexOf("**", i + 2)
                    if (end >= 0) {
                        val inner = text.substring(i + 2, end)
                        spans.add(MdSpan(text = inner, bold = true))
                        i = end + 2
                    } else { spans.add(MdSpan(text = "**")); i += 2 }
                }
                // Italic *
                chars[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end >= 0 && end > i + 1) {
                        val inner = text.substring(i + 1, end)
                        spans.add(MdSpan(text = inner, italic = true))
                        i = end + 1
                    } else { spans.add(MdSpan(text = chars[i].toString())); i++ }
                }
                // Strikethrough ~~
                i + 1 < chars.size && chars[i] == '~' && chars[i + 1] == '~' -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end >= 0) {
                        spans.add(MdSpan(text = text.substring(i + 2, end)))
                        i = end + 2
                    } else { spans.add(MdSpan(text = "~~")); i += 2 }
                }
                else -> {
                    // Accumulate plain text
                    val start = i
                    while (i < chars.size && chars[i] != '*' && chars[i] != '`' && chars[i] != '[' && chars[i] != '~') {
                        i++
                    }
                    if (i > start) {
                        spans.add(MdSpan(text = text.substring(start, i)))
                    } else {
                        spans.add(MdSpan(text = chars[i].toString()))
                        i++
                    }
                }
            }
        }
        return spans
    }
}
