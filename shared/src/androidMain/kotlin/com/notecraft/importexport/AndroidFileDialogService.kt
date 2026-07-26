package com.notecraft.importexport

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.notecraft.data.importexport.FileDialogService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidFileDialogService(private val activity: Activity) : FileDialogService {

    private var openContinuation: kotlin.coroutines.Continuation<Uri?>? = null
    private var saveContinuation: kotlin.coroutines.Continuation<Boolean>? = null

    override suspend fun openAndRead(): String? {
        val uri = suspendCancellableCoroutine<Uri?> { cont ->
            openContinuation = cont
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/markdown", "text/plain", "text/x-markdown"))
            }
            activity.startActivityForResult(intent, REQUEST_OPEN)
        }
        if (uri == null) return null
        return try {
            activity.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (_: Exception) { null }
    }

    override suspend fun saveAndWrite(defaultName: String, content: String): Boolean {
        return suspendCancellableCoroutine { cont ->
            saveContinuation = cont
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/markdown"
                putExtra(Intent.EXTRA_TITLE, defaultName)
            }
            activity.startActivityForResult(intent, REQUEST_SAVE)
        }
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            openContinuation?.resume(null); openContinuation = null
            saveContinuation?.resume(false); saveContinuation = null
            return
        }
        when (requestCode) {
            REQUEST_OPEN -> {
                openContinuation?.resume(data?.data); openContinuation = null
            }
            REQUEST_SAVE -> {
                saveContinuation?.resume(true); saveContinuation = null
            }
        }
    }

    companion object {
        const val REQUEST_OPEN = 1001
        const val REQUEST_SAVE = 1002
    }
}
