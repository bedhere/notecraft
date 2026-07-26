package com.notecraft.platform

actual fun currentTimeMillis(): Long {
    return js("Date.now()").unsafeCast<Double>().toLong()
}

actual object FileSystem {
    actual fun readTextFile(path: String): String {
        throw UnsupportedOperationException("FileSystem not available in browser (Wasm)")
    }
    actual fun writeTextFile(path: String, content: String) {
        throw UnsupportedOperationException("FileSystem not available in browser (Wasm)")
    }
    actual fun createDirectories(path: String) {
        throw UnsupportedOperationException("FileSystem not available in browser (Wasm)")
    }
    actual fun deleteFile(path: String) {
        throw UnsupportedOperationException("FileSystem not available in browser (Wasm)")
    }
    actual fun fileExists(path: String): Boolean = false
}
