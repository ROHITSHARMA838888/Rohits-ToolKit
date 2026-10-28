package com.rohit.toolkit.util

import android.graphics.*
import java.io.File
import java.io.FileOutputStream

object ImageProcessingUtils {

    // 1. Compress Image
    fun compressImage(inputFile: File, quality: Int, format: Bitmap.CompressFormat, outputFile: File) {
        val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(format, quality.coerceIn(1, 100), out)
        }
    }

    // 2. Resize Image
    fun resizeImage(inputFile: File, targetWidth: Int, targetHeight: Int, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val scaled = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
        FileOutputStream(outputFile).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }

    // 3. Crop Image
    fun cropImage(inputFile: File, left: Int, top: Int, width: Int, height: Int, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val safeLeft = left.coerceIn(0, original.width - 1)
        val safeTop = top.coerceIn(0, original.height - 1)
        val safeWidth = width.coerceIn(1, original.width - safeLeft)
        val safeHeight = height.coerceIn(1, original.height - safeTop)

        val cropped = Bitmap.createBitmap(original, safeLeft, safeTop, safeWidth, safeHeight)
        FileOutputStream(outputFile).use { out ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }

    // 4. Convert Formats (PNG, JPG, WEBP)
    fun convertFormat(inputFile: File, targetFormat: Bitmap.CompressFormat, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        FileOutputStream(outputFile).use { out ->
            original.compress(targetFormat, 95, out)
        }
    }

    // 5. Apply Photo Filter (Brightness, Contrast, Saturation, Sepia, Grayscale, Invert)
    fun applyColorFilter(inputFile: File, filterType: String, intensity: Float, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colorMatrix = ColorMatrix()
        when (filterType.lowercase()) {
            "grayscale" -> colorMatrix.setSaturation(0f)
            "sepia" -> {
                colorMatrix.setSaturation(0f)
                val sepiaMatrix = ColorMatrix(floatArrayOf(
                    1.0f, 0.0f, 0.0f, 0.0f, 30.0f * intensity,
                    0.0f, 0.9f, 0.0f, 0.0f, 15.0f * intensity,
                    0.0f, 0.0f, 0.8f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                ))
                colorMatrix.postConcat(sepiaMatrix)
            }
            "invert" -> {
                colorMatrix.set(floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            "brightness" -> {
                val b = (intensity * 255).coerceIn(-255f, 255f)
                colorMatrix.set(floatArrayOf(
                    1f, 0f, 0f, 0f, b,
                    0f, 1f, 0f, 0f, b,
                    0f, 0f, 1f, 0f, b,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            "contrast" -> {
                val scale = intensity.coerceAtLeast(0.1f)
                val translate = (-0.5f * scale + 0.5f) * 255f
                colorMatrix.set(floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            "saturation" -> colorMatrix.setSaturation(intensity.coerceAtLeast(0f))
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(original, 0f, 0f, paint)

        FileOutputStream(outputFile).use { out ->
            result.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }

    // 6. Watermark Image
    fun addWatermark(inputFile: File, watermarkText: String, opacity: Int, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = opacity.coerceIn(0, 255)
            textSize = (mutable.width / 18f).coerceAtLeast(24f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }

        val bounds = Rect()
        paint.getTextBounds(watermarkText, 0, watermarkText.length, bounds)
        val x = (mutable.width - bounds.width()) / 2f
        val y = mutable.height - (bounds.height() * 2f)

        canvas.drawText(watermarkText, x, y, paint)

        FileOutputStream(outputFile).use { out ->
            mutable.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }

    // 7. Meme Generator
    fun generateMeme(inputFile: File, topText: String, bottomText: String, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val textSize = (mutable.width / 12f).coerceAtLeast(32f)

        // Text Outline (Black stroke)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            this.textSize = textSize
            style = Paint.Style.STROKE
            strokeWidth = textSize / 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Text Fill (White fill)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            style = Paint.Style.FILL
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val centerX = mutable.width / 2f

        if (topText.isNotBlank()) {
            val topY = textSize * 1.3f
            canvas.drawText(topText.uppercase(), centerX, topY, strokePaint)
            canvas.drawText(topText.uppercase(), centerX, topY, fillPaint)
        }

        if (bottomText.isNotBlank()) {
            val bottomY = mutable.height - (textSize * 0.5f)
            canvas.drawText(bottomText.uppercase(), centerX, bottomY, strokePaint)
            canvas.drawText(bottomText.uppercase(), centerX, bottomY, fillPaint)
        }

        FileOutputStream(outputFile).use { out ->
            mutable.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }

    // 8. Rotate Image
    fun rotateImage(inputFile: File, degrees: Float, flipHorizontal: Boolean, flipVertical: Boolean, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val matrix = Matrix()
        matrix.postRotate(degrees)
        val scaleX = if (flipHorizontal) -1f else 1f
        val scaleY = if (flipVertical) -1f else 1f
        matrix.postScale(scaleX, scaleY)

        val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        FileOutputStream(outputFile).use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }

    // 9. Blur Faces on detected face Rects
    fun blurFaces(inputFile: File, faceRects: List<Rect>, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (rect in faceRects) {
            val safeLeft = rect.left.coerceIn(0, mutable.width - 1)
            val safeTop = rect.top.coerceIn(0, mutable.height - 1)
            val safeWidth = rect.width().coerceIn(1, mutable.width - safeLeft)
            val safeHeight = rect.height().coerceIn(1, mutable.height - safeTop)

            // Extract face bitmap, downscale dramatically, then upscale to pixelate/blur
            val faceCrop = Bitmap.createBitmap(mutable, safeLeft, safeTop, safeWidth, safeHeight)
            val downscaled = Bitmap.createScaledBitmap(faceCrop, maxOf(1, safeWidth / 12), maxOf(1, safeHeight / 12), false)
            val pixelated = Bitmap.createScaledBitmap(downscaled, safeWidth, safeHeight, false)

            canvas.drawBitmap(pixelated, safeLeft.toFloat(), safeTop.toFloat(), paint)
        }

        FileOutputStream(outputFile).use { out ->
            mutable.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
    }

    // 10. Upscale Image (2x, 4x with bicubic filtering and unsharp sharpening)
    fun upscaleImage(inputFile: File, factor: Int, outputFile: File) {
        val original = BitmapFactory.decodeFile(inputFile.absolutePath) ?: throw Exception("Invalid image file")
        val targetWidth = original.width * factor
        val targetHeight = original.height * factor

        val scaled = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
        FileOutputStream(outputFile).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 98, out)
        }
    }
}
