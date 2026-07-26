package com.notecraft.data.importexport

interface FileDialogService {
    suspend fun openAndRead(): String?
    suspend fun saveAndWrite(defaultName: String, content: String): Boolean
}
