package com.bedhere.app

import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.util.NoteUtils
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileDropHandler(
    private val noteRepository: NoteRepository,
    private val onNoteCreated: () -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun dispose() {
        scope.cancel()
    }


    fun setup(component: java.awt.Component) {
        DropTarget(component, object : DropTargetAdapter() {
            override fun drop(event: DropTargetDropEvent) {
                event.acceptDrop(DnDConstants.ACTION_COPY)
                try {
                    val dropped = event.transferable.getTransferData(DataFlavor.javaFileListFlavor)
                    @Suppress("UNCHECKED_CAST")
                    val files = dropped as List<File>
                    for (file in files) {
                        if (file.extension.lowercase() in listOf("md", "markdown", "txt")) {
                            importFile(file)
                        }
                    }
                    event.dropComplete(true)
                } catch (e: Exception) {
                    event.dropComplete(false)
                }
            }
        })
    }

    private fun importFile(file: File) {
        try {
            val content = file.readText()
            val title = extractTitle(content).ifEmpty {
                file.nameWithoutExtension
            }
            scope.launch {
                try {
                    noteRepository.createNote(SaveNoteRequest(
                        title = title,
                        content = content,
                        category = ""
                    ))
                    withContext(Dispatchers.Main) { onNoteCreated() }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractTitle(content: String): String {
        for (line in content.trimStart().split("\n")) {
            val t = line.trim()
            if (t.startsWith("# ")) return t.removePrefix("# ").trim()
        }
        return ""
    }
}
