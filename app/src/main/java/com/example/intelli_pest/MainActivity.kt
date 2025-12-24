package com.example.intelli_pest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intelli_pest.di.AppContainer
import com.example.intelli_pest.presentation.camera.CameraScreen
import com.example.intelli_pest.presentation.detection.DetectionViewModel
import com.example.intelli_pest.presentation.gallery.GalleryPicker
import com.example.intelli_pest.presentation.main.MainScreen
import com.example.intelli_pest.presentation.main.MainViewModel
import com.example.intelli_pest.presentation.models.ModelSelectionScreen
import com.example.intelli_pest.presentation.navigation.Screen
import com.example.intelli_pest.presentation.results.ResultsScreen
import com.example.intelli_pest.presentation.settings.SettingsScreen
import com.example.intelli_pest.presentation.settings.SettingsViewModel
import com.example.intelli_pest.presentation.splash.SplashScreen
import com.example.intelli_pest.ui.theme.Intelli_PESTTheme
import com.example.intelli_pest.util.AppLogger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.logInfo("MainActivity", "Activity_Created", "MainActivity onCreate called")

        // Initialize DI
        AppContainer.initialize(applicationContext)
        AppLogger.logResponse("MainActivity", "DI_Init", "AppContainer initialized in MainActivity")

        enableEdgeToEdge()
        setContent {
            Intelli_PESTTheme {
                IntelliPestApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppLogger.logInfo("MainActivity", "Activity_Resumed", "MainActivity onResume")
    }

    override fun onPause() {
        AppLogger.logInfo("MainActivity", "Activity_Paused", "MainActivity onPause")
        super.onPause()
    }

    override fun onDestroy() {
        AppLogger.logInfo("MainActivity", "Activity_Destroyed", "MainActivity onDestroy")
        super.onDestroy()
    }
}

@Composable
fun IntelliPestApp() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    // Log app composable start
    LaunchedEffect(Unit) {
        AppLogger.logScreenOpened("IntelliPestApp")
    }

    if (showSplash) {
        SplashScreen(
            onSplashFinished = {
                AppLogger.logResponse("SplashScreen", "Splash_Finished", "Splash animation completed")
                showSplash = false
            }
        )
        return
    }

    val detectionViewModel: DetectionViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppContainer.provideDetectionViewModel() as T
            }
        }
    )
    val mainViewModel: MainViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppContainer.provideMainViewModel() as T
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppContainer.provideSettingsViewModel() as T
            }
        }
    )

    val detectionUiState by detectionViewModel.uiState.collectAsState()
    val mainUiState by mainViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val currentRuntime by mainViewModel.currentRuntime.collectAsState()
    val selectedModelId by mainViewModel.selectedModelId.collectAsState()

    var showGalleryPicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    // Handle detection completion - navigate to results
    LaunchedEffect(detectionUiState.detectionResult) {
        if (detectionUiState.detectionResult != null && !detectionUiState.isDetecting) {
            val result = detectionUiState.detectionResult!!
            AppLogger.logResponse(
                "Detection",
                "Detection_Complete",
                "Pest: ${result.pestType.displayName}, Confidence: ${result.getConfidencePercentage()}"
            )
            navController.navigate(Screen.Results.route.replace("{resultId}", "current")) {
                popUpTo(Screen.Main.route)
            }
        }
    }

    // Error handling
    detectionUiState.error?.let { error ->
        LaunchedEffect(error) {
            AppLogger.logError("Detection", "Detection_Error", error)
            showError = error
        }
    }

    // Error dialog
    if (showError != null) {
        AlertDialog(
            onDismissRequest = {
                AppLogger.logAction("ErrorDialog", "Error_Dismissed", "User dismissed error: $showError")
                showError = null
                detectionViewModel.dismissError()
            },
            title = { Text("Error") },
            text = { Text(showError ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    AppLogger.logAction("ErrorDialog", "Error_Acknowledged", "User clicked OK on error: $showError")
                    showError = null
                    detectionViewModel.dismissError()
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Loading overlay
    if (detectionUiState.isDetecting) {
        AppLogger.logInfo("Detection", "Detection_InProgress", "Showing detection loading overlay")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
            ) {}

            Card(
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Detecting pest...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    // Gallery picker
    if (showGalleryPicker) {
        AppLogger.logScreenOpened("GalleryPicker")
        GalleryPicker(
            onImageSelected = { bitmap ->
                AppLogger.logResponse(
                    "GalleryPicker",
                    "Image_Selected",
                    "Bitmap: ${bitmap.width}x${bitmap.height}, Config: ${bitmap.config}"
                )
                showGalleryPicker = false
                AppLogger.logAction("GalleryPicker", "Starting_Detection", "Initiating pest detection from gallery image")
                detectionViewModel.detectPest(bitmap)
            },
            onError = { error ->
                AppLogger.logError("GalleryPicker", "Gallery_Error", error)
                showGalleryPicker = false
                showError = error
            },
            onDismiss = {
                AppLogger.logAction("GalleryPicker", "Gallery_Dismissed", "User cancelled gallery picker")
                showGalleryPicker = false
            }
        )
    }

    // Navigation
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            val context = LocalContext.current
            AppLogger.logScreenOpened("MainScreen")
            MainScreen(
                uiState = mainUiState,
                currentRuntime = currentRuntime,
                selectedModelId = selectedModelId,
                onCaptureClick = {
                    AppLogger.logAction("MainScreen", "Camera_Button_Clicked", "User clicked capture/camera button")
                    navController.navigate(Screen.Camera.route)
                },
                onGalleryClick = {
                    AppLogger.logAction("MainScreen", "Gallery_Button_Clicked", "User clicked gallery button")
                    showGalleryPicker = true
                },
                onModelsClick = {
                    AppLogger.logAction("MainScreen", "Models_Button_Clicked", "User clicked models button")
                    navController.navigate(Screen.Models.route)
                },
                onHistoryClick = {
                    AppLogger.logAction("MainScreen", "History_Button_Clicked", "User clicked history button")
                    showError = "History screen coming soon!"
                },
                onSettingsClick = {
                    AppLogger.logAction("MainScreen", "Settings_Button_Clicked", "User clicked settings button")
                    navController.navigate(Screen.Settings.route)
                },
                onShareLogsClick = {
                    AppLogger.logAction("MainScreen", "Share_Logs_Clicked", "User clicked share logs button")
                    try {
                        val logFile = AppLogger.getShareableLogFile()
                        if (logFile != null && logFile.exists()) {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                logFile
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Intelli-PEST App Logs")
                                putExtra(Intent.EXTRA_TEXT, "Please find attached the application logs for debugging.")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))
                            AppLogger.logResponse("MainScreen", "Share_Logs_Started", "Share intent launched successfully")
                        } else {
                            AppLogger.logError("MainScreen", "Share_Logs_Failed", "Log file is null or doesn't exist")
                            Toast.makeText(context, "No logs available to share", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        AppLogger.logError("MainScreen", "Share_Logs_Error", e, "Exception while sharing logs")
                        Toast.makeText(context, "Failed to share logs: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable(Screen.Models.route) {
            AppLogger.logScreenOpened("ModelSelectionScreen")
            ModelSelectionScreen(
                viewModel = mainViewModel,
                onBack = {
                    AppLogger.logAction("ModelSelectionScreen", "Back_Button_Clicked", "User navigating back from models")
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Camera.route) {
            AppLogger.logScreenOpened("CameraScreen")
            CameraScreen(
                onImageCaptured = { bitmap ->
                    AppLogger.logResponse(
                        "CameraScreen",
                        "Image_Captured",
                        "Bitmap: ${bitmap.width}x${bitmap.height}, Config: ${bitmap.config}"
                    )
                    navController.popBackStack()
                    AppLogger.logAction("CameraScreen", "Starting_Detection", "Initiating pest detection from camera image")
                    detectionViewModel.detectPest(bitmap)
                },
                onError = { error ->
                    AppLogger.logError("CameraScreen", "Camera_Error", error)
                    showError = error
                },
                onBack = {
                    AppLogger.logAction("CameraScreen", "Back_Button_Clicked", "User navigating back from camera")
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Results.route) {
            AppLogger.logScreenOpened("ResultsScreen")
            ResultsScreen(
                result = detectionUiState.detectionResult,
                bitmap = detectionUiState.capturedBitmap,
                onSave = {
                    AppLogger.logAction("ResultsScreen", "Save_Button_Clicked", "User clicked save result")
                    showError = "Result saved to history!"
                },
                onRetry = {
                    AppLogger.logAction("ResultsScreen", "Retry_Button_Clicked", "User wants to retry detection")
                    detectionViewModel.clearResult()
                    navController.popBackStack(Screen.Main.route, false)
                },
                onBack = {
                    AppLogger.logAction("ResultsScreen", "Back_Button_Clicked", "User navigating back from results")
                    detectionViewModel.clearResult()
                    navController.popBackStack(Screen.Main.route, false)
                }
            )
        }

        composable(Screen.Settings.route) {
            val context = LocalContext.current
            AppLogger.logScreenOpened("SettingsScreen")
            SettingsScreen(
                uiState = settingsUiState,
                onBack = {
                    AppLogger.logAction("SettingsScreen", "Back_Button_Clicked", "User navigating back from settings")
                    navController.popBackStack()
                },
                onTrackingModeChanged = { enabled ->
                    settingsViewModel.setTrackingMode(enabled)
                },
                onMLRuntimeChanged = { runtime ->
                    settingsViewModel.setMLRuntime(runtime)
                },
                onConfidenceThresholdChanged = { threshold ->
                    settingsViewModel.setConfidenceThreshold(threshold)
                },
                onDownloadLogs = {
                    settingsViewModel.downloadLogs()
                },
                onShareLogs = {
                    settingsViewModel.shareLogs(context)
                },
                onClearMessage = {
                    settingsViewModel.clearMessage()
                }
            )
        }
    }
}