package com.rohit.toolkit.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown_file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.SIZE)
                if (index != -1) {
                    size = it.getLong(index)
                }
            }
        }
        return size
    }

    fun copyUriToTempFile(context: Context, uri: Uri, prefix: String = "temp", suffix: String = ".tmp"): File {
        val tempDir = File(context.cacheDir, "input_temps").apply { if (!exists()) mkdirs() }
        val tempFile = File.createTempFile(prefix, suffix, tempDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    fun createOutputFile(context: Context, prefix: String, extension: String): File {
        val toolsDir = File(context.filesDir, "processed_tools").apply { if (!exists()) mkdirs() }
        val timestamp = System.currentTimeMillis()
        val ext = if (extension.startsWith(".")) extension else ".$extension"
        return File(toolsDir, "${prefix}_$timestamp$ext")
    }

    fun listProcessedFiles(context: Context): List<File> {
        val toolsDir = File(context.filesDir, "processed_tools")
        if (!toolsDir.exists()) return emptyList()
        return toolsDir.listFiles()?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }
}
