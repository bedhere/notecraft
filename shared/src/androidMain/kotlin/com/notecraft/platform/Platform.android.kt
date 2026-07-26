package com.notecraft.platform

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual object FileSystem {
    actual fun readTextFile(path: String): String {
        throw UnsupportedOperationException("FileSystem should use AndroidNoteStorage")
    }
    actual fun writeTextFile(path: String, content: String) {
        throw UnsupportedOperationException("FileSystem should use AndroidNoteStorage")
    }
    actual fun createDirectories(path: String) {
        throw UnsupportedOperationException("FileSystem should use AndroidNoteStorage")
    }
    actual fun deleteFile(path: String) {
        throw UnsupportedOperationException("FileSystem should use AndroidNoteStorage")
    }
    actual fun fileExists(path: String): Boolean {
        throw UnsupportedOperationException("FileSystem should use AndroidNoteStorage")
    }
}
