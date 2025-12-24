# Intelli-PEST Application Summary
## Build Date: December 24, 2025

---

## ✅ BUILD STATUS: SUCCESSFUL

**APK Files:** 
- `intelli_pest_20241224_build.apk` (Debug APK)
- Located in: `D:\App\Intelli_PEST\`
- Also at: `D:\App\Intelli_PEST\app\build\outputs\apk\debug\app-debug.apk`

**Models Bundled:**
- ✅ `student_model.onnx` - ONNX Runtime
- ✅ `student_model.pt` - PyTorch Mobile

---

## 🎯 Application Overview

**Intelli-PEST** is an AI-powered Android application for detecting and classifying pest damage in sugarcane crops using advanced machine learning.

### Key Features
- 📸 **Camera Capture** - Take photos for instant pest detection
- 🖼️ **Gallery Import** - Select existing images for analysis
- 🔄 **Dual Runtime Support** - ONNX Runtime & PyTorch Mobile
- 🔍 **Image Validation** - Filters unrelated/low-quality images
- 📊 **Detection History** - Stores past detection results
- 📝 **Comprehensive Logging** - Debug tracking system
- ⚙️ **Configurable Settings** - Confidence threshold, runtime selection

---

## 🐛 Pest Detection Classes (11 Classes)

| Index | Class Name | Display Name |
|-------|------------|--------------|
| 0 | HEALTHY | Healthy |
| 1 | INTERNODE_BORER | Internode Borer |
| 2 | PINK_BORER | Pink Borer |
| 3 | RAT_DAMAGE | Rat Damage |
| 4 | STALK_BORER | Stalk Borer |
| 5 | TOP_BORER | Top Borer |
| 6 | ARMY_WORM | Army Worm |
| 7 | MEALY_BUG | Mealy Bug |
| 8 | PORCUPINE_DAMAGE | Porcupine Damage |
| 9 | ROOT_BORER | Root Borer |
| 10 | TERMITE | Termite |

---

## 🧠 ML Models & Runtimes

### Bundled Models
| Model File | Runtime | Format |
|------------|---------|--------|
| `student_model.onnx` | ONNX Runtime | ONNX |
| `student_model.pt` | PyTorch Mobile | PyTorch |

### Model Specifications
- **Input Size:** 256 × 256 × 3 (RGB)
- **Output:** 11 classes with softmax probabilities
- **Normalization:** ImageNet (mean: [0.485, 0.456, 0.406], std: [0.229, 0.224, 0.225])

### Runtime Selection
Users can switch between:
1. **ONNX Runtime** (Default) - Cross-platform, recommended for stability
2. **PyTorch Mobile** - Native PyTorch inference

---

## 📁 Project Structure

```
app/src/main/
├── assets/models/
│   ├── student_model.onnx    # ONNX model
│   └── student_model.pt      # PyTorch model
├── java/com/example/intelli_pest/
│   ├── data/
│   │   ├── model/            # Data entities
│   │   ├── repository/       # Repository implementations
│   │   └── source/local/     # DataStore, Room DB
│   ├── di/                   # Dependency injection
│   ├── domain/
│   │   ├── model/            # Domain models
│   │   ├── repository/       # Repository interfaces
│   │   └── usecase/          # Business logic
│   ├── ml/                   # Machine Learning
│   │   ├── InferenceEngine.kt       # Main ML orchestrator
│   │   ├── ONNXModelWrapper.kt      # ONNX Runtime wrapper
│   │   ├── PyTorchModelWrapper.kt   # PyTorch wrapper
│   │   ├── ImagePreprocessor.kt     # Image preprocessing
│   │   └── ImageValidator.kt        # Image validation
│   ├── presentation/         # UI Layer
│   │   ├── common/           # Reusable components
│   │   ├── detection/        # Detection screen
│   │   ├── main/             # Main screen
│   │   ├── models/           # Runtime selection
│   │   ├── settings/         # Settings screen
│   │   └── splash/           # Splash screen
│   └── util/                 # Utilities (AppLogger)
└── res/                      # Resources
```

---

## 🔧 Key Components

### 1. InferenceEngine
- Central ML orchestrator
- Manages ONNX and PyTorch wrappers
- Handles model loading based on selected runtime
- Coordinates image validation and inference

### 2. ONNXModelWrapper
- Wraps Microsoft's ONNX Runtime
- Dynamic input size detection
- Retry logic for model loading
- Memory-mapped model loading

### 3. PyTorchModelWrapper
- Wraps Meta's PyTorch Mobile
- Uses TensorImageUtils for preprocessing
- Asset-to-cache model copying
- Native PyTorch inference

### 4. ImageValidator
- Filters unrelated images
- Color distribution analysis
- Texture complexity measurement
- Minimum confidence enforcement

### 5. PestDetectionRepository
- Orchestrates detection pipeline
- Runtime-aware model path resolution
- Unrelated image detection
- Detection history management

---

## 🔄 Detection Pipeline Flow

```
1. User captures/selects image
         ↓
2. Convert to software Bitmap (ARGB_8888)
         ↓
3. ImageValidator checks image suitability
         ↓
4. Resolve model path based on runtime (ONNX/PyTorch)
         ↓
5. InferenceEngine loads model if not loaded
         ↓
6. ImagePreprocessor resizes to 256x256, normalizes
         ↓
7. Selected runtime (ONNX/PyTorch) runs inference
         ↓
8. Apply softmax to get probabilities
         ↓
9. Check for unrelated image (low confidence/entropy)
         ↓
10. Save result to Room database
         ↓
11. Display results to user
```

---

## 📱 Screens

| Screen | Description |
|--------|-------------|
| **Splash** | App branding with ICAR-ISRI attribution |
| **Main** | Home with camera/gallery buttons, runtime info |
| **Detection** | Shows detection results and predictions |
| **Model Selection** | Switch between ONNX/PyTorch runtime |
| **Settings** | Confidence threshold, tracking mode, logs |
| **History** | Past detection results |

---

## 📦 Dependencies

### ML Libraries
```kotlin
// ONNX Runtime
implementation("com.microsoft.onnxruntime:onnxruntime-android:1.16.3")

// PyTorch Mobile
implementation("org.pytorch:pytorch_android_lite:2.1.0")
implementation("org.pytorch:pytorch_android_torchvision_lite:2.1.0")
```

### Key Android Libraries
- Jetpack Compose (Material 3)
- CameraX
- Room Database
- DataStore Preferences
- Navigation Compose
- Coil (Image loading)
- Lottie (Animations)

---

## 🔒 Permissions

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## ✅ Quality Assurance

### Code Quality
- All files checked for errors
- Only IDE warnings remain (unused functions kept for API compatibility)
- Proper null safety handling
- Locale-aware string formatting

### Files Verified
- ✅ InferenceEngine.kt
- ✅ ONNXModelWrapper.kt
- ✅ PyTorchModelWrapper.kt
- ✅ ImagePreprocessor.kt
- ✅ ImageValidator.kt
- ✅ PestDetectionRepositoryImpl.kt
- ✅ DetectionViewModel.kt
- ✅ MainViewModel.kt
- ✅ MainScreen.kt
- ✅ ModelSelectionScreen.kt
- ✅ SettingsViewModel.kt
- ✅ build.gradle.kts

---

## 🚀 Installation

1. Transfer `intelli_pest_20241224_build.apk` to Android device
2. Enable "Install from unknown sources" if needed
3. Install the APK
4. Grant camera and storage permissions when prompted
5. App is ready to use!

---

## 📞 Support

- **GitHub:** github.com/SERVER-246/pest-detection-app
- **Developer:** ICAR-ISRI Team

---

*Document generated: December 24, 2025*

