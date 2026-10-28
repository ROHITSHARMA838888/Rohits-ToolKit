package com.rohit.toolkit.ui.tools

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import com.rohit.toolkit.ui.components.*
import com.rohit.toolkit.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var fileList by remember { mutableStateOf(FileUtils.listProcessedFiles(context)) }

    var fileToRename by remember { mutableStateOf<File?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var fileToDelete by remember { mutableStateOf<File?>(null) }

    fun refreshFiles() {
        fileList = FileUtils.listProcessedFiles(context)
    }

    CommonToolScaffold(title = "File Manager", categoryName = "File Operations", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Processed Files (${fileList.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { refreshFiles() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (fileList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No files processed yet",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Files saved by Rohit's ToolKit will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            fileList.forEach { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (file.name.endsWith(".pdf", true)) Icons.Default.PictureAsPdf
                                else if (file.name.endsWith(".zip", true)) Icons.Default.FolderZip
                                else Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1
                                )
                                Text(
                                    text = formatFileSize(file.length()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { openFileWithIntent(context, file) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text("Open", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { shareFileWithIntent(context, file) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text("Share", fontSize = 12.sp)
                            }

                            IconButton(onClick = {
                                fileToRename = file
                                renameInput = file.name
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            IconButton(onClick = { fileToDelete = file }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (fileToRename != null) {
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New file name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameInput.isNotBlank()) {
                        val newFile = File(fileToRename!!.parentFile, renameInput.trim())
                        fileToRename!!.renameTo(newFile)
                        refreshFiles()
                    }
                    fileToRename = null
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to permanently delete '${fileToDelete!!.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        fileToDelete!!.delete()
                        refreshFiles()
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
