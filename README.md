# 🐛 Intelli-PEST - AI Pest Detection App

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/ONNX-005CED?style=for-the-badge&logo=onnx&logoColor=white" alt="ONNX"/>
</p>

<p align="center">
  <b>An AI-powered Android application for detecting and classifying pest damage in sugarcane crops using advanced machine learning models.</b>
</p>

---

## 📥 Quick Install

### Download APK
1. Go to [Releases](https://github.com/SERVER-246/pest-detection-app/releases)
2. Download `intelli_pest-raw.apk` (~570MB)
3. Install on your Android device (Android 7.0+)

> **Note:** The APK includes the Super Ensemble model (best accuracy). Additional models can be downloaded within the app.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🎯 **11 AI Models** | Choose from multiple models for different accuracy/speed tradeoffs |
| 📸 **Easy to Use** | Simply take a photo or select from gallery |
| 📊 **Instant Results** | Get pest classification in seconds |
| 🔌 **Works Offline** | Super Ensemble model bundled for offline use |
| 📥 **On-Demand Models** | Download additional models as needed |
| 🎨 **Modern UI** | Clean, intuitive Material Design 3 interface |
| 🛡️ **Image Validation** | Automatically rejects non-crop images |
| 📈 **Confidence Filtering** | Configurable threshold for reliable results |

---

## 🐞 Detects 11 Pest Types

| # | Pest Type | Description |
|:-:|-----------|-------------|
| 1 | **Armyworm** | Fall armyworm damage on sugarcane leaves |
| 2 | **Healthy** | No pest damage detected - healthy crop |
| 3 | **Internode Borer** | Damage caused by internode borer |
| 4 | **Mealy Bug** | Mealy bug infestation |
| 5 | **Pink Borer** | Pink borer damage on sugarcane |
| 6 | **Porcupine Damage** | Physical damage caused by porcupines |
| 7 | **Rat Damage** | Damage caused by rats |
| 8 | **Root Borer** | Root borer infestation |
| 9 | **Stalk Borer** | Stalk borer damage |
| 10 | **Termite** | Termite infestation damage |
| 11 | **Top Borer** | Top shoot borer damage |

---

## 📊 Available AI Models

### Bundled Model (Included in APK)
| Model | Accuracy | Speed | Size | Status |
|-------|:--------:|:-----:|:----:|:------:|
| **Super Ensemble** ⭐ | 96% | ~450ms | 544MB | ✅ Included |

### Downloadable Models (via GitHub)
| Model | Accuracy | Speed | Size | Download |
|-------|:--------:|:-----:|:----:|:--------:|
| Cross-Attention Fusion | 95% | ~320ms | 399MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/cross_attention_fusion.onnx) |
| Attention Fusion | 94% | ~280ms | 371MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/attention_fusion.onnx) |
| Concatenation Fusion | 93% | ~250ms | 373MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/concatination_fusion.onnx) |
| ResNet-50 | 93% | ~200ms | 98MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/resnet50.onnx) |
| DarkNet-53 | 92% | ~300ms | 81MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/darknet53.onnx) |
| Inception V3 | 92% | ~220ms | 91MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/inception_v3.onnx) |
| EfficientNet B0 | 91% | ~120ms | 18MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/efficientnet_b0.onnx) |
| MobileNet V2 | 89% | ~80ms | 12MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/mobilenet_v2.onnx) |
| AlexNet | 88% | ~200ms | 172MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/alexnet.onnx) |
| YOLO 11 Nano | 87% | ~50ms | 18MB | [Download](https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models/yolo_11n.onnx) |

---

## 📱 Installation Guide

### Method 1: Direct APK Install (Recommended)

1. **Download the APK**
   - Download `intelli_pest-raw.apk` from [Releases](https://github.com/SERVER-246/pest-detection-app/releases)

2. **Enable Unknown Sources** (if not already enabled)
   - Go to `Settings → Security → Unknown Sources` → Enable
   - Or when prompted, tap "Settings" and allow installation

3. **Install the APK**
   - Open the downloaded APK file
   - Tap "Install"
   - Wait for installation to complete (~1-2 minutes)

4. **Launch the App**
   - Find "Intelli-PEST" in your app drawer
   - Grant camera permission when prompted
   - Start detecting pests! 🐛

### Method 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/SERVER-246/pest-detection-app.git
cd pest-detection-app

# Build the APK
./gradlew assembleRelease

# APK will be at: app/build/outputs/apk/release/app-release.apk
```

---

## 📱 How to Use

### Step 1: Launch App
Open Intelli-PEST from your app drawer

### Step 2: Capture or Select Image
- **Camera:** Tap "Capture Image" → Take photo of affected crop
- **Gallery:** Tap "Choose from Gallery" → Select existing image

### Step 3: View Results
- AI processes the image (2-30 seconds depending on model)
- See detected pest type with confidence score
- View detailed analysis and recommendations

### Step 4: Take Action
- Save results to history
- Detect another image
- Switch models for comparison

---

## 🔧 Technical Specifications

### Requirements
| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| Android Version | 7.0 (API 24) | 10.0+ (API 29+) |
| RAM | 2GB | 4GB+ |
| Storage | 1GB free | 3GB+ free |
| Camera | Required | High-res recommended |

### Tech Stack
| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Design System | Material Design 3 |
| Architecture | Clean Architecture + MVVM |
| ML Runtime | ONNX Runtime |
| Camera | CameraX |
| Database | Room |
| Preferences | DataStore |

---

## 🏗️ Project Structure

```
app/src/main/
├── assets/models/           # Bundled ONNX models
│   └── super_ensemble.onnx  # Default model (544MB)
├── java/com/example/intelli_pest/
│   ├── data/                # Data layer
│   │   ├── model/           # Entities
│   │   ├── repository/      # Repository implementations
│   │   └── source/local/    # Local data sources
│   ├── di/                  # Dependency injection
│   ├── domain/              # Domain layer
│   │   ├── model/           # Domain models
│   │   ├── repository/      # Repository interfaces
│   │   └── usecase/         # Use cases
│   ├── ml/                  # Machine learning
│   │   ├── ImagePreprocessor
│   │   ├── ImageValidator
│   │   ├── InferenceEngine
│   │   └── OnnxModelWrapper
│   ├── presentation/        # UI layer
│   │   ├── camera/
│   │   ├── common/
│   │   ├── detection/
│   │   ├── gallery/
│   │   ├── main/
│   │   ├── navigation/
│   │   └── results/
│   └── ui/theme/            # App theme
└── res/                     # Resources
```

---

## 🐛 Troubleshooting

### Installation Issues

| Problem | Solution |
|---------|----------|
| "App not installed" | Enable Unknown Sources in Settings |
| "Parse error" | Re-download APK, file may be corrupted |
| Installation blocked | Disable Play Protect temporarily |

### Runtime Issues

| Problem | Solution |
|---------|----------|
| App crashes on launch | Clear app data, reinstall |
| Camera not working | Grant camera permission in Settings |
| Detection fails | Use clearer images, ensure good lighting |
| Out of memory | Close background apps, use smaller models |
| Slow detection | First run is slower (model loading), try YOLO for speed |

---

## 📊 Performance Tips

1. **Best Accuracy:** Use Super Ensemble (bundled)
2. **Fastest Detection:** Download YOLO 11 Nano (50ms)
3. **Balanced Performance:** Download EfficientNet B0
4. **Low Memory Devices:** Use MobileNet V2 or YOLO 11 Nano
5. **First Run:** Slower due to model initialization (cached after)

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**SERVER-246**
- GitHub: [@SERVER-246](https://github.com/SERVER-246)

---

## 🙏 Acknowledgments

- [ONNX Runtime](https://onnxruntime.ai/) by Microsoft
- [Material Design 3](https://m3.material.io/) by Google
- [Jetpack Compose](https://developer.android.com/jetpack/compose) by Google
- [CameraX](https://developer.android.com/training/camerax) by Google
- Sugarcane pest research community

---

## 📈 Version History

### v1.0.0 (December 2025)
- ✅ Initial release
- ✅ 11 AI models (1 bundled, 10 downloadable)
- ✅ Camera & gallery support
- ✅ Material Design 3 UI
- ✅ Image validation
- ✅ Detection history
- ✅ Offline support with Super Ensemble

---

<p align="center">
  <b>Built with ❤️ for sugarcane farmers</b>
</p>

<p align="center">
  ⭐ Star this repo if you find it useful!
</p>

