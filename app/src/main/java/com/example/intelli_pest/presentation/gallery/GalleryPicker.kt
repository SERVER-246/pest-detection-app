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
                    onImageSelected(bitmap)
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
 * Convert URI to Bitmap
 */
private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

