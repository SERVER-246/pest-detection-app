package com.example.intelli_pest.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.intelli_pest.presentation.common.AnimatedButton
import com.example.intelli_pest.presentation.common.AnimatedOutlinedButton
import com.example.intelli_pest.ui.theme.*

/**
 * Main screen of the app with attractive UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onModelsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Intelli-PEST",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PrimaryGreen.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Welcome card
                WelcomeCard()

                Spacer(modifier = Modifier.height(24.dp))

                // Main action buttons
                MainActionButtons(
                    onCaptureClick = onCaptureClick,
                    onGalleryClick = onGalleryClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Current model info
                uiState.selectedModel?.let { model ->
                    CurrentModelCard(model = model, onChangeClick = onModelsClick)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Quick actions
                QuickActionsSection(
                    onModelsClick = onModelsClick,
                    onHistoryClick = onHistoryClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Recent detections
                if (uiState.recentDetections.isNotEmpty()) {
                    RecentDetectionsSection(
                        detections = uiState.recentDetections,
                        onViewAllClick = onHistoryClick
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Features info
                FeaturesSection()

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = PrimaryGreen
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Welcome to Intelli-PEST",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "AI-powered pest detection for sugarcane crops",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MainActionButtons(
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column {
        // Camera button (primary action)
        AnimatedButton(
            onClick = onCaptureClick,
            modifier = Modifier.fillMaxWidth(),
            text = "Capture Image",
            icon = Icons.Default.CameraAlt,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gallery button
        AnimatedOutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier.fillMaxWidth(),
            text = "Choose from Gallery",
            icon = Icons.Default.PhotoLibrary,
            borderColor = PrimaryGreen
        )
    }
}

@Composable
private fun CurrentModelCard(
    model: com.example.intelli_pest.domain.model.ModelInfo,
    onChangeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
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
                    "Current Model",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onChangeClick) {
                    Text("Change")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    model.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Default.CheckCircle,
                    text = model.getAccuracyPercentage(),
                    color = SuccessGreen
                )
                InfoChip(
                    icon = Icons.Default.Speed,
                    text = "${model.inferenceSpeedMs}ms",
                    color = InfoBlue
                )
                InfoChip(
                    icon = Icons.Default.Storage,
                    text = model.getSizeFormatted(),
                    color = SecondaryYellow
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onModelsClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Column {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Dashboard,
                title = "Models",
                subtitle = "View all models",
                onClick = onModelsClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.History,
                title = "History",
                subtitle = "Past detections",
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecentDetectionsSection(
    @Suppress("UNUSED_PARAMETER") detections: List<com.example.intelli_pest.domain.model.DetectionResult>,
    onViewAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Detections",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onViewAllClick) {
                Text("View All")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Coming soon - Recent detection history",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeaturesSection() {
    Column {
        Text(
            "Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        FeatureItem(
            icon = Icons.Default.Verified,
            title = "11 AI Models",
            description = "Multiple models for different accuracy/speed tradeoffs"
        )
        FeatureItem(
            icon = Icons.Default.OfflinePin,
            title = "Offline Detection",
            description = "Works without internet using bundled model"
        )
        FeatureItem(
            icon = Icons.Default.Science,
            title = "High Accuracy",
            description = "Detects 11 different pest types with high precision"
        )
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = PrimaryGreen.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

