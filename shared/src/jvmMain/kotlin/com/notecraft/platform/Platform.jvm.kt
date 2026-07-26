package com.notecraft.platform

import java.io.File

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual object FileSystem {
    actual fun readTextFile(path: String): String = File(path).readText()
    actual fun writeTextFile(path: String, content: String) {
        File(path).parentFile?.mkdirs()
        File(path).writeText(content)
    }
    actual fun createDirectories(path: String) {
        File(path).mkdirs()
    }
    actual fun deleteFile(path: String) {
        File(path).delete()
    }
    actual fun fileExists(path: String): Boolean = File(path).exists()
}
