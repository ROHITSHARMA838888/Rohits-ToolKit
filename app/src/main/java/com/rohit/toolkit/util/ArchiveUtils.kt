package com.rohit.toolkit.util

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ArchiveEntryInfo(
    val name: String,
    val size: Long,
    val compressedSize: Long,
    val isDirectory: Boolean
)

object ArchiveUtils {

    private const val MAX_TOTAL_EXTRACTED_BYTES = 500L * 1024L * 1024L // 500 MB max bomb protection
    private const val MAX_ENTRY_COUNT = 5000 // 5000 entries max

    // 1. Inspect ZIP without extracting
    fun inspectZip(zipFile: File): List<ArchiveEntryInfo> {
        val entries = mutableListOf<ArchiveEntryInfo>()
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                entries.add(
                    ArchiveEntryInfo(
                        name = entry.name,
                        size = entry.size,
                        compressedSize = entry.compressedSize,
                        isDirectory = entry.isDirectory
                    )
                )
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return entries
    }

    // 2. Create ZIP from multiple files
    fun createZip(filesToZip: List<File>, outputZipFile: File, onProgress: (Int) -> Unit) {
        val buffer = ByteArray(8 * 1024)
        ZipOutputStream(FileOutputStream(outputZipFile)).use { zos ->
            filesToZip.forEachIndexed { index, file ->
                if (file.exists() && file.isFile) {
                    FileInputStream(file).use { fis ->
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            zos.write(buffer, 0, bytesRead)
                        }
                        zos.closeEntry()
                    }
                }
                val progress = ((index + 1) * 100) / filesToZip.size
                onProgress(progress)
            }
        }
    }

    // 3. Extract ZIP with Zip-Slip & Zip-Bomb protection
    fun extractZip(zipFile: File, targetDir: File, onProgress: (Int) -> Unit): List<File> {
        if (!targetDir.exists()) targetDir.mkdirs()

        val extractedFiles = mutableListOf<File>()
        val buffer = ByteArray(8 * 1024)
        var totalBytesExtracted = 0L
        var totalEntries = 0

        val canonicalTargetDir = targetDir.canonicalFile

        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                totalEntries++
                if (totalEntries > MAX_ENTRY_COUNT) {
                    throw SecurityException("ZIP archive contains too many entries (possible ZIP bomb attack).")
                }

                val destinationFile = File(targetDir, entry.name)

                // Critical Zip-Slip protection
                if (!destinationFile.canonicalFile.toPath().startsWith(canonicalTargetDir.toPath())) {
                    throw SecurityException("Malicious path traversal detected in archive entry: ${entry.name}")
                }

                if (entry.isDirectory) {
                    destinationFile.mkdirs()
                } else {
                    destinationFile.parentFile?.mkdirs()
                    FileOutputStream(destinationFile).use { fos ->
                        var bytesRead: Int
                        while (zis.read(buffer).also { bytesRead = it } != -1) {
                            totalBytesExtracted += bytesRead
                            if (totalBytesExtracted > MAX_TOTAL_EXTRACTED_BYTES) {
                                throw SecurityException("Extracted size exceeded 500MB security limit (ZIP bomb protection).")
                            }
                            fos.write(buffer, 0, bytesRead)
                        }
                    }
                    extractedFiles.add(destinationFile)
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return extractedFiles
    }
}
