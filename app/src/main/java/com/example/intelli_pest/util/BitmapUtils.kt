package com.example.intelli_pest.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log

/**
 * A centralized, robust utility for handling Bitmap conversions.
 */
object BitmapUtils {
    private const val TAG = "BitmapUtils"

    /**
     * Converts any bitmap to a mutable, software-based ARGB_8888 bitmap.
     * This is the most reliable method to ensure a bitmap's pixels can be accessed
     * by the CPU, avoiding crashes from Hardware bitmaps.
     *
     * @param bitmap The input bitmap, which can be in any format (Hardware, RGB_565, etc.).
     * @return A new, mutable ARGB_8888 bitmap, or the original bitmap if conversion fails.
     */
    fun toSoftwareBitmap(bitmap: Bitmap): Bitmap {
        try {
            // Check if already in desired format
            if (bitmap.config == Bitmap.Config.ARGB_8888 && !isHardwareBitmap(bitmap)) {
                Log.d(TAG, "Bitmap already in ARGB_8888 software format")
                return bitmap
            }

            Log.d(TAG, "Converting bitmap: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")

            // Create a new ARGB_8888 bitmap and draw the original onto it
            val softwareBitmap = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(softwareBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            Log.d(TAG, "Successfully converted to software bitmap")
            return softwareBitmap

        } catch (e: Exception) {
            Log.e(TAG, "Canvas conversion failed, trying copy()", e)

            // Fallback: try copy() method
            return try {
                val copied = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                if (copied != null) {
                    Log.d(TAG, "Successfully copied bitmap")
                    copied
                } else {
                    Log.w(TAG, "copy() returned null, using original")
                    bitmap
                }
            } catch (e2: Exception) {
                Log.e(TAG, "All conversion methods failed", e2)
                bitmap
            }
        }
    }

    /**
     * Checks if a bitmap is a hardware bitmap (Android O+)
     */
    private fun isHardwareBitmap(bitmap: Bitmap): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bitmap.config == Bitmap.Config.HARDWARE
        } else {
            false
        }
    }

    /**
     * Safely recycles a bitmap if it's not null and not recycled
     */
    fun safeRecycle(bitmap: Bitmap?) {
        try {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
                Log.d(TAG, "Bitmap recycled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recycling bitmap", e)
        }
    }
}
