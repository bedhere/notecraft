package com.notecraft.util

import com.notecraft.domain.model.Note
import com.notecraft.domain.model.NoteMetadata

object NoteUtils {
    fun buildPreview(content: String): String =
        content.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ").take(80)

    fun countChars(content: String): Int =
        content.count { !it.isWhitespace() }

    fun buildFileName(id: String, title: String): String {
        val safe = title
            .take(80)
            .map { if (it in "<>:\"/\\|?*" || it.code < 0x20) '_' else it }
            .joinToString("")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return "${id}_${safe}.md"
    }

    fun getDisplayTitle(note: NoteMetadata): String {
        val title = note.title.trim()
        if (title.isNotEmpty()) return title
        val preview = note.preview.trim()
        if (preview.isNotEmpty()) return preview.take(20)
        return "untitled"
    }

    data class CategoryGroup(
        val category: String,
        val notes: List<NoteMetadata>,
        val latestUpdatedAt: Long
    )

    fun groupByCategory(
        notes: List<NoteMetadata>,
        allCategories: List<String> = emptyList()
    ): List<CategoryGroup> {
        val grouped = allCategories.associateWith { mutableListOf<NoteMetadata>() }.toMutableMap()
        for (note in notes) {
            val key = if (note.category.isEmpty()) "" else note.category
            grouped.getOrPut(key) { mutableListOf() }.add(note)
        }
        return grouped.map { (cat, catNotes) ->
            val sorted = catNotes.sortedByDescending { it.updatedAt }
            CategoryGroup(cat, sorted, sorted.firstOrNull()?.updatedAt ?: 0L)
        }.sortedBy { if (it.category.isEmpty()) 1 else 0 }
    }

    fun filterNotes(notes: List<NoteMetadata>, query: String): List<NoteMetadata> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return notes
        return notes.filter { note ->
            listOf(note.title, note.preview, note.fileName)
                .joinToString(" ").lowercase().contains(q)
        }
    }
}
