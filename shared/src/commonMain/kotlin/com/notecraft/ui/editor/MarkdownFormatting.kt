package com.notecraft.ui.editor

enum class MarkdownFormat {
    BOLD,
    ITALIC,
    HEADING,
    THEMATIC_BREAK,
    BULLET_LIST,
    ORDERED_LIST,
    CODE_BLOCK,
    BLOCKQUOTE
}

data class MarkdownEditResult(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int
)

object MarkdownFormatting {

    fun apply(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        format: MarkdownFormat
    ): MarkdownEditResult {
        val start = selectionStart.coerceIn(0, text.length)
        val end = selectionEnd.coerceIn(start, text.length)
        return when (format) {
            MarkdownFormat.BOLD -> wrapInline(text, start, end, "**", "**")
            MarkdownFormat.ITALIC -> wrapInline(text, start, end, "*", "*")
            MarkdownFormat.HEADING -> prefixLines(text, start, end, "# ", PrefixMode.HEADING)
            MarkdownFormat.THEMATIC_BREAK -> insertThematicBreak(text, start, end)
            MarkdownFormat.BULLET_LIST -> prefixLines(text, start, end, "- ", PrefixMode.BULLET)
            MarkdownFormat.ORDERED_LIST -> prefixLines(text, start, end, "1. ", PrefixMode.ORDERED)
            MarkdownFormat.CODE_BLOCK -> wrapBlock(text, start, end, "```")
            MarkdownFormat.BLOCKQUOTE -> prefixLines(text, start, end, "> ", PrefixMode.BLOCKQUOTE)
        }
    }

    private fun wrapInline(
        text: String,
        start: Int,
        end: Int,
        prefix: String,
        suffix: String
    ): MarkdownEditResult {
        if (start == end) {
            val inserted = prefix + suffix
            return replace(text, start, end, inserted, start + prefix.length, start + prefix.length)
        }

        val selected = text.substring(start, end)
        return if (selected.startsWith(prefix) && selected.endsWith(suffix) &&
            selected.length >= prefix.length + suffix.length
        ) {
            val unwrapped = selected.removePrefix(prefix).removeSuffix(suffix)
            replace(text, start, end, unwrapped, start, start + unwrapped.length)
        } else {
            val wrapped = prefix + selected + suffix
            replace(text, start, end, wrapped, start, start + wrapped.length)
        }
    }

    private fun wrapBlock(text: String, start: Int, end: Int, fence: String): MarkdownEditResult {
        if (start == end) {
            val inserted = "$fence\n\n$fence"
            val cursor = start + fence.length + 1
            return replace(text, start, end, inserted, cursor, cursor)
        }

        val selected = text.substring(start, end)
        val wrapped = "$fence\n$selected\n$fence"
        val contentStart = start + fence.length + 1
        return replace(text, start, end, wrapped, contentStart, contentStart + selected.length)
    }

    private fun insertThematicBreak(text: String, start: Int, end: Int): MarkdownEditResult {
        if (start != end) {
            return replace(text, start, end, "---", start + 3, start + 3)
        }
        val prefix = if (start > 0 && text[start - 1] != '\n') "\n" else ""
        val suffix = if (start < text.length && text[start] != '\n') "\n" else ""
        val inserted = prefix + "---" + suffix
        val cursor = start + inserted.length
        return replace(text, start, end, inserted, cursor, cursor)
    }

    private fun prefixLines(
        text: String,
        start: Int,
        end: Int,
        prefix: String,
        mode: PrefixMode
    ): MarkdownEditResult {
        val target = lineRange(text, start, end)
        val selected = text.substring(target.first, target.second)
        val lines = selected.split("\n")
        val nonBlankLines = lines.filter { it.isNotBlank() }
        val shouldRemove = nonBlankLines.isNotEmpty() && nonBlankLines.all { mode.hasPrefix(it) }
        val transformed = lines.joinToString("\n") { line ->
            when {
                line.isBlank() -> line
                shouldRemove -> mode.removePrefix(line)
                else -> prefix + line
            }
        }
        val resultStart = if (start == end) {
            start + if (shouldRemove) -prefix.length else prefix.length
        } else {
            target.first
        }
        val resultEnd = if (start == end) {
            resultStart
        } else {
            target.first + transformed.length
        }
        return replace(text, target.first, target.second, transformed, resultStart, resultEnd)
    }

    private fun lineRange(text: String, start: Int, end: Int): Pair<Int, Int> {
        val selectionEnd = if (
            end > start &&
            end > 0 &&
            end <= text.length &&
            text[end - 1] == '\n'
        ) {
            end - 1
        } else {
            end
        }
        val lineStart = text.lastIndexOf('\n', start - 1).let { if (it == -1) 0 else it + 1 }
        val lineEndIndex = text.indexOf('\n', selectionEnd)
        val lineEnd = if (lineEndIndex == -1) text.length else lineEndIndex
        return lineStart to lineEnd
    }

    private fun replace(
        text: String,
        start: Int,
        end: Int,
        replacement: String,
        selectionStart: Int,
        selectionEnd: Int
    ): MarkdownEditResult {
        val newText = text.substring(0, start) + replacement + text.substring(end)
        return MarkdownEditResult(
            text = newText,
            selectionStart = selectionStart.coerceIn(0, newText.length),
            selectionEnd = selectionEnd.coerceIn(0, newText.length)
        )
    }

    private enum class PrefixMode {
        HEADING {
            override fun hasPrefix(line: String) = line.startsWith("# ")
            override fun removePrefix(line: String) = line.removePrefix("# ")
        },
        BULLET {
            override fun hasPrefix(line: String) = line.startsWith("- ")
            override fun removePrefix(line: String) = line.removePrefix("- ")
        },
        ORDERED {
            override fun hasPrefix(line: String) = orderedPrefixLength(line) > 0
            override fun removePrefix(line: String): String {
                val prefixLength = orderedPrefixLength(line)
                return if (prefixLength == 0) line else line.substring(prefixLength)
            }
        },
        BLOCKQUOTE {
            override fun hasPrefix(line: String) = line.startsWith("> ")
            override fun removePrefix(line: String) = line.removePrefix("> ")
        };

        abstract fun hasPrefix(line: String): Boolean
        abstract fun removePrefix(line: String): String

        companion object {
            fun orderedPrefixLength(line: String): Int {
                val separator = line.indexOf(". ")
                if (separator <= 0 || !line.substring(0, separator).all { it.isDigit() }) {
                    return 0
                }
                return separator + 2
            }
        }
    }
}
