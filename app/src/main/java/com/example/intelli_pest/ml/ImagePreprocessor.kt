package com.example.intelli_pest.ml

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImagePreprocessor {

    companion object {
        private const val IMAGE_SIZE = 224
        private const val PIXEL_SIZE = 3 // RGB
        private const val IMAGE_MEAN = 0.485f
        private const val IMAGE_STD = 0.229f
    }

    /**
     * Preprocess bitmap for model inference
     * Resizes, normalizes, and converts to the format expected by ONNX model
     */
    fun preprocessImage(bitmap: Bitmap): FloatArray {
        // Resize bitmap to required size
        val resizedBitmap = resizeBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE)

        // Convert to float array with normalization
        val floatArray = FloatArray(IMAGE_SIZE * IMAGE_SIZE * PIXEL_SIZE)
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

        return floatArray
    }

    /**
     * Resize bitmap while maintaining aspect ratio and center cropping
     */
    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val targetAspectRatio = targetWidth.toFloat() / targetHeight.toFloat()

        val scaledBitmap = if (aspectRatio > targetAspectRatio) {
            // Bitmap is wider, scale by height
            val scaledWidth = (targetHeight * aspectRatio).toInt()
            Bitmap.createScaledBitmap(bitmap, scaledWidth, targetHeight, true)
        } else {
            // Bitmap is taller, scale by width
            val scaledHeight = (targetWidth / aspectRatio).toInt()
            Bitmap.createScaledBitmap(bitmap, targetWidth, scaledHeight, true)
        }

        // Center crop to target size
        val x = (scaledBitmap.width - targetWidth) / 2
        val y = (scaledBitmap.height - targetHeight) / 2

        return Bitmap.createBitmap(scaledBitmap, x, y, targetWidth, targetHeight)
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
        if (bitmap.width < 100 || bitmap.height < 100) {
            return false
        }

        // Check if image is not too dark or too bright
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var totalBrightness = 0
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            totalBrightness += (r + g + b) / 3
        }

        val avgBrightness = totalBrightness / pixels.size
        // Image should not be too dark (< 20) or too bright (> 235)
        return avgBrightness in 20..235
    }
}

