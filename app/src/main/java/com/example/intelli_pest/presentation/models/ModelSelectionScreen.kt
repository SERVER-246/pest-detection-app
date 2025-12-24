package com.example.intelli_pest.presentation.models

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.intelli_pest.presentation.main.MainViewModel
import com.example.intelli_pest.ui.theme.*
import com.example.intelli_pest.util.AppLogger

private const val TAG = "ModelSelectionScreen"

// Colors for runtime sections
private val ONNXColor = Color(0xFF2196F3)    // Blue for ONNX
private val ONNXLightColor = Color(0xFFE3F2FD)
private val PyTorchColor = Color(0xFFEE4C2C)  // PyTorch orange/red
private val PyTorchLightColor = Color(0xFFFFEBEE)

/**
 * Screen for selecting ML runtime
 * Simplified: Only ONNX runtime available, PyTorch coming soon
 * Uses student_model.onnx for inference
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectionScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentRuntime by viewModel.currentRuntime.collectAsState()

    Log.d(TAG, "ModelSelectionScreen | currentRuntime=$currentRuntime")

    LaunchedEffect(Unit) {
        AppLogger.logScreenOpened("ModelSelectionScreen")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Inference Engine")
                        Text(
                            text = "Select runtime for pest detection",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        AppLogger.logAction("ModelSelectionScreen", "Back_Button_Clicked", "User navigating back")
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Selection Summary
            item {
                CurrentRuntimeCard(runtime = currentRuntime)
            }

            item {
                Text(
                    text = "Available Runtimes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // ONNX Runtime Option
            item {
                RuntimeOptionCard(
                    title = "ONNX Runtime",
                    subtitle = "Microsoft's cross-platform ML inference",
                    description = "Uses student_model.onnx • Optimized for mobile",
                    icon = Icons.Default.Hub,
                    color = ONNXColor,
                    lightColor = ONNXLightColor,
                    isSelected = currentRuntime == "onnx",
                    isAvailable = true,
                    onClick = {
                        AppLogger.logAction("ModelSelectionScreen", "Runtime_Selected", "ONNX")
                        viewModel.setMLRuntime("onnx")
                    }
                )
            }

            // PyTorch Runtime Option (Now Available!)
            item {
                RuntimeOptionCard(
                    title = "PyTorch Mobile",
                    subtitle = "Native PyTorch inference",
                    description = "Uses student_model.pt • Native performance",
                    icon = Icons.Default.LocalFireDepartment,
                    color = PyTorchColor,
                    lightColor = PyTorchLightColor,
                    isSelected = currentRuntime == "tflite", // TFLITE enum repurposed for PyTorch
                    isAvailable = true, // Now implemented!
                    onClick = {
                        AppLogger.logAction("ModelSelectionScreen", "Runtime_Selected", "PyTorch")
                        viewModel.setMLRuntime("tflite") // Using tflite enum for PyTorch
                    }
                )
            }

            // Model Info Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ModelInfoCard()
            }

            // Info Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                InfoCard()
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Shows current runtime selection at the top
 */
@Composable
fun CurrentRuntimeCard(runtime: String) {
    // runtime == "onnx" for ONNX, runtime == "tflite" for PyTorch (repurposed enum)
    val isOnnx = runtime == "onnx"
    val color = if (isOnnx) ONNXColor else PyTorchColor
    val runtimeName = if (isOnnx) "ONNX Runtime" else "PyTorch Mobile"
    val modelFile = if (isOnnx) "student_model.onnx" else "student_model.pt"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Active Runtime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = runtimeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "Model: $modelFile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Runtime selection option card
 */
@Composable
fun RuntimeOptionCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    color: Color,
    lightColor: Color,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isAvailable) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> lightColor
                !isAvailable -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            isSelected -> BorderStroke(2.dp, color)
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Surface(
                    color = color.copy(alpha = if (isAvailable) 0.2f else 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isAvailable) color else color.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) {
                                if (isSelected) color else MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isSelected) {
                            Surface(
                                color = color,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        } else if (!isAvailable) {
                            Surface(
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "COMING SOON",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isAvailable) 1f else 0.6f
                        )
                    )
                }

                // Selection indicator
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (isAvailable) 0.8f else 0.5f
                )
            )

            if (isAvailable && !isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Icon(
                        Icons.Default.Rocket,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use This Runtime")
                }
            }
        }
    }
}

/**
 * Model information card
 */
@Composable
fun ModelInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Student Model",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A lightweight, optimized model trained for sugarcane pest detection. Detects 11 different pest types with high accuracy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModelStat(icon = Icons.Default.Category, value = "11", label = "Classes")
                ModelStat(icon = Icons.Default.Speed, value = "~100ms", label = "Speed")
                ModelStat(icon = Icons.Default.Verified, value = "High", label = "Accuracy")
            }
        }
    }
}

@Composable
fun ModelStat(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = PrimaryGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "About Runtimes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• ONNX Runtime: Cross-platform ML inference by Microsoft\n" +
                       "• PyTorch Mobile: Native PyTorch inference by Meta\n" +
                       "• Both use the same trained student_model\n" +
                       "• ONNX is recommended for broader compatibility\n" +
                       "• PyTorch may offer better performance on some devices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

