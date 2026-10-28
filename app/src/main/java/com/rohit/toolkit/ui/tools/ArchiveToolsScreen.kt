package com.rohit.toolkit.ui.tools

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohit.toolkit.ui.components.CommonToolScaffold
import com.rohit.toolkit.ui.components.FileSelectionCard
import com.rohit.toolkit.ui.components.ResultSuccessCard
import com.rohit.toolkit.ui.components.formatFileSize
import com.rohit.toolkit.util.ArchiveEntryInfo
import com.rohit.toolkit.util.ArchiveUtils
import com.rohit.toolkit.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ArchiveToolsScreen(
    toolId: String,
    onBack: () -> Unit
) {
    when (toolId) {
        "archive_zip" -> CreateZipScreen(onBack)
        "archive_extract" -> ExtractZipScreen(onBack)
        "archive_inspect" -> InspectZipScreen(onBack)
        else -> CreateZipScreen(onBack)
    }
}

// 1. Create ZIP Archive
@Composable
fun CreateZipScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var resultZipFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressPercent by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = uris.mapIndexed { idx, uri ->
                FileUtils.copyUriToTempFile(context, uri, "zip_input_$idx", ".tmp")
            }
        }
    }

    CommonToolScaffold(title = "Create ZIP Archive", categoryName = "Archive & Compression", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        FileSelectionCard(
            title = "Select Files to Compress",
            subtitle = if (selectedFiles.isEmpty()) "Tap to select multiple files" else "${selectedFiles.size} files selected",
            selectedFileName = if (selectedFiles.isNotEmpty()) "${selectedFiles.size} files chosen" else null,
            icon = Icons.Default.FolderZip,
            onClick = { pickerLauncher.launch("*/*") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedFiles.isNotEmpty()) {
            Button(
                onClick = {
                    isProcessing = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val outputZip = withContext(Dispatchers.IO) {
                                val outFile = FileUtils.createOutputFile(context, "archive", ".zip")
                                ArchiveUtils.createZip(selectedFiles, outFile) { prog ->
                                    progressPercent = prog
                                }
                                outFile
                            }
                            resultZipFile = outputZip
                        } catch (e: Exception) {
                            errorMessage = "Compression failed: ${e.localizedMessage}"
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compressing... $progressPercent%")
                } else {
                    Text("Compress to ZIP")
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        if (resultZipFile != null) {
            Spacer(modifier = Modifier.height(20.dp))
            ResultSuccessCard(resultFile = resultZipFile!!)
        }
    }
}

// 2. Extract ZIP Archive
@Composable
fun ExtractZipScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedZip by remember { mutableStateOf<File?>(null) }
    var extractedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedZip = FileUtils.copyUriToTempFile(context, uri, "extract_src", ".zip")
        }
    }

    CommonToolScaffold(title = "Extract ZIP Archive", categoryName = "Archive & Compression", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        FileSelectionCard(
            title = "Select ZIP Archive",
            subtitle = "Extract securely with Path Traversal & Bomb protection",
            selectedFileName = selectedZip?.name,
            icon = Icons.Default.Unarchive,
            onClick = { pickerLauncher.launch("application/zip") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedZip != null) {
            Button(
                onClick = {
                    isProcessing = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val targetDir = File(context.filesDir, "extracted_${System.currentTimeMillis()}")
                            val files = withContext(Dispatchers.IO) {
                                ArchiveUtils.extractZip(selectedZip!!, targetDir) {}
                            }
                            extractedFiles = files
                        } catch (e: Exception) {
                            errorMessage = "Extraction failed: ${e.localizedMessage}"
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
                    Text("Extract All Files")
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        if (extractedFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Extracted ${extractedFiles.size} File(s)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    extractedFiles.take(10).forEach { f ->
                        Text("• ${f.name} (${formatFileSize(f.length())})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// 3. Inspect ZIP Contents
@Composable
fun InspectZipScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedZip by remember { mutableStateOf<File?>(null) }
    var entries by remember { mutableStateOf<List<ArchiveEntryInfo>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val file = FileUtils.copyUriToTempFile(context, uri, "inspect_src", ".zip")
            selectedZip = file
            try {
                entries = ArchiveUtils.inspectZip(file)
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Failed to inspect ZIP: ${e.localizedMessage}"
            }
        }
    }

    CommonToolScaffold(title = "Inspect ZIP Archive", categoryName = "Archive & Compression", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        FileSelectionCard(
            title = "Select ZIP to Inspect",
            subtitle = "Examine file headers, uncompressed size without extracting",
            selectedFileName = selectedZip?.name,
            icon = Icons.Default.FindInPage,
            onClick = { pickerLauncher.launch("application/zip") }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        if (entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Archive Contents (${entries.size} entries)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    entries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = (if (entry.isDirectory) "📁 " else "📄 ") + entry.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            if (!entry.isDirectory && entry.size >= 0) {
                                Text(
                                    text = formatFileSize(entry.size),
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}
