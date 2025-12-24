# 🐛 Pest Detection App
An AI-powered Android application for detecting and classifying pest damage in sugarcane crops using advanced machine learning models.

---

## 📱 Features
- 🎯 **11 AI Models** - Choose from multiple models for different accuracy/speed tradeoffs
- 📸 **Easy to Use** - Simply take a photo or select from gallery
- 📊 **Instant Results** - Get pest classification in seconds
- 🔌 **Works Offline** - 5 optimized TFLite models bundled for offline use
- 📥 **On-Demand Models** - Download additional models as needed
- 🎨 **Modern UI** - Clean, intuitive Material Design interface
- 🔒 **Crash-Free** - Hardened camera & gallery pipelines with software bitmap conversion
- 💾 **Memory Optimized** - Smart model selection prevents OOM crashes
- 📝 **Comprehensive Logging** - Detailed diagnostics for debugging

---

## 🐞 Detects 11 Pest Types
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

---

## 📥 Installation

### Option 1: Download APK (Recommended)
1. Download the latest APK from [Releases](https://github.com/SERVER-246/pest-detection-app/releases)
2. Transfer `app-release.apk` to your Android device
3. Enable "Install from Unknown Sources" in Settings → Security
4. Tap the APK file to install
5. Grant camera and storage permissions when prompted

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/SERVER-246/pest-detection-app.git
cd pest-detection-app

# Build the release APK
./gradlew assembleRelease

# Find APK at: app/build/outputs/apk/release/app-release.apk
```

### System Requirements
- **Minimum:** Android 7.0 (API 24)
- **Target:** Android 14 (API 35)
- **Recommended Storage:** 100 MB free space
- **Recommended RAM:** 512 MB+ available for model inference
- **Permissions Required:**
  - Camera (for capturing images)
  - Storage (for saving detection results)

---

## 🚀 Usage

### 1. Launch the App
Open Intelli_PEST from your app drawer. The bundled `mobilenet_v2.tflite` model (~3MB) is ready immediately for offline detection.

### 2. Capture or Select Image
- **Camera:** Tap "Camera" → Grant permission → Capture image of affected crop
- **Gallery:** Tap "Gallery" → Select existing image from your device

### 3. View Results
- Detection runs automatically after image selection
- Results display:
  - Detected pest type
  - Confidence percentage
  - Processing time
  - All prediction alternatives

### 4. Switch Models (Optional)
- Go to "AI Models" screen
- Choose from 5 bundled models (MobileNet, YOLO, EfficientNet, etc.)
- Download additional models if needed

---

## 🔧 Technical Details

### Framework
- **ML Runtime:** TensorFlow Lite (Migrated from ONNX Runtime)
- **Architecture:** Clean Architecture with Repository pattern
- **UI Framework:** Jetpack Compose with Material Design 3
- **Camera:** CameraX with lifecycle-aware binding
- **Database:** Room for detection history
- **Async:** Kotlin Coroutines & Flow

### Models
The app uses **TensorFlow Lite (.tflite)** models for efficient on-device inference.
Original **ONNX (.onnx)** models are also available in the repository for research and reference purposes.

### Key Components
- **BitmapUtils:** Centralized software bitmap conversion (prevents HARDWARE bitmap crashes)
- **DetectPestUseCase:** Orchestrates detection flow with structured logging
- **PestDetectionRepository:** Manages model loading, inference, and persistence
- **InferenceEngine:** ONNX model wrapper with preprocessing pipeline
- **ImageValidator:** Validates crop images before detection

### Build Configuration
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 35 (Android 14+)
- **Compile SDK:** API 35
- **ProGuard:** Enabled for release (R8 optimization)
- **Signing:** server246 keystore

---

## 📊 Model Performance

All models trained on a comprehensive sugarcane pest dataset with validated accuracy metrics.

### Bundled Model
- **resnet50.onnx** (~98 MB) - Deep residual network, excellent balance of accuracy and speed, works offline

### Downloadable Models (via GitHub)
- Super Ensemble (~544 MB) - Highest accuracy for high-end devices
- AlexNet
- EfficientNet-B0
- MobileNet-V3
- Inception-V3
- DarkNet53
- YOLO-11n
- Attention Fusion
- Concatenation Fusion
- Cross-Attention Fusion

Performance varies based on:
- Image quality and lighting conditions
- Pest visibility and positioning
- Device hardware capabilities

---

## 🧪 Testing & Quality

### Automated Tests
- **Unit Tests:** `./gradlew testDebugUnitTest`
- **Instrumentation Tests:** `./gradlew connectedDebugAndroidTest`
- **Lint Checks:** `./gradlew lintDebug`

### Manual Testing
- Camera capture validated on emulator & physical devices
- Gallery import tested with diverse image formats
- Bitmap conversion pipeline verified with logcat traces
- See `MANUAL_TEST_LOG.md` for detailed test cases

### Known Warnings
- Deprecated Compose APIs (LocalLifecycleOwner, Icons.Filled.ArrowBack)
- Deprecated WindowManager statusBarColor (Android 11+)
- Non-critical; scheduled for future updates

---

## 📄 Documentation

- **DEEP_ANALYSIS_REPORT.md** - Complete technical analysis of camera/gallery crash fixes
- **MANUAL_TEST_LOG.md** - Step-by-step manual test procedures and results
- **VERIFICATION_REPORT.md** - Build validation and testing summary
- **TEST_DOCUMENTATION.md** - Comprehensive test suite documentation

---

## 🛠️ Development

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 35
- Gradle 8.0+

### Build Commands
```bash
# Clean build
./gradlew clean assembleDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest

# Build release APK
./gradlew assembleRelease

# Install debug APK on connected device
./gradlew installDebug
```

### Debugging
Enable verbose logging for key components:
```bash
adb logcat | findstr "BitmapUtils DetectPestUseCase PestDetectionRepo CameraScreen GalleryPicker"
```

---

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes with clear messages
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

### Contribution Guidelines
- Follow Kotlin coding conventions
- Add unit tests for new features
- Update documentation for API changes
- Ensure `./gradlew lintDebug` passes before submitting

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **ONNX Runtime** by Microsoft
- **Material Design 3** by Google
- **CameraX** by Android Jetpack
- **Jetpack Compose** UI toolkit
- Sugarcane pest research community

---

## 📞 Support

For issues, questions, or feature requests:
- Open an issue on [GitHub Issues](https://github.com/SERVER-246/pest-detection-app/issues)
- Contact: server246@example.com

---

## 🔄 Version History

### v1.0.0 (December 10, 2025)
- ✅ Initial release with 11 pest detection models
- ✅ Camera capture with lifecycle-aware CameraX
- ✅ Gallery import with robust bitmap conversion
- ✅ HARDWARE bitmap crash fixes (BitmapUtils)
- ✅ Structured logging across detection pipeline
- ✅ Bundled ResNet50 model (~98MB) for offline use
- ✅ Super Ensemble & other models downloadable via GitHub
- ✅ OOM (Out Of Memory) protection - optimized for mobile devices
- ✅ Material Design 3 UI
- ✅ Room database for detection history
- ✅ Comprehensive automated & manual tests

---

**Made with ❤️ for sustainable agriculture**
