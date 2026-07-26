package com.notecraft.data.migration

import com.notecraft.domain.model.Note
import com.notecraft.domain.model.SaveNoteRequest
import com.notecraft.domain.repository.NoteRepository
import com.notecraft.util.NoteUtils
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

class LegacyDataMigrator(
    private val noteRepository: NoteRepository
) {

    private val sentinelFile: File
        get() = File(legacyDataDir.parentFile ?: legacyDataDir, ".notecraft_migrated")

    fun isAlreadyMigrated(): Boolean = sentinelFile.exists()

    fun findLegacyDataDir(): File? {
        val home = System.getProperty("user.home")
        val candidates = listOf(
            File(home, "Documents" + File.separator + "花笺"),
            File(home, "Documents" + File.separator + "floral"),
            File(System.getenv("USERPROFILE") ?: home, "Documents" + File.separator + "花笺"),
        )
        return candidates.firstOrNull {
            it.isDirectory() && File(it, "metadata.json").exists()
        }
    }

    private val legacyDataDir: File
        get() = findLegacyDataDir() ?: throw IllegalStateException("No legacy data found")

    suspend fun migrate(): MigrationResult {
        val dir = findLegacyDataDir() ?: return MigrationResult()
        if (isAlreadyMigrated()) return MigrationResult()

        val metaFile = File(dir, "metadata.json")
        if (!metaFile.exists()) return MigrationResult()

        return try {
            val metadata = json.decodeFromString<LegacyMetadataFile>(metaFile.readText())
            var imported = 0
            var skipped = 0
            val errors = mutableListOf<String>()

            for (legacy in metadata.notes) {
                try {
                    val noteDir = if (legacy.category.isNotEmpty())
                        File(dir, legacy.category) else dir
                    val noteFile = File(noteDir, legacy.fileName)
                    val content = if (noteFile.exists()) noteFile.readText() else legacy.preview
                    val request = SaveNoteRequest(
                        title = legacy.title,
                        content = content,
                        category = legacy.category
                    )
                    noteRepository.createNote(request)
                    imported++
                } catch (e: Exception) {
                    errors.add("Failed to import " + legacy.id + ": " + e.message.orEmpty())
                    skipped++
                }
            }

            // Write sentinel
            sentinelFile.writeText("Migrated on " + java.util.Date().toString())

            MigrationResult(
                totalNotes = metadata.notes.size,
                importedNotes = imported,
                skippedNotes = skipped,
                errors = errors
            )
        } catch (e: Exception) {
            MigrationResult(errors = listOf("Migration failed: " + e.message.orEmpty()))
        }
    }
}
