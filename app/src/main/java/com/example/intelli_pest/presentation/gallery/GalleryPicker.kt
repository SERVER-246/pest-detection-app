package com.example.intelli_pest.presentation.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
                val bitmap = uriToBitmap(context, uri)
                if (bitmap != null) {
                    // Ensure it's a software bitmap for pixel access
                    val softwareBitmap = ensureSoftwareBitmap(bitmap)
                    onImageSelected(softwareBitmap)
                } else {
                    onError("Failed to load image")
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
 * Convert URI to Bitmap - always returns a software bitmap
 */
private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            // Use setMutableRequired to avoid hardware bitmaps
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        // Fallback: try loading with BitmapFactory
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e2: Exception) {
            e2.printStackTrace()
            null
        }
    }
}

/**
 * Ensure bitmap is a software bitmap (not HARDWARE config)
 */
private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
               bitmap.config == Bitmap.Config.HARDWARE) {
        // Convert HARDWARE bitmap to ARGB_8888 for pixel access
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
    } else {
        bitmap
    }
}

