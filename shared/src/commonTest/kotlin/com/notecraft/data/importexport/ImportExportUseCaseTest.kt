package com.notecraft.data.importexport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImportExportUseCaseTest {

    private val useCase = ImportExportUseCase(
        noteRepository = com.notecraft.data.repository.InMemoryNoteRepository(),
        fileDialog = object : FileDialogService {
            override suspend fun openAndRead(): String? = null
            override suspend fun saveAndWrite(defaultName: String, content: String): Boolean = false
        }
    )

    @Test
    fun `extractTitle from H1`() {
        assertEquals("My Title", useCase.extractTitle("# My Title\n\ncontent"))
    }

    @Test
    fun `extractTitle from first H1 only`() {
        assertEquals("First", useCase.extractTitle("# First\n\n# Second"))
    }

    @Test
    fun `extractTitle returns empty when no H1`() {
        assertEquals("", useCase.extractTitle("Plain text\nno heading"))
    }

    @Test
    fun `extractTitle handles Chinese`() {
        assertEquals("中文标题", useCase.extractTitle("# 中文标题\n\n内容"))
    }

    @Test
    fun `sanitizeFileName replaces forbidden chars`() {
        val result = useCase.sanitizeFileName("a<b>c:d")
        assertFalse(result.contains('<'))
        assertFalse(result.contains('>'))
        assertFalse(result.contains(':'))
    }

    @Test
    fun `sanitizeFileName handles empty`() {
        assertEquals("", useCase.sanitizeFileName(""))
    }

    @Test
    fun `sanitizeFileName truncates long names`() {
        val long = "A".repeat(200)
        val result = useCase.sanitizeFileName(long)
        assertTrue(result.length <= 80)
    }

    @Test
    fun `sanitizeFileName handles Chinese`() {
        val result = useCase.sanitizeFileName("中文文件名")
        assertEquals("中文文件名", result)
    }
}
