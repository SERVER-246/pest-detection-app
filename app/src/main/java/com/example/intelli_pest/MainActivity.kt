package com.example.intelli_pest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.intelli_pest.presentation.navigation.Screen
import com.example.intelli_pest.presentation.results.ResultsScreen
import com.example.intelli_pest.ui.theme.Intelli_PESTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DI
        AppContainer.initialize(applicationContext)

        enableEdgeToEdge()
        setContent {
            Intelli_PESTTheme {
                IntelliPestApp()
            }
        }
    }
}

@Composable
fun IntelliPestApp() {
    val navController = rememberNavController()
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

    val detectionUiState by detectionViewModel.uiState.collectAsState()
    val mainUiState by mainViewModel.uiState.collectAsState()

    var showGalleryPicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    // Handle detection completion - navigate to results
    LaunchedEffect(detectionUiState.detectionResult) {
        if (detectionUiState.detectionResult != null && !detectionUiState.isDetecting) {
            navController.navigate(Screen.Results.route.replace("{resultId}", "current")) {
                popUpTo(Screen.Main.route)
            }
        }
    }

    // Error handling
    detectionUiState.error?.let { error ->
        LaunchedEffect(error) {
            showError = error
        }
    }

    // Error dialog
    if (showError != null) {
        AlertDialog(
            onDismissRequest = {
                showError = null
                detectionViewModel.dismissError()
            },
            title = { Text("Error") },
            text = { Text(showError ?: "") },
            confirmButton = {
                TextButton(onClick = {
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
        GalleryPicker(
            onImageSelected = { bitmap ->
                showGalleryPicker = false
                detectionViewModel.detectPest(bitmap)
            },
            onError = { error ->
                showGalleryPicker = false
                showError = error
            },
            onDismiss = {
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
            MainScreen(
                uiState = mainUiState,
                onCaptureClick = {
                    navController.navigate(Screen.Camera.route)
                },
                onGalleryClick = {
                    showGalleryPicker = true
                },
                onModelsClick = {
                    // TODO: Navigate to models screen
                    showError = "Models screen coming soon!"
                },
                onHistoryClick = {
                    // TODO: Navigate to history screen
                    showError = "History screen coming soon!"
                },
                onSettingsClick = {
                    // TODO: Navigate to settings screen
                    showError = "Settings screen coming soon!"
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { bitmap ->
                    navController.popBackStack()
                    detectionViewModel.detectPest(bitmap)
                },
                onError = { error ->
                    showError = error
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Results.route) {
            ResultsScreen(
                result = detectionUiState.detectionResult,
                bitmap = detectionUiState.capturedBitmap,
                onSave = {
                    // Already saved automatically if confidence meets threshold
                    showError = "Result saved to history!"
                },
                onRetry = {
                    detectionViewModel.clearResult()
                    navController.popBackStack(Screen.Main.route, false)
                },
                onBack = {
                    detectionViewModel.clearResult()
                    navController.popBackStack(Screen.Main.route, false)
                }
            )
        }
    }
}