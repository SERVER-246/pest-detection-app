package com.example.intelli_pest.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImagePreprocessor {

    companion object {
        private const val IMAGE_SIZE = 224
        private const val PIXEL_SIZE = 3 // RGB
    }

    /**
     * Preprocess bitmap for model inference
     * Resizes, normalizes, and converts to the format expected by ONNX model
     */
    fun preprocessImage(bitmap: Bitmap): FloatArray {
        // This function now assumes it receives a software bitmap.
        // The conversion is handled at the source (Camera/Gallery).

        // Resize bitmap to required size
        val resizedBitmap = resizeBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE)

        // Convert to float array with normalization
        val floatArray = FloatArray(IMAGE_SIZE * IMAGE_SIZE * PIXEL_SIZE)

        try {
            val pixels = IntArray(IMAGE_SIZE * IMAGE_SIZE)
            resizedBitmap.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE)

            var idx = 0
            for (pixel in pixels) {
                // Extract RGB values
                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                // Normalize using ImageNet mean and std
                floatArray[idx++] = (r - 0.485f) / 0.229f
                floatArray[idx++] = (g - 0.456f) / 0.224f
                floatArray[idx++] = (b - 0.406f) / 0.225f
            }
        } catch (e: Exception) {
            // If getPixels fails, try pixel by pixel as a fallback
            var idx = 0
            for (y in 0 until IMAGE_SIZE) {
                for (x in 0 until IMAGE_SIZE) {
                    try {
                        val pixel = resizedBitmap.getPixel(x, y)
                        val r = ((pixel shr 16) and 0xFF) / 255.0f
                        val g = ((pixel shr 8) and 0xFF) / 255.0f
                        val b = (pixel and 0xFF) / 255.0f

                        floatArray[idx++] = (r - 0.485f) / 0.229f
                        floatArray[idx++] = (g - 0.456f) / 0.224f
                        floatArray[idx++] = (b - 0.406f) / 0.225f
                    } catch (e2: Exception) {
                        // Use neutral gray values if single pixel access fails
                        floatArray[idx++] = 0f
                        floatArray[idx++] = 0f
                        floatArray[idx++] = 0f
                    }
                }
            }
        }

        return floatArray
    }

    /**
     * Resize bitmap while maintaining aspect ratio and center cropping
     */
    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return try {
            // First ensure it's a software bitmap
            val softwareBitmap = toSoftwareBitmap(bitmap)

            val aspectRatio = softwareBitmap.width.toFloat() / softwareBitmap.height.toFloat()
            val targetAspectRatio = targetWidth.toFloat() / targetHeight.toFloat()

            val scaledBitmap = if (aspectRatio > targetAspectRatio) {
                // Bitmap is wider, scale by height
                val scaledWidth = (targetHeight * aspectRatio).toInt().coerceAtLeast(targetWidth)
                Bitmap.createScaledBitmap(softwareBitmap, scaledWidth, targetHeight, true)
            } else {
                // Bitmap is taller, scale by width
                val scaledHeight = (targetWidth / aspectRatio).toInt().coerceAtLeast(targetHeight)
                Bitmap.createScaledBitmap(softwareBitmap, targetWidth, scaledHeight, true)
            }

            // Ensure scaled bitmap is software
            val softScaledBitmap = toSoftwareBitmap(scaledBitmap)

            // Center crop to target size
            val x = ((softScaledBitmap.width - targetWidth) / 2).coerceAtLeast(0)
            val y = ((softScaledBitmap.height - targetHeight) / 2).coerceAtLeast(0)

            val croppedWidth = targetWidth.coerceAtMost(softScaledBitmap.width - x)
            val croppedHeight = targetHeight.coerceAtMost(softScaledBitmap.height - y)

            Bitmap.createBitmap(softScaledBitmap, x, y, croppedWidth, croppedHeight)
        } catch (e: Exception) {
            // Fallback: just scale directly
            try {
                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            } catch (e2: Exception) {
                bitmap
            }
        }
    }

    /**
     * Convert FloatArray to ByteBuffer for ONNX Runtime
     */
    fun floatArrayToByteBuffer(floatArray: FloatArray): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(floatArray.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        floatArray.forEach { byteBuffer.putFloat(it) }
        byteBuffer.rewind()
        return byteBuffer
    }

    /**
     * Check if image has sufficient quality for detection
     */
    fun isImageQualitySufficient(bitmap: Bitmap): Boolean {
        // Check minimum resolution
        if (bitmap.width < 50 || bitmap.height < 50) {
            return false
        }

        return try {
            val softwareBitmap = toSoftwareBitmap(bitmap)

            // Sample center region for brightness check
            val centerX = softwareBitmap.width / 2
            val centerY = softwareBitmap.height / 2

            var totalBrightness = 0L
            var sampleCount = 0
            val sampleSize = minOf(50, softwareBitmap.width / 4, softwareBitmap.height / 4)

            for (dy in -sampleSize..sampleSize step 10) {
                for (dx in -sampleSize..sampleSize step 10) {
                    try {
                        val x = (centerX + dx).coerceIn(0, softwareBitmap.width - 1)
                        val y = (centerY + dy).coerceIn(0, softwareBitmap.height - 1)
                        val pixel = softwareBitmap.getPixel(x, y)
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val b = pixel and 0xFF
                        totalBrightness += (r + g + b) / 3
                        sampleCount++
                    } catch (e: Exception) {
                        // Skip this sample
                    }
                }
            }

            if (sampleCount == 0) return true

            val avgBrightness = totalBrightness / sampleCount
            avgBrightness in 10..245
        } catch (e: Exception) {
            true // Accept on error
        }
    }

    /**
     * Convert any bitmap to a software ARGB_8888 bitmap suitable for pixel operations
     * Uses Canvas drawing as the most reliable conversion method
     */
    private fun toSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return try {
            // Check if conversion is needed
            val needsConversion = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    bitmap.config == Bitmap.Config.HARDWARE -> true
                bitmap.config == null -> true
                bitmap.config != Bitmap.Config.ARGB_8888 -> true
                else -> false
            }

            if (needsConversion) {
                // Create a new ARGB_8888 bitmap and draw the original onto it
                val softwareBitmap = Bitmap.createBitmap(
                    bitmap.width,
                    bitmap.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(softwareBitmap)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                softwareBitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            // Fallback: try bitmap.copy()
            try {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } catch (e2: Exception) {
                bitmap
            }
        }
    }
}

