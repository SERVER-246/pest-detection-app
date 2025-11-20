package com.example.pest_1.domain.model

/**
 * Result of image classification
 */
data class PredictionResult(
    val className: String,
    val confidence: Float,
    val allPredictions: List<ClassPrediction>,
    val meetsThreshold: Boolean,
    val inferenceTimeMs: Long = 0
)

/**
 * Individual class prediction
 */
data class ClassPrediction(
    val className: String,
    val confidence: Float
)

