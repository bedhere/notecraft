package com.notecraft.importexport

import com.notecraft.data.importexport.FileDialogService
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class JsFileDialogService : FileDialogService {

    override suspend fun openAndRead(): String? {
        return suspendCancellableCoroutine { continuation ->
            val input = document.createElement("input")
            input.setAttribute("type", "file")
            input.setAttribute("accept", ".md,.markdown,.txt")

            input.addEventListener("change", {
                val file = input.asDynamic().files[0]
                if (file == null) { continuation.resume(null); return@addEventListener }
                val reader = js("new FileReader()")
                reader.onload = {
                    val text = reader.result?.unsafeCast<String>()
                    continuation.resume(text)
                }
                reader.onerror = { continuation.resume(null) }
                reader.readAsText(file)
            }, false)

            input.asDynamic().click()
        }
    }

    override suspend fun saveAndWrite(defaultName: String, content: String): Boolean {
        try {
            val encoded = window.btoa(content)
            val anchor = document.createElement("a")
            anchor.setAttribute("href", "data:text/markdown;base64," + encoded)
            anchor.setAttribute("download", defaultName)
            anchor.asDynamic().style.display = "none"
            document.body?.appendChild(anchor)
            anchor.asDynamic().click()
            document.body?.removeChild(anchor)
            return true
        } catch (_: Exception) {
            return false
        }
    }
}
