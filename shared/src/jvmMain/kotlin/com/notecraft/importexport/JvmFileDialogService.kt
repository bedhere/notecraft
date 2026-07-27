package com.notecraft.importexport

import com.notecraft.util.Strings
import com.notecraft.data.importexport.FileDialogService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class JvmFileDialogService : FileDialogService {
    override suspend fun openAndRead(): String? = withContext(Dispatchers.IO) {
        val chooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter("Markdown 文件 (*.md)", "md")
            isAcceptAllFileFilterUsed = false
            dialogTitle = "导入 Markdown"
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile?.let { file ->
                if (file.exists()) file.readText() else null
            }
        } else null
    }

    override suspend fun saveAndWrite(defaultName: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val chooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter("Markdown 文件 (*.md)", "md")
            isAcceptAllFileFilterUsed = false
            selectedFile = File(defaultName)
            dialogTitle = "导出 Markdown"
        }
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile?.let { file ->
                file.writeText(content)
                true
            } ?: false
        } else false
    }
}
