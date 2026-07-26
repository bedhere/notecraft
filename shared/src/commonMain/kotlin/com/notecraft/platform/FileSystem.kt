package com.notecraft.platform

expect object FileSystem {
    fun readTextFile(path: String): String
    fun writeTextFile(path: String, content: String)
    fun createDirectories(path: String)
    fun deleteFile(path: String)
    fun fileExists(path: String): Boolean
}
