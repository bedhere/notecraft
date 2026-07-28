package com.notecraft.ui.editor

import kotlin.math.roundToInt

data class CursorPosition(
    val line: Int,
    val column: Int
)

object EditorStatusInfo {

    fun cursorPosition(text: String, cursorIndex: Int): CursorPosition {
        val cursor = cursorIndex.coerceIn(0, text.length)
        var line = 1
        var column = 1
        for (index in 0 until cursor) {
            if (text[index] == '\n') {
                line++
                column = 1
            } else {
                column++
            }
        }
        return CursorPosition(line = line, column = column)
    }

    fun utf8SizeLabel(text: String): String {
        val bytes = text.encodeToByteArray().size
        if (bytes < 1024) return "$bytes B"

        val tenths = (bytes * 10.0 / 1024.0).roundToInt()
        val whole = tenths / 10
        val fraction = tenths % 10
        return "$whole.$fraction KB"
    }
}
