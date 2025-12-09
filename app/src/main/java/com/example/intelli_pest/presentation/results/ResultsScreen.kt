package com.example.intelli_pest.presentation.results

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.PestPrediction
import com.example.intelli_pest.domain.model.PestType
import com.example.intelli_pest.presentation.common.AnimatedButton
import com.example.intelli_pest.ui.theme.*

/**
 * Results screen showing detection results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    result: DetectionResult?,
    bitmap: Bitmap?,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detection Results") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
        if (result != null && bitmap != null) {
            SuccessResultsContent(
                result = result,
                bitmap = bitmap,
                onSave = onSave,
                onRetry = onRetry,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            ErrorResultsContent(
                onRetry = onRetry,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun SuccessResultsContent(
    result: DetectionResult,
    bitmap: Bitmap,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Main result card
        item {
            MainResultCard(result = result)
        }

        // Confidence info
        item {
            ConfidenceCard(result = result)
        }

        // All predictions
        if (result.allPredictions.isNotEmpty()) {
            item {
                Text(
                    "All Predictions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(result.allPredictions.take(5)) { prediction ->
                PredictionItem(prediction = prediction)
            }
        }

        // Action buttons
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedButton(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    text = "Save to History",
                    icon = Icons.Default.Save,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    )
                )

                AnimatedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    text = "Detect Another",
                    icon = Icons.Default.Refresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun MainResultCard(result: DetectionResult) {
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (result.pestType) {
                PestType.HEALTHY -> SuccessGreen.copy(alpha = 0.1f)
                else -> ErrorRed.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = when (result.pestType) {
                    PestType.HEALTHY -> SuccessGreen
                    else -> ErrorRed
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = when (result.pestType) {
                        PestType.HEALTHY -> Icons.Default.CheckCircle
                        else -> Icons.Default.BugReport
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pest type
            Text(
                result.pestType.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when (result.pestType) {
                    PestType.HEALTHY -> SuccessGreen
                    else -> ErrorRed
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                result.pestType.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConfidenceCard(result: DetectionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Confidence",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    result.getConfidencePercentage(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { result.confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryGreen,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    icon = Icons.Default.Speed,
                    label = "Processing Time",
                    value = "${result.processingTimeMs}ms"
                )
                InfoItem(
                    icon = Icons.Default.Memory,
                    label = "Model",
                    value = result.modelUsed.take(10)
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PredictionItem(prediction: PestPrediction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                prediction.pestType.displayName,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { prediction.confidence },
                    modifier = Modifier
                        .width(80.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    prediction.getConfidencePercentage(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ErrorResultsContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = ErrorRed
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Detection Failed",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Something went wrong during detection. Please try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedButton(
                onClick = onRetry,
                text = "Try Again",
                icon = Icons.Default.Refresh
            )
        }
    }
}

