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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    onImageSelected(bitmap)
                } else {
                    onError("Failed to load image. Please try a different image.")
                }
            } catch (e: Exception) {
                onError("Error loading image: ${e.message}")
            }
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }
}

/**
 * Load bitmap from URI with multiple fallback methods
 * Always returns a software ARGB_8888 bitmap suitable for pixel operations
 */
private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    // Try multiple methods to load the bitmap

    // Method 1: ImageDecoder with software allocator (Android P+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
            return ensureSoftwareBitmap(bitmap)
        } catch (e: Exception) {
            // Fall through to next method
        }
    }

    // Method 2: MediaStore (legacy but reliable)
    try {
        @Suppress("DEPRECATION")
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        return ensureSoftwareBitmap(bitmap)
    } catch (e: Exception) {
        // Fall through to next method
    }

    // Method 3: BitmapFactory with InputStream
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = false
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            return bitmap?.let { ensureSoftwareBitmap(it) }
        }
    } catch (e: Exception) {
        // Fall through to next method
    }

    // Method 4: Try with content resolver and file descriptor
    try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor, null, options)
            return bitmap?.let { ensureSoftwareBitmap(it) }
        }
    } catch (e: Exception) {
        // All methods failed
    }

    return null
}

/**
 * Ensure bitmap is a software ARGB_8888 bitmap suitable for pixel operations
 * Uses Canvas drawing as a reliable conversion method
 */
private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
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
            // This is the most reliable way to convert hardware bitmaps
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

