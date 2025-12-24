package com.example.intelli_pest.presentation.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.intelli_pest.util.AppLogger

/**
 * Gallery picker for selecting images
 */
@Composable
fun GalleryPicker(
    onImageSelected: (Bitmap) -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AppLogger.logInfo("GalleryPicker", "Picker_Initialized", "Gallery picker composable started")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        AppLogger.logAction("GalleryPicker", "Image_Selection_Result", "URI returned: ${uri?.toString() ?: "null"}")
        if (uri != null) {
            try {
                AppLogger.logInfo("GalleryPicker", "Loading_Bitmap", "Attempting to load bitmap from URI: $uri")
                val bitmap = loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    AppLogger.logResponse(
                        "GalleryPicker",
                        "Bitmap_Loaded",
                        "Size: ${bitmap.width}x${bitmap.height}, Config: ${bitmap.config}"
                    )
                    onImageSelected(bitmap)
                } else {
                    AppLogger.logError("GalleryPicker", "Load_Failed", "loadBitmapFromUri returned null")
                    onError("Failed to load image. Please try a different image.")
                }
            } catch (e: Exception) {
                AppLogger.logError("GalleryPicker", "Load_Exception", e, "Exception while loading image")
                onError("Error loading image: ${e.message}")
            }
        } else {
            AppLogger.logAction("GalleryPicker", "Selection_Cancelled", "User cancelled image selection or no URI returned")
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        AppLogger.logAction("GalleryPicker", "Launching_Picker", "Launching system image picker")
        launcher.launch("image/*")
    }
}

/**
 * Load bitmap from URI with multiple fallback methods
 * Always returns a software ARGB_8888 bitmap suitable for pixel operations
 */
private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    AppLogger.logDebug("GalleryPicker", "Load_Start", "Starting bitmap load from URI, Android SDK: ${Build.VERSION.SDK_INT}")

    // Try multiple methods to load the bitmap

    // Method 1: ImageDecoder with software allocator (Android P+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        try {
            AppLogger.logDebug("GalleryPicker", "Method_1", "Trying ImageDecoder (API 28+)")
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
            AppLogger.logResponse("GalleryPicker", "Method_1_Success", "ImageDecoder succeeded: ${bitmap.width}x${bitmap.height}")
            return ensureSoftwareBitmap(bitmap)
        } catch (e: Exception) {
            AppLogger.logWarning("GalleryPicker", "Method_1_Failed", "ImageDecoder failed: ${e.message}")
            // Fall through to next method
        }
    }

    // Method 2: MediaStore (legacy but reliable)
    try {
        AppLogger.logDebug("GalleryPicker", "Method_2", "Trying MediaStore.Images.Media.getBitmap")
        @Suppress("DEPRECATION")
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        AppLogger.logResponse("GalleryPicker", "Method_2_Success", "MediaStore succeeded: ${bitmap.width}x${bitmap.height}")
        return ensureSoftwareBitmap(bitmap)
    } catch (e: Exception) {
        AppLogger.logWarning("GalleryPicker", "Method_2_Failed", "MediaStore failed: ${e.message}")
        // Fall through to next method
    }

    // Method 3: BitmapFactory with InputStream
    try {
        AppLogger.logDebug("GalleryPicker", "Method_3", "Trying BitmapFactory with InputStream")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = false
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            if (bitmap != null) {
                AppLogger.logResponse("GalleryPicker", "Method_3_Success", "BitmapFactory.decodeStream succeeded: ${bitmap.width}x${bitmap.height}")
            }
            return bitmap?.let { ensureSoftwareBitmap(it) }
        }
    } catch (e: Exception) {
        AppLogger.logWarning("GalleryPicker", "Method_3_Failed", "BitmapFactory.decodeStream failed: ${e.message}")
        // Fall through to next method
    }

    // Method 4: Try with content resolver and file descriptor
    try {
        AppLogger.logDebug("GalleryPicker", "Method_4", "Trying BitmapFactory with FileDescriptor")
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor, null, options)
            if (bitmap != null) {
                AppLogger.logResponse("GalleryPicker", "Method_4_Success", "FileDescriptor method succeeded: ${bitmap.width}x${bitmap.height}")
            }
            return bitmap?.let { ensureSoftwareBitmap(it) }
        }
    } catch (e: Exception) {
        AppLogger.logWarning("GalleryPicker", "Method_4_Failed", "FileDescriptor method failed: ${e.message}")
        // All methods failed
    }

    AppLogger.logError("GalleryPicker", "All_Methods_Failed", "All 4 bitmap loading methods failed for URI: $uri")
    return null
}

/**
 * Ensure bitmap is a software ARGB_8888 bitmap suitable for pixel operations
 * Uses Canvas drawing as a reliable conversion method
 */
private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
    AppLogger.logDebug("GalleryPicker", "Ensure_Software", "Input: ${bitmap.width}x${bitmap.height}, Config: ${bitmap.config}")
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
            AppLogger.logInfo("GalleryPicker", "Converting_Bitmap", "Converting from ${bitmap.config} to ARGB_8888")
            // Create a new ARGB_8888 bitmap and draw the original onto it
            // This is the most reliable way to convert hardware bitmaps
            val softwareBitmap = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(softwareBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            AppLogger.logResponse("GalleryPicker", "Conversion_Success", "Converted to ${softwareBitmap.config}")
            softwareBitmap
        } else {
            AppLogger.logDebug("GalleryPicker", "No_Conversion_Needed", "Bitmap already ARGB_8888")
            bitmap
        }
    } catch (e: Exception) {
        AppLogger.logWarning("GalleryPicker", "Canvas_Conversion_Failed", "Trying bitmap.copy() fallback: ${e.message}")
        // Fallback: try bitmap.copy()
        try {
            val copied = bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            AppLogger.logResponse("GalleryPicker", "Copy_Fallback_Success", "bitmap.copy() succeeded")
            copied
        } catch (e2: Exception) {
            AppLogger.logError("GalleryPicker", "All_Conversions_Failed", e2, "Returning original bitmap")
            bitmap
        }
    }
}

