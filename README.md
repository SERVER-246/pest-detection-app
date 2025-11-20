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

## 🏗️ Architecture

```
app/
├── MainActivity.kt              # Main UI and user interaction
├── OnnxModelManager.kt          # ML inference engine
├── data/
│   └── model/
│       ├── ModelInfo.kt         # Model metadata catalog
│       ├── ModelRepository.kt   # Model access layer
│       └── ModelDownloader.kt   # Download management
└── domain/
    └── model/
        └── PredictionResult.kt  # Prediction data classes
```

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

## 🔧 Configuration

### Enable Model Downloads
1. Upload models to GitHub Releases using:
   ```powershell
   .\create-github-models.ps1
   ```

2. Update `app/src/main/java/com/example/pest_1/data/model/ModelInfo.kt`:
   ```kotlin
   private const val MODEL_BASE_URL = "https://github.com/YOUR_USERNAME/pest-detection-models/releases/download/v1.0"
   ```

### Build Variants
```powershell
# Debug build (with logging)
.\gradlew.bat assembleDebug

# Release build (optimized)
.\gradlew.bat assembleRelease
```

## 🧪 Testing

See **[TESTING_DEPLOYMENT_GUIDE.md](TESTING_DEPLOYMENT_GUIDE.md)** for complete testing checklist.

### Quick Test
1. ✅ App installs (no "too large" error)
2. ✅ App launches without crash
3. ✅ Image selection works
4. ✅ Classification produces results
5. ✅ Results display correctly

### Test with Logs
```powershell
adb logcat | Select-String "OnnxModelManager"
```

## 📁 Important Files

### Documentation
- `IMPLEMENTATION_SUMMARY.md` - Complete fix implementation details
- `TESTING_DEPLOYMENT_GUIDE.md` - Full testing checklist
- `FIXES_IMPLEMENTED.md` - Technical details of bug fixes
- `ACTION_CHECKLIST.md` - Quick reference

### Scripts
- `COMPREHENSIVE_SETUP.ps1` - Complete automated setup
- `VALIDATE.ps1` - Pre-build validation
- `cleanup_assets.ps1` - Asset cleanup only
- `create-github-models.ps1` - Model packaging

## 🐛 Troubleshooting

### APK Still Too Large (>100 MB)
```powershell
# Verify only mobilenet_v2 in assets
Get-ChildItem "app\src\main\assets\models" -Directory
# Should show only: mobilenet_v2

# If other models exist, run cleanup
.\cleanup_assets.ps1

# Rebuild
.\gradlew.bat clean assembleDebug
```

### Classification Not Working
```powershell
# Check device logs
adb logcat | Select-String "pest_1"

# Verify model files exist
ls app\src\main\assets\models\mobilenet_v2
# Should see: model.onnx, labels.txt, metadata.json, class_mapping.json
```

### Can't Download Models
- MODEL_BASE_URL must be configured with real GitHub URL
- Device must have internet connection
- Test URL in browser first

### App Crashes
```powershell
# View crash logs
adb logcat *:E | Select-String "pest_1"

# Check device Android version (needs 7.0+)
adb shell getprop ro.build.version.release
```

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

## 🏭 Production Deployment

### 1. Configure Signing
Create keystore and update `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("path/to/keystore.jks")
        storePassword = "your-password"
        keyAlias = "your-alias"
        keyPassword = "your-key-password"
    }
}
```

### 2. Build Release APK
```powershell
.\gradlew.bat assembleRelease
```

### 3. Test Release Build
- Install on multiple devices
- Verify all features work
- Check performance metrics

### 4. Deploy
- Google Play Store (recommended)
- Direct APK distribution
- Enterprise deployment (MDM)

## 📊 Technical Stack

- **Language:** Kotlin
- **UI:** Android XML Views + Material Design
- **ML Framework:** ONNX Runtime 1.16.3
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Build System:** Gradle 8.13

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

[Add your license here]

## 📞 Support

For issues, questions, or contributions:
- Check documentation in `/docs`
- Review troubleshooting section above
- Check device logs with `adb logcat`

## 🎉 Credits

- ONNX Runtime by Microsoft
- Model training dataset: ICAR-ISRI Crop Protection Division Dataset
- UI Design: Material Design 3

---

## 🚨 Critical Notes

### Before First Build
⚠️ **IMPORTANT:** Must run asset cleanup to reduce APK from 2GB to 40-60 MB:
```powershell
.\COMPREHENSIVE_SETUP.ps1
```

### Model Hosting
📤 Models must be uploaded to GitHub Releases for download feature:
```powershell
.\create-github-models.ps1
```

### Testing Priority
🧪 **Test this first:** MobileNet V2 classification with bundled model (no internet required)

---

**Status:** ✅ Ready for Testing  
**Last Updated:** November 20, 2025  
**Version:** 1.0.0

