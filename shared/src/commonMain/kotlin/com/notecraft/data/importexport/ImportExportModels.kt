package com.notecraft.data.importexport

data class ImportResult(
    val content: String,
    val suggestedTitle: String,
    val fileName: String
)

data class ExportRequest(
    val content: String,
    val fileName: String
)
