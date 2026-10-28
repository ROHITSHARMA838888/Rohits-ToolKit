package com.rohit.toolkit

import com.rohit.toolkit.util.ArchiveUtils
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ArchiveSecurityTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testNormalZipCreateAndExtract() {
        val testFile1 = tempFolder.newFile("sample1.txt").apply { writeText("Hello Rohit ToolKit") }
        val testFile2 = tempFolder.newFile("sample2.txt").apply { writeText("Sample Content 2") }

        val zipFile = File(tempFolder.root, "test_output.zip")
        ArchiveUtils.createZip(listOf(testFile1, testFile2), zipFile) { _ -> }

        assertTrue(zipFile.exists())
        assertTrue(zipFile.length() > 0)

        // Inspect zip
        val entries = ArchiveUtils.inspectZip(zipFile)
        assertEquals(2, entries.size)

        // Extract zip
        val extractTarget = tempFolder.newFolder("extracted")
        val extractedFiles = ArchiveUtils.extractZip(zipFile, extractTarget) { _ -> }
        assertEquals(2, extractedFiles.size)
    }

    @Test
    fun testZipSlipPathTraversalBlocked() {
        // Construct a malicious ZIP file containing an entry with "../escaped.txt"
        val maliciousZip = File(tempFolder.root, "malicious.zip")
        ZipOutputStream(FileOutputStream(maliciousZip)).use { zos ->
            val evilEntry = ZipEntry("../escaped.txt")
            zos.putNextEntry(evilEntry)
            zos.write("Malicious payload".toByteArray())
            zos.closeEntry()
        }

        val extractTarget = tempFolder.newFolder("safe_sandbox")
        try {
            ArchiveUtils.extractZip(maliciousZip, extractTarget) { _ -> }
            fail("Expected SecurityException for Zip-Slip traversal!")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("path traversal", ignoreCase = true) == true)
        }
    }
}
