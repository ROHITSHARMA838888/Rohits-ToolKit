package com.rohit.toolkit.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.multipdf.Splitter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    // 1. Merge PDF
    fun mergePdfs(pdfFiles: List<File>, outputFile: File) {
        val merger = PDFMergerUtility()
        merger.destinationFileName = outputFile.absolutePath
        for (file in pdfFiles) {
            merger.addSource(file)
        }
        merger.mergeDocuments(null)
    }

    // 2. Split PDF
    fun splitPdf(sourcePdf: File, outputDir: File): List<File> {
        val document = PDDocument.load(sourcePdf)
        val splitter = Splitter()
        val pages = splitter.split(document)
        val resultFiles = mutableListOf<File>()

        pages.forEachIndexed { index, doc ->
            val pageFile = File(outputDir, "page_${index + 1}.pdf")
            doc.save(pageFile)
            doc.close()
            resultFiles.add(pageFile)
        }
        document.close()
        return resultFiles
    }

    // 3. Remove Pages (1-based indices)
    fun removePages(sourcePdf: File, pagesToRemove: Set<Int>, outputFile: File) {
        val document = PDDocument.load(sourcePdf)
        val total = document.numberOfPages
        // Remove backwards to avoid shift errors
        for (pageIndex in total downTo 1) {
            if (pagesToRemove.contains(pageIndex)) {
                document.removePage(pageIndex - 1)
            }
        }
        document.save(outputFile)
        document.close()
    }

    // 4. Extract Pages (1-based indices)
    fun extractPages(sourcePdf: File, pagesToExtract: List<Int>, outputFile: File) {
        val sourceDoc = PDDocument.load(sourcePdf)
        val newDoc = PDDocument()
        for (pageNum in pagesToExtract) {
            if (pageNum in 1..sourceDoc.numberOfPages) {
                newDoc.addPage(sourceDoc.getPage(pageNum - 1))
            }
        }
        newDoc.save(outputFile)
        newDoc.close()
        sourceDoc.close()
    }

    // 5. Rotate PDF Pages
    fun rotatePdf(sourcePdf: File, angle: Int, outputFile: File) {
        val document = PDDocument.load(sourcePdf)
        for (page in document.pages) {
            val currentRotation = page.rotation
            page.rotation = (currentRotation + angle) % 360
        }
        document.save(outputFile)
        document.close()
    }

    // 6. Protect / Encrypt PDF
    fun protectPdf(sourcePdf: File, userPass: String, ownerPass: String, outputFile: File) {
        val document = PDDocument.load(sourcePdf)
        val ap = AccessPermission()
        val spp = StandardProtectionPolicy(ownerPass, userPass, ap)
        spp.encryptionKeyLength = 128
        document.protect(spp)
        document.save(outputFile)
        document.close()
    }

    // 7. Unlock PDF
    fun unlockPdf(sourcePdf: File, password: String, outputFile: File) {
        val document = PDDocument.load(sourcePdf, password)
        document.isAllSecurityToBeRemoved = true
        document.save(outputFile)
        document.close()
    }

    // 8. Add Watermark to PDF
    fun addWatermark(sourcePdf: File, watermarkText: String, outputFile: File) {
        val document = PDDocument.load(sourcePdf)
        for (page in document.pages) {
            val contentStream = PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
            )
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36f)
            contentStream.setNonStrokingColor(180, 180, 180)
            val mediaBox = page.mediaBox
            val x = mediaBox.width / 4f
            val y = mediaBox.height / 2f
            contentStream.newLineAtOffset(x, y)
            contentStream.showText(watermarkText)
            contentStream.endText()
            contentStream.close()
        }
        document.save(outputFile)
        document.close()
    }

    // 9. Add Page Numbers
    fun addPageNumbers(sourcePdf: File, outputFile: File) {
        val document = PDDocument.load(sourcePdf)
        val total = document.numberOfPages
        for (i in 0 until total) {
            val page = document.getPage(i)
            val contentStream = PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
            )
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.setNonStrokingColor(80, 80, 80)
            val text = "Page ${i + 1} of $total"
            val mediaBox = page.mediaBox
            contentStream.newLineAtOffset(mediaBox.width / 2f - 25f, 25f)
            contentStream.showText(text)
            contentStream.endText()
            contentStream.close()
        }
        document.save(outputFile)
        document.close()
    }

    // 10. Extract Text / OCR PDF
    fun extractTextFromPdf(sourcePdf: File): String {
        val document = PDDocument.load(sourcePdf)
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        document.close()
        return text
    }

    // 11. Images to PDF (JPG to PDF)
    fun convertImagesToPdf(imageFiles: List<File>, outputFile: File) {
        val document = PDDocument()
        for (imgFile in imageFiles) {
            val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath) ?: continue
            val page = PDPage(PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat()))
            document.addPage(page)

            val pdImage = JPEGFactory.createFromImage(document, bitmap, 0.85f)
            val contentStream = PDPageContentStream(document, page)
            contentStream.drawImage(pdImage, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            contentStream.close()
        }
        document.save(outputFile)
        document.close()
    }

    // 12. PDF to Images (PDF to JPG)
    fun convertPdfToImages(sourcePdf: File, outputDir: File): List<File> {
        val document = PDDocument.load(sourcePdf)
        val renderer = PDFRenderer(document)
        val imageFiles = mutableListOf<File>()

        for (page in 0 until document.numberOfPages) {
            val bitmap = renderer.renderImageWithDPI(page, 150f)
            val imgFile = File(outputDir, "page_${page + 1}.jpg")
            FileOutputStream(imgFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            imageFiles.add(imgFile)
        }
        document.close()
        return imageFiles
    }

    // 13. Compress PDF (renders pages with optimized JPEG compression)
    fun compressPdf(sourcePdf: File, quality: Float, outputFile: File) {
        val sourceDoc = PDDocument.load(sourcePdf)
        val renderer = PDFRenderer(sourceDoc)
        val compressedDoc = PDDocument()

        for (page in 0 until sourceDoc.numberOfPages) {
            val bitmap = renderer.renderImageWithDPI(page, 120f)
            val newPage = PDPage(PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat()))
            compressedDoc.addPage(newPage)

            val pdImage = JPEGFactory.createFromImage(compressedDoc, bitmap, quality)
            val contentStream = PDPageContentStream(compressedDoc, newPage)
            contentStream.drawImage(pdImage, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            contentStream.close()
        }
        compressedDoc.save(outputFile)
        compressedDoc.close()
        sourceDoc.close()
    }

    // 14. Convert PDF to Markdown
    fun convertPdfToMarkdown(sourcePdf: File, outputFile: File): String {
        val rawText = extractTextFromPdf(sourcePdf)
        val lines = rawText.lines()
        val sb = StringBuilder()

        sb.append("# ${sourcePdf.nameWithoutExtension}\n\n")
        var inParagraph = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                if (inParagraph) {
                    sb.append("\n\n")
                    inParagraph = false
                }
            } else if (trimmed.length < 50 && (trimmed.all { it.isUpperCase() || it.isWhitespace() } || !trimmed.endsWith("."))) {
                sb.append("## $trimmed\n\n")
                inParagraph = false
            } else {
                sb.append("$trimmed ")
                inParagraph = true
            }
        }

        val mdContent = sb.toString()
        outputFile.writeText(mdContent)
        return mdContent
    }
}
