package com.notecraft.data.importexport

import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.util.NoteUtils

class ImportExportUseCase(
    private val noteRepository: NoteRepository,
    private val fileDialog: FileDialogService
) {
    suspend fun importMarkdownFile(category: String = ""): Boolean {
        val content = fileDialog.openAndRead() ?: return false
        if (content.isBlank()) return false
        val title = extractTitle(content)
        val fileName = NoteUtils.buildFileName("import", title)
        noteRepository.createNote(SaveNoteRequest(
            title = title,
            content = content,
            category = category
        ))
        return true
    }

    suspend fun exportMarkdownFile(noteId: String): Boolean {
        val note = try { noteRepository.getNote(noteId) } catch (_: Exception) { return false }
        val safeName = sanitizeFileName(note.title).ifBlank { "untitled" }
        val fileName = safeName + ".md"
        return fileDialog.saveAndWrite(fileName, note.content)
    }

    fun extractTitle(content: String): String {
        val lines = content.trimStart().split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("# ")) {
                return trimmed.removePrefix("# ").trim()
            }
        }
        return ""
    }

    fun sanitizeFileName(title: String): String {
        val forbidden = setOf(
            '<', '>', ':', '\u0022', '/', '\\', '|', '?', '*'
        )
        return title.map { c ->
            if (c in forbidden || c.code < 32) '_' else c
        }.joinToString("").trim().take(80).replace(Regex("\\s+"), "_")
    }
}
