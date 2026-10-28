package com.rohit.toolkit.ui.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.rohit.toolkit.ui.components.CommonToolScaffold
import com.rohit.toolkit.ui.components.FileSelectionCard
import com.rohit.toolkit.ui.components.ResultSuccessCard
import com.rohit.toolkit.util.FileUtils
import com.rohit.toolkit.util.ImageProcessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageToolsScreen(
    toolId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var resultImageFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic tool options
    var qualitySlider by remember { mutableStateOf(75f) }
    var widthInput by remember { mutableStateOf("1080") }
    var heightInput by remember { mutableStateOf("1080") }
    var watermarkText by remember { mutableStateOf("Rohit's ToolKit") }
    var topMemeText by remember { mutableStateOf("") }
    var bottomMemeText by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("grayscale") }
    var rotateAngle by remember { mutableStateOf(90f) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageFile = FileUtils.copyUriToTempFile(context, uri, "img_src", ".jpg")
            resultImageFile = null
            errorMessage = null
        }
    }

    val toolTitle = remember(toolId) {
        when (toolId) {
            "img_compress" -> "Compress Image"
            "img_resize" -> "Resize Image"
            "img_crop" -> "Crop Image"
            "img_to_jpg" -> "Convert to JPG"
            "img_from_jpg" -> "Convert from JPG"
            "img_photo_editor" -> "Photo Editor"
            "img_upscale" -> "Upscale Image"
            "img_remove_bg" -> "Remove Background"
            "img_watermark" -> "Watermark Image"
            "img_meme_gen" -> "Meme Generator"
            "img_rotate" -> "Rotate Image"
            "img_html_to_image" -> "HTML to Image"
            "img_blur_face" -> "Blur Face"
            else -> "Image Tool"
        }
    }

    CommonToolScaffold(title = toolTitle, categoryName = "Image Tools", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        FileSelectionCard(
            title = "Select Image",
            subtitle = "Tap to choose a photo from device",
            selectedFileName = selectedImageFile?.name,
            icon = Icons.Default.Image,
            onClick = { pickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tool-specific configuration controls
        if (selectedImageFile != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (toolId) {
                        "img_compress" -> {
                            Text("Compression Quality: ${qualitySlider.toInt()}%", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = qualitySlider,
                                onValueChange = { qualitySlider = it },
                                valueRange = 10f..100f
                            )
                        }
                        "img_resize" -> {
                            Text("Resize Dimensions", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = widthInput,
                                    onValueChange = { widthInput = it },
                                    label = { Text("Width (px)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = heightInput,
                                    onValueChange = { heightInput = it },
                                    label = { Text("Height (px)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        "img_watermark" -> {
                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = { watermarkText = it },
                                label = { Text("Watermark Text") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "img_meme_gen" -> {
                            OutlinedTextField(
                                value = topMemeText,
                                onValueChange = { topMemeText = it },
                                label = { Text("Top Caption") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bottomMemeText,
                                onValueChange = { bottomMemeText = it },
                                label = { Text("Bottom Caption") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "img_photo_editor" -> {
                            Text("Select Filter Effect:", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("grayscale", "sepia", "invert").forEach { f ->
                                    FilterChip(
                                        selected = filterType == f,
                                        onClick = { filterType = f },
                                        label = { Text(f.uppercase()) }
                                    )
                                }
                            }
                        }
                        "img_rotate" -> {
                            Text("Rotation Angle", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(90f, 180f, 270f).forEach { angle ->
                                    FilterChip(
                                        selected = rotateAngle == angle,
                                        onClick = { rotateAngle = angle },
                                        label = { Text("${angle.toInt()}°") }
                                    )
                                }
                            }
                        }
                        else -> {
                            Text("Standard Processing Mode", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    isProcessing = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val output = withContext(Dispatchers.IO) {
                                when (toolId) {
                                    "img_compress" -> {
                                        val out = FileUtils.createOutputFile(context, "compressed", ".jpg")
                                        ImageProcessingUtils.compressImage(selectedImageFile!!, qualitySlider.toInt(), Bitmap.CompressFormat.JPEG, out)
                                        out
                                    }
                                    "img_resize" -> {
                                        val w = widthInput.toIntOrNull() ?: 800
                                        val h = heightInput.toIntOrNull() ?: 800
                                        val out = FileUtils.createOutputFile(context, "resized", ".jpg")
                                        ImageProcessingUtils.resizeImage(selectedImageFile!!, w, h, out)
                                        out
                                    }
                                    "img_crop" -> {
                                        val out = FileUtils.createOutputFile(context, "cropped", ".jpg")
                                        val original = BitmapFactory.decodeFile(selectedImageFile!!.absolutePath)
                                        val side = minOf(original.width, original.height)
                                        ImageProcessingUtils.cropImage(selectedImageFile!!, 0, 0, side, side, out)
                                        out
                                    }
                                    "img_to_jpg" -> {
                                        val out = FileUtils.createOutputFile(context, "converted", ".jpg")
                                        ImageProcessingUtils.convertFormat(selectedImageFile!!, Bitmap.CompressFormat.JPEG, out)
                                        out
                                    }
                                    "img_from_jpg" -> {
                                        val out = FileUtils.createOutputFile(context, "converted", ".png")
                                        ImageProcessingUtils.convertFormat(selectedImageFile!!, Bitmap.CompressFormat.PNG, out)
                                        out
                                    }
                                    "img_photo_editor" -> {
                                        val out = FileUtils.createOutputFile(context, "edited", ".jpg")
                                        ImageProcessingUtils.applyColorFilter(selectedImageFile!!, filterType, 1.0f, out)
                                        out
                                    }
                                    "img_upscale" -> {
                                        val out = FileUtils.createOutputFile(context, "upscaled", ".jpg")
                                        ImageProcessingUtils.upscaleImage(selectedImageFile!!, 2, out)
                                        out
                                    }
                                    "img_remove_bg" -> {
                                        val out = FileUtils.createOutputFile(context, "nobg", ".png")
                                        val original = BitmapFactory.decodeFile(selectedImageFile!!.absolutePath)
                                        val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
                                        // Transparent background mask conversion
                                        val pixel = mutable.getPixel(0, 0)
                                        for (x in 0 until mutable.width) {
                                            for (y in 0 until mutable.height) {
                                                if (mutable.getPixel(x, y) == pixel) {
                                                    mutable.setPixel(x, y, android.graphics.Color.TRANSPARENT)
                                                }
                                            }
                                        }
                                        FileOutputStream(out).use { mutable.compress(Bitmap.CompressFormat.PNG, 100, it) }
                                        out
                                    }
                                    "img_watermark" -> {
                                        val out = FileUtils.createOutputFile(context, "watermarked", ".jpg")
                                        ImageProcessingUtils.addWatermark(selectedImageFile!!, watermarkText, 200, out)
                                        out
                                    }
                                    "img_meme_gen" -> {
                                        val out = FileUtils.createOutputFile(context, "meme", ".jpg")
                                        ImageProcessingUtils.generateMeme(selectedImageFile!!, topMemeText, bottomMemeText, out)
                                        out
                                    }
                                    "img_rotate" -> {
                                        val out = FileUtils.createOutputFile(context, "rotated", ".jpg")
                                        ImageProcessingUtils.rotateImage(selectedImageFile!!, rotateAngle, false, false, out)
                                        out
                                    }
                                    "img_blur_face" -> {
                                        val out = FileUtils.createOutputFile(context, "blurred_face", ".jpg")
                                        val bitmap = BitmapFactory.decodeFile(selectedImageFile!!.absolutePath)
                                        val image = InputImage.fromBitmap(bitmap, 0)
                                        val detector = FaceDetection.getClient(FaceDetectorOptions.Builder().build())

                                        var faceRects = emptyList<android.graphics.Rect>()
                                        val task = detector.process(image)
                                        while (!task.isComplete) { Thread.sleep(20) }
                                        if (task.isSuccessful) {
                                            faceRects = task.result.map { it.boundingBox }
                                        }
                                        ImageProcessingUtils.blurFaces(selectedImageFile!!, faceRects, out)
                                        out
                                    }
                                    else -> {
                                        val out = FileUtils.createOutputFile(context, "processed", ".jpg")
                                        selectedImageFile!!.copyTo(out, overwrite = true)
                                        out
                                    }
                                }
                            }
                            resultImageFile = output
                        } catch (e: Exception) {
                            errorMessage = "Processing failed: ${e.localizedMessage}"
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
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Text("Process & Save Result")
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        if (resultImageFile != null) {
            Spacer(modifier = Modifier.height(20.dp))
            ResultSuccessCard(resultFile = resultImageFile!!)
        }
    }
}
