package com.rohit.toolkit.ui.tools

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.rohit.toolkit.ui.components.CommonToolScaffold
import com.rohit.toolkit.ui.components.FileSelectionCard
import com.rohit.toolkit.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MlKitToolsScreen(
    toolId: String,
    onBack: () -> Unit
) {
    when (toolId) {
        "ml_text_ocr" -> MlTextOcrScreen(onBack)
        "ml_qr_barcode" -> MlQrBarcodeScreen(onBack)
        "ml_face_detect" -> MlFaceDetectScreen(onBack)
        "ml_translation" -> MlTranslationScreen(onBack)
        "ml_lang_id" -> MlLanguageIdScreen(onBack)
        "ml_doc_scanner" -> MlDocScannerScreen(onBack)
        else -> MlTextOcrScreen(onBack)
    }
}

// 1. Bundled Text Recognition / OCR
@Composable
fun MlTextOcrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFile = FileUtils.copyUriToTempFile(context, uri, "ocr_img", ".jpg")
        }
    }

    CommonToolScaffold(title = "Text Recognition / OCR", categoryName = "Google ML Kit", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        FileSelectionCard(
            title = "Select Image for OCR",
            subtitle = "Offline Latin text extraction from photos",
            selectedFileName = selectedFile?.name,
            icon = Icons.Default.DocumentScanner,
            onClick = { pickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedFile != null) {
            Button(
                onClick = {
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val bitmap = BitmapFactory.decodeFile(selectedFile!!.absolutePath)
                            val image = InputImage.fromBitmap(bitmap, 0)
                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    isProcessing = false
                                    recognizedText = if (visionText.text.isBlank()) "No text found in image" else visionText.text
                                }
                                .addOnFailureListener { e ->
                                    isProcessing = false
                                    recognizedText = "OCR Failed: ${e.localizedMessage}"
                                }
                        } catch (e: Exception) {
                            isProcessing = false
                            recognizedText = "Error: ${e.localizedMessage}"
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
                    Text("Extract Text (Offline OCR)")
                }
            }
        }

        if (recognizedText != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recognized Text", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("OCR Result", recognizedText))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recognizedText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// 2. Bundled QR & Barcode Scanner
@Composable
fun MlQrBarcodeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFile = FileUtils.copyUriToTempFile(context, uri, "barcode_img", ".jpg")
        }
    }

    CommonToolScaffold(title = "QR & Barcode Scanner", categoryName = "Google ML Kit", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        FileSelectionCard(
            title = "Select Barcode or QR Image",
            subtitle = "Offline scan for QR, Aztec, EAN, UPC, Code 128",
            selectedFileName = selectedFile?.name,
            icon = Icons.Default.QrCodeScanner,
            onClick = { pickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedFile != null) {
            Button(
                onClick = {
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val bitmap = BitmapFactory.decodeFile(selectedFile!!.absolutePath)
                            val image = InputImage.fromBitmap(bitmap, 0)
                            val scanner = BarcodeScanning.getClient()

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    isProcessing = false
                                    if (barcodes.isNotEmpty()) {
                                        val first = barcodes[0]
                                        scannedResult = "Format: ${first.format}\nValue: ${first.rawValue ?: first.displayValue ?: "N/A"}"
                                    } else {
                                        scannedResult = "No QR code or barcode detected."
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isProcessing = false
                                    scannedResult = "Scan failed: ${e.localizedMessage}"
                                }
                        } catch (e: Exception) {
                            isProcessing = false
                            scannedResult = "Error: ${e.localizedMessage}"
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
                    Text("Scan Barcode")
                }
            }
        }

        if (scannedResult != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scan Result", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = scannedResult!!, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// 3. Face Detection
@Composable
fun MlFaceDetectScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var faceCountInfo by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFile = FileUtils.copyUriToTempFile(context, uri, "face_img", ".jpg")
        }
    }

    CommonToolScaffold(title = "Face Detection", categoryName = "Google ML Kit", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        FileSelectionCard(
            title = "Select Photo for Face Detection",
            subtitle = "Detect faces, bounds, smile and head orientation",
            selectedFileName = selectedFile?.name,
            icon = Icons.Default.Face,
            onClick = { pickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedFile != null) {
            Button(
                onClick = {
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val bitmap = BitmapFactory.decodeFile(selectedFile!!.absolutePath)
                            val image = InputImage.fromBitmap(bitmap, 0)
                            val options = FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                .build()
                            val detector = FaceDetection.getClient(options)

                            detector.process(image)
                                .addOnSuccessListener { faces ->
                                    isProcessing = false
                                    faceCountInfo = "Detected ${faces.size} face(s) in image.\n" +
                                            faces.mapIndexed { idx, f ->
                                                "• Face ${idx + 1}: Bounds [${f.boundingBox.left}, ${f.boundingBox.top}, ${f.boundingBox.right}, ${f.boundingBox.bottom}]" +
                                                        (f.smilingProbability?.let { String.format(" | Smile: %.0f%%", it * 100) } ?: "")
                                            }.joinToString("\n")
                                }
                                .addOnFailureListener { e ->
                                    isProcessing = false
                                    faceCountInfo = "Detection failed: ${e.localizedMessage}"
                                }
                        } catch (e: Exception) {
                            isProcessing = false
                            faceCountInfo = "Error: ${e.localizedMessage}"
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
                    Text("Detect Faces")
                }
            }
        }

        if (faceCountInfo != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detection Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = faceCountInfo!!, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// 4. Text Translation with Auto Model Delivery
@Composable
fun MlTranslationScreen(onBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    CommonToolScaffold(title = "Text Translation", categoryName = "Google ML Kit", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("English -> Hindi (Automatic Model Download)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter English text to translate") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (inputText.isBlank()) return@Button
                isProcessing = true
                statusMessage = "Checking on-device model availability..."

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.HINDI)
                    .build()
                val translator = Translation.getClient(options)
                val conditions = DownloadConditions.Builder().build()

                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        statusMessage = "Translating text locally..."
                        translator.translate(inputText)
                            .addOnSuccessListener { result ->
                                isProcessing = false
                                statusMessage = null
                                translatedText = result
                                translator.close()
                            }
                            .addOnFailureListener { e ->
                                isProcessing = false
                                statusMessage = "Translation failed: ${e.localizedMessage}"
                                translator.close()
                            }
                    }
                    .addOnFailureListener { e ->
                        isProcessing = false
                        statusMessage = "Model download error: ${e.localizedMessage} (Requires Internet for initial download)"
                        translator.close()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isProcessing && inputText.isNotBlank()
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(statusMessage ?: "Processing...")
            } else {
                Text("Translate Text")
            }
        }

        if (statusMessage != null && !isProcessing) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(statusMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        if (translatedText != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Translated Result", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = translatedText!!, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// 5. Language Identification
@Composable
fun MlLanguageIdScreen(onBack: () -> Unit) {
    var textInput by remember { mutableStateOf("") }
    var detectedLanguage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    CommonToolScaffold(title = "Language Identification", categoryName = "Google ML Kit", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Paste any text snippet to identify its language") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (textInput.isBlank()) return@Button
                isProcessing = true
                val languageIdentifier = LanguageIdentification.getClient()
                languageIdentifier.identifyLanguage(textInput)
                    .addOnSuccessListener { languageCode ->
                        isProcessing = false
                        detectedLanguage = if (languageCode == "und") {
                            "Unable to identify language"
                        } else {
                            "Detected Language Code: '$languageCode'"
                        }
                    }
                    .addOnFailureListener { e ->
                        isProcessing = false
                        detectedLanguage = "Error: ${e.localizedMessage}"
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isProcessing && textInput.isNotBlank()
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text("Identify Language")
            }
        }

        if (detectedLanguage != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Result", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = detectedLanguage!!, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// 6. ML Kit Document Scanner
@Composable
fun MlDocScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var scanStatus by remember { mutableStateOf<String?>(null) }
    var scannedPagesCount by remember { mutableStateOf(0) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pages = scanResult?.pages?.size ?: 0
            val pdfUri = scanResult?.pdf?.uri
            scannedPagesCount = pages
            scanStatus = if (pdfUri != null) {
                "Successfully scanned $pages page(s) into PDF!"
            } else {
                "Scanned $pages page(s)."
            }
        } else {
            scanStatus = "Scan cancelled or closed."
        }
    }

    CommonToolScaffold(title = "Document Scanner", categoryName = "Google ML Kit", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Official ML Kit Document Scanner",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Provides automatic document edge detection, perspective correction, shadow removal, and cleans up documents into PDF format automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val options = GmsDocumentScannerOptions.Builder()
                    .setGalleryImportAllowed(true)
                    .setPageLimit(15)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                    .build()

                val scanner = GmsDocumentScanning.getClient(options)
                val activity = context as? Activity
                if (activity != null) {
                    scanner.getStartScanIntent(activity)
                        .addOnSuccessListener { intentSender ->
                            scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        }
                        .addOnFailureListener { e ->
                            scanStatus = "Scanner failed to launch: ${e.localizedMessage}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Document Scanner")
        }

        if (scanStatus != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scanner Status", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = scanStatus!!, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
