# 🐛 Pest Detection Android App

An AI-powered Android application for detecting and classifying pest damage in sugarcane crops using ONNX machine learning models.

## 📱 App Overview

- **Purpose:** Identify pest types from images using on-device AI
- **Models:** 11 different ONNX models (1 bundled, 10 downloadable)
- **Accuracy:** Up to 99.96% with ensemble models
- **Platform:** Android 7.0+ (API 24+)
- **Size:** 40-60 MB APK (optimized)

## 🚀 Quick Start

### Prerequisites
- Windows with PowerShell
- Android SDK and Gradle (or Android Studio)
- USB-enabled Android device (optional, for testing)

### 1. Validate Project
```powershell
cd D:\App\Pest1
.\VALIDATE.ps1
```

### 2. Setup and Build
```powershell
.\COMPREHENSIVE_SETUP.ps1
```

This script will:
- ✅ Remove large models from assets (keeps only MobileNet V2)
- ✅ Clean build artifacts
- ✅ Build optimized debug APK
- ✅ Verify APK size (~40-60 MB)

### 3. Install on Device
```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```

### 4. Test
- Open app on device
- Select/capture a pest image
- Tap "Analyze Pest"
- View classification results

## 📊 Features

### ✅ Implemented
- 🎯 11 pre-trained ONNX models
- 📸 Camera capture and gallery selection
- 🔄 Real-time image classification
- 📥 On-demand model downloading
- 💾 Model caching for offline use
- 📈 Confidence score display
- ⚡ Optimized performance (100-300ms inference)
- 🎨 Material Design UI

### 🎯 Pest Classes Detected
- Armyworm
- Healthy (no pest)
- Internode borer
- Mealy bug
- Pink borer
- Porcupine damage
- Rat damage
- Root borer
- Stalk borer
- Termite
- Top borer


## 📦 Models

### Bundled (Always Available)
- **MobileNet V2**: 98.74% accuracy, 14 MB, ~150ms

### Downloadable (On-Demand)
| Model | Accuracy | Size | Speed |
|-------|----------|------|-------|
| DarkNet53 | 99.38% | 163 MB | 450ms |
| ResNet50 | 98.74% | 98 MB | 300ms |
| YOLO11n | 98.80% | 7 MB | 120ms |
| EfficientNet B0 | 98.50% | 20 MB | 180ms |
| Super Ensemble | 99.96% | 280 MB | 1500ms |
| ... and 5 more |


## 📈 Performance

### Expected Metrics
- **APK Size:** 40-60 MB (with 1 bundled model)
- **Install Time:** < 30 seconds
- **Inference Time:** 100-300ms (MobileNet V2)
- **Memory Usage:** < 200 MB
- **Accuracy:** 98.74% (MobileNet V2)

### Optimization Features
- ✅ ProGuard code minification
- ✅ Resource shrinking
- ✅ ABI splits for smaller APKs
- ✅ On-demand model loading
- ✅ ONNX Runtime optimizations

## 🔐 Permissions

Required permissions:
- **CAMERA** - For capturing pest images
- **READ_MEDIA_IMAGES** (Android 13+) - For gallery access
- **READ_EXTERNAL_STORAGE** (Android <13) - For gallery access
- **INTERNET** - For downloading models
- **ACCESS_NETWORK_STATE** - For checking connectivity

## 📊 Technical Stack

- **Language:** Kotlin
- **UI:** Android XML Views + Material Design
- **ML Framework:** ONNX Runtime 1.16.3
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Build System:** Gradle 8.13

## 📄 License

UnderProcess

## 📞 Support

For issues, questions, or contributions:
- Check documentation in `/docs`
- Review troubleshooting section above
- Check device logs with `adb logcat`

## 🎉 Credits

- ONNX Runtime by Microsoft
- Model training dataset: ICAR-ISRI Crop Protection Division Dataset
- UI Design: Material Design 3

**Status:** ✅ Ready for Testing  
**Last Updated:** November 20, 2025  
**Version:** 1.0.0

