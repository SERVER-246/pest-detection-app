package com.example.intelli_pest.presentation.camera

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.intelli_pest.presentation.common.LoadingAnimation
import com.example.intelli_pest.ui.theme.PrimaryGreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

/**
 * Camera screen for capturing images
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit,
    onBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            CameraPreview(
                onImageCaptured = onImageCaptured,
                onError = onError,
                onBack = onBack
            )
        }
        else -> {
            PermissionDeniedScreen(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun CameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }

    val executor = remember { ContextCompat.getMainExecutor(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageCaptureBuilder = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)

                        // Set rotation if display is available
                        previewView.display?.let { display ->
                            imageCaptureBuilder.setTargetRotation(display.rotation)
                        }

                        imageCapture = imageCaptureBuilder.build()

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        onError("Failed to start camera: ${e.message}")
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        CameraOverlay()

        CameraTopBar(
            flashEnabled = flashEnabled,
            onFlashToggle = { flashEnabled = !flashEnabled },
            onBack = onBack
        )

        CameraControls(
            isCapturing = isCapturing,
            onCapture = {
                isCapturing = true
                captureImage(
                    context = context,
                    imageCapture = imageCapture,
                    executor = executor,
                    onImageCaptured = { bitmap ->
                        isCapturing = false
                        onImageCaptured(bitmap)
                    },
                    onError = { error ->
                        isCapturing = false
                        onError(error)
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun CameraOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(
                    width = 3.dp,
                    color = PrimaryGreen.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            "Position the crop within the frame",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CameraTopBar(
    flashEnabled: Boolean,
    onFlashToggle: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Flash",
                tint = if (flashEnabled) PrimaryGreen else Color.White
            )
        }
    }
}

@Composable
private fun CameraControls(
    isCapturing: Boolean,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isCapturing) {
            LoadingAnimation(
                modifier = Modifier.size(80.dp),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Capturing...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = onCapture,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Capture",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tap to capture",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PermissionDeniedScreen(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "This app needs camera access to capture images for pest detection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("Go Back")
            }
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    if (imageCapture == null) {
        onError("Camera not ready. Please try again.")
        return
    }

    try {
        // Create a temporary file in cache directory
        val photoFile = File(
            context.cacheDir,
            "capture_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        // Read the saved file
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                        if (bitmap == null) {
                            onError("Failed to decode captured image")
                            photoFile.delete()
                            return
                        }

                        // Get rotation from EXIF data and apply it
                        val rotatedBitmap = correctBitmapRotation(photoFile.absolutePath, bitmap)

                        // Clean up temp file
                        photoFile.delete()

                        // Ensure it's a software bitmap
                        val finalBitmap = ensureSoftwareBitmap(rotatedBitmap)

                        onImageCaptured(finalBitmap)
                    } catch (e: Exception) {
                        photoFile.delete()
                        onError("Failed to process image: ${e.message}")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    photoFile.delete()
                    onError("Capture failed: ${exception.message}")
                }
            }
        )
    } catch (e: Exception) {
        onError("Failed to capture: ${e.message}")
    }
}

private fun correctBitmapRotation(filePath: String, bitmap: Bitmap): Bitmap {
    return try {
        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotationDegrees != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    } catch (e: Exception) {
        bitmap // Return original on error
    }
}

private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
               bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        bitmap
    }
}

