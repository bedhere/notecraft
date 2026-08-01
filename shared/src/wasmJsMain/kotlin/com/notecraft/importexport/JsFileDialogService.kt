package com.notecraft.importexport

import com.notecraft.data.importexport.FileDialogService

class JsFileDialogService : FileDialogService {
    override suspend fun openAndRead(): String? = null

    override suspend fun saveAndWrite(defaultName: String, content: String): Boolean = false
}
