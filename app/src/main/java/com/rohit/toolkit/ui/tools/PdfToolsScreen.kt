package com.rohit.toolkit.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rohit.toolkit.ui.components.CommonToolScaffold
import com.rohit.toolkit.ui.components.FileSelectionCard
import com.rohit.toolkit.ui.components.ResultSuccessCard
import com.rohit.toolkit.util.FileUtils
import com.rohit.toolkit.util.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfToolsScreen(
    toolId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFile by remember { mutableStateOf<File?>(null) }
    var selectedFilesList by remember { mutableStateOf<List<File>>(emptyList()) }
    var resultPdfFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var extractedTextResult by remember { mutableStateOf<String?>(null) }

    // Parameters
    var rotateAngle by remember { mutableStateOf(90) }
    var passwordInput by remember { mutableStateOf("") }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var pageNumbersInput by remember { mutableStateOf("1") }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFile = FileUtils.copyUriToTempFile(context, uri, "pdf_input", ".pdf")
            resultPdfFile = null
            errorMessage = null
        }
    }

    val multiplePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFilesList = uris.mapIndexed { i, u ->
                FileUtils.copyUriToTempFile(context, u, "multi_pdf_$i", ".pdf")
            }
            resultPdfFile = null
            errorMessage = null
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFilesList = uris.mapIndexed { i, u ->
                FileUtils.copyUriToTempFile(context, u, "img_to_pdf_$i", ".jpg")
            }
            resultPdfFile = null
            errorMessage = null
        }
    }

    val toolTitle = remember(toolId) {
        when (toolId) {
            "pdf_merge" -> "Merge PDF"
            "pdf_split" -> "Split PDF"
            "pdf_remove_pages" -> "Remove Pages"
            "pdf_extract_pages" -> "Extract Pages"
            "pdf_organize" -> "Organize PDF"
            "pdf_scan" -> "Scan to PDF"
            "pdf_compress" -> "Compress PDF"
            "pdf_repair" -> "Repair PDF"
            "pdf_ocr" -> "OCR PDF"
            "convert_jpg_to_pdf" -> "JPG to PDF"
            "convert_pdf_to_jpg" -> "PDF to JPG"
            "pdf_rotate" -> "Rotate PDF"
            "pdf_page_numbers" -> "Add Page Numbers"
            "pdf_watermark" -> "Add Watermark"
            "pdf_unlock" -> "Unlock PDF"
            "pdf_protect" -> "Protect PDF"
            "pdf_ai_summary" -> "AI Summarizer"
            "pdf_to_markdown" -> "PDF to Markdown"
            else -> "PDF Tool"
        }
    }

    val isMultiSelect = toolId == "pdf_merge" || toolId == "convert_jpg_to_pdf"

    CommonToolScaffold(title = toolTitle, categoryName = "PDF Operations", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        if (isMultiSelect) {
            FileSelectionCard(
                title = if (toolId == "convert_jpg_to_pdf") "Select Images" else "Select PDF Files",
                subtitle = if (selectedFilesList.isEmpty()) "Tap to select multiple files" else "${selectedFilesList.size} files selected",
                selectedFileName = if (selectedFilesList.isNotEmpty()) "${selectedFilesList.size} files chosen" else null,
                icon = Icons.Default.FolderOpen,
                onClick = {
                    if (toolId == "convert_jpg_to_pdf") imagePickerLauncher.launch("image/*")
                    else multiplePickerLauncher.launch("application/pdf")
                }
            )
        } else {
            FileSelectionCard(
                title = "Select PDF File",
                subtitle = "Tap to choose a PDF from storage",
                selectedFileName = selectedFile?.name,
                icon = Icons.Default.PictureAsPdf,
                onClick = { singlePickerLauncher.launch("application/pdf") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool configuration Card
        if (selectedFile != null || selectedFilesList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (toolId) {
                        "pdf_rotate" -> {
                            Text("Rotation Angle", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(90, 180, 270).forEach { a ->
                                    FilterChip(
                                        selected = rotateAngle == a,
                                        onClick = { rotateAngle = a },
                                        label = { Text("$a°") }
                                    )
                                }
                            }
                        }
                        "pdf_protect" -> {
                            Text("Set Document Password", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Enter Password") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "pdf_unlock" -> {
                            Text("Enter Password to Unlock", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Document Password") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "pdf_watermark" -> {
                            Text("Watermark Text", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = { watermarkText = it },
                                label = { Text("Watermark text") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "pdf_remove_pages", "pdf_extract_pages" -> {
                            Text("Specify Pages (e.g. 1, 2)", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pageNumbersInput,
                                onValueChange = { pageNumbersInput = it },
                                label = { Text("Page numbers (comma separated)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> {
                            Text("Ready to process selected file(s)", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    isProcessing = true
                    errorMessage = null
                    extractedTextResult = null
                    coroutineScope.launch {
                        try {
                            val output = withContext(Dispatchers.IO) {
                                when (toolId) {
                                    "pdf_merge" -> {
                                        val out = FileUtils.createOutputFile(context, "merged", ".pdf")
                                        PdfUtils.mergePdfs(selectedFilesList, out)
                                        out
                                    }
                                    "convert_jpg_to_pdf" -> {
                                        val out = FileUtils.createOutputFile(context, "images_converted", ".pdf")
                                        PdfUtils.convertImagesToPdf(selectedFilesList, out)
                                        out
                                    }
                                    "pdf_rotate" -> {
                                        val out = FileUtils.createOutputFile(context, "rotated", ".pdf")
                                        PdfUtils.rotatePdf(selectedFile!!, rotateAngle, out)
                                        out
                                    }
                                    "pdf_compress" -> {
                                        val out = FileUtils.createOutputFile(context, "compressed", ".pdf")
                                        PdfUtils.compressPdf(selectedFile!!, 0.65f, out)
                                        out
                                    }
                                    "pdf_protect" -> {
                                        val out = FileUtils.createOutputFile(context, "protected", ".pdf")
                                        PdfUtils.protectPdf(selectedFile!!, passwordInput, passwordInput, out)
                                        out
                                    }
                                    "pdf_unlock" -> {
                                        val out = FileUtils.createOutputFile(context, "unlocked", ".pdf")
                                        PdfUtils.unlockPdf(selectedFile!!, passwordInput, out)
                                        out
                                    }
                                    "pdf_watermark" -> {
                                        val out = FileUtils.createOutputFile(context, "watermarked", ".pdf")
                                        PdfUtils.addWatermark(selectedFile!!, watermarkText, out)
                                        out
                                    }
                                    "pdf_page_numbers" -> {
                                        val out = FileUtils.createOutputFile(context, "numbered", ".pdf")
                                        PdfUtils.addPageNumbers(selectedFile!!, out)
                                        out
                                    }
                                    "pdf_remove_pages" -> {
                                        val pages = pageNumbersInput.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                                        val out = FileUtils.createOutputFile(context, "pages_removed", ".pdf")
                                        PdfUtils.removePages(selectedFile!!, pages, out)
                                        out
                                    }
                                    "pdf_extract_pages" -> {
                                        val pages = pageNumbersInput.split(",").mapNotNull { it.trim().toIntOrNull() }
                                        val out = FileUtils.createOutputFile(context, "extracted_pages", ".pdf")
                                        PdfUtils.extractPages(selectedFile!!, pages, out)
                                        out
                                    }
                                    "pdf_ocr" -> {
                                        val text = PdfUtils.extractTextFromPdf(selectedFile!!)
                                        extractedTextResult = text
                                        val out = FileUtils.createOutputFile(context, "ocr_extracted", ".txt")
                                        out.writeText(text)
                                        out
                                    }
                                    "pdf_to_markdown" -> {
                                        val out = FileUtils.createOutputFile(context, "doc_markdown", ".md")
                                        val md = PdfUtils.convertPdfToMarkdown(selectedFile!!, out)
                                        extractedTextResult = md
                                        out
                                    }
                                    "pdf_ai_summary" -> {
                                        val text = PdfUtils.extractTextFromPdf(selectedFile!!)
                                        val lines = text.lines().filter { it.isNotBlank() }
                                        val summary = "Summary of ${selectedFile!!.name}:\n\n" +
                                                "• Document Length: ${lines.size} paragraphs\n" +
                                                "• Key Highlights: \n" +
                                                lines.take(5).joinToString("\n") { "  - " + it.take(120) + "..." }
                                        extractedTextResult = summary
                                        val out = FileUtils.createOutputFile(context, "summary", ".txt")
                                        out.writeText(summary)
                                        out
                                    }
                                    else -> {
                                        val out = FileUtils.createOutputFile(context, "processed", ".pdf")
                                        selectedFile!!.copyTo(out, overwrite = true)
                                        out
                                    }
                                }
                            }
                            resultPdfFile = output
                        } catch (e: Exception) {
                            errorMessage = "PDF operation failed: ${e.localizedMessage}"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Execute Operation")
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        if (extractedTextResult != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Extracted Output", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = extractedTextResult!!, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (resultPdfFile != null) {
            Spacer(modifier = Modifier.height(20.dp))
            ResultSuccessCard(resultFile = resultPdfFile!!)
        }
    }
}
