package com.notecraft.importexport

import com.notecraft.data.importexport.FileDialogService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class JvmFileDialogService : FileDialogService {
    override suspend fun openAndRead(): String? = withContext(Dispatchers.IO) {
        val chooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter("Markdown Files", "md")
            isAcceptAllFileFilterUsed = false
            dialogTitle = "Import Markdown"
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile?.let { file ->
                if (file.exists()) file.readText() else null
            }
        } else null
    }

    override suspend fun saveAndWrite(defaultName: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val chooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter("Markdown Files", "md")
            isAcceptAllFileFilterUsed = false
            selectedFile = File(defaultName)
            dialogTitle = "Export Markdown"
        }
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile?.let { file ->
                file.writeText(content)
                true
            } ?: false
        } else false
    }
}
