package com.example.intelli_pest.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build

/**
 * A centralized, robust utility for handling Bitmap conversions.
 */
object BitmapUtils {

    /**
     * Converts any bitmap to a mutable, software-based ARGB_8888 bitmap.
     * This is the most reliable method to ensure a bitmap's pixels can be accessed
     * by the CPU, avoiding crashes from Hardware bitmaps.
     *
     * @param bitmap The input bitmap, which can be in any format (Hardware, RGB_565, etc.).
     * @return A new, mutable ARGB_8888 bitmap, or the original bitmap if conversion fails.
     */
    fun toSoftwareBitmap(bitmap: Bitmap): Bitmap {
        // If it's already in the desired format, no need to convert.
        if (bitmap.isMutable && bitmap.config == Bitmap.Config.ARGB_8888) {
            return bitmap
        }

        return try {
            // Create a new ARGB_8888 bitmap and draw the original onto it.
            // This is the most reliable way to convert hardware bitmaps.
            val softwareBitmap = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(softwareBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            softwareBitmap
        } catch (e: Exception) {
            // If Canvas method fails, try copy() as a last resort.
            try {
                bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: bitmap
            } catch (e2: Exception) {
                // If all else fails, return the original and hope for the best.
                bitmap
            }
        }
    }
}

