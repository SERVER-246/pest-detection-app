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

## 📱 Screenshots

| Home Screen | Camera | Results |
|:-----------:|:------:|:-------:|
| Beautiful main screen with quick actions | Capture crop images with guided overlay | Instant AI-powered pest detection results |

---

## ✨ Features

- 🎯 **11 AI Models** - Choose from multiple models for different accuracy/speed tradeoffs
- 📸 **Easy to Use** - Simply take a photo or select from gallery
- 📊 **Instant Results** - Get pest classification in seconds
- 🔌 **Works Offline** - All models bundled for offline use
- 🎨 **Modern UI** - Clean, intuitive Material Design 3 interface
- 🛡️ **Image Validation** - Automatically rejects non-crop images
- 📈 **Confidence Filtering** - Configurable threshold for reliable results
- 💾 **Detection History** - Save and review past detections

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

| Model | Speed | Accuracy | Size | Best For |
|-------|:-----:|:--------:|:----:|----------|
| **Super Ensemble** | ~450ms | 96% | 85MB | Best accuracy |
| Cross-Attention Fusion | ~320ms | 95% | 100MB | High accuracy |
| Attention Fusion | ~280ms | 94% | 90MB | High accuracy |
| ResNet-50 | ~200ms | 93% | 98MB | Balanced |
| Concatenation Fusion | ~250ms | 93% | 95MB | Multi-model fusion |
| DarkNet-53 | ~300ms | 92% | 160MB | YOLO backbone |
| Inception V3 | ~220ms | 92% | 90MB | Classic CNN |
| EfficientNet B0 | ~120ms | 91% | 20MB | Efficient |
| MobileNet V2 | ~80ms | 89% | 14MB | Fast mobile |
| AlexNet | ~200ms | 88% | 240MB | Classic |
| **YOLO 11n** | ~50ms | 87% | 8MB | Ultra fast |

---

## 🔧 Technical Specifications

### Requirements
| Requirement | Version |
|-------------|---------|
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 35 (Android 14+) |
| Compile SDK | API 35 |
| Kotlin | 1.9+ |
| Gradle | 8.7+ |

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Design System:** Material Design 3
- **Architecture:** Clean Architecture + MVVM
- **ML Runtime:** ONNX Runtime for Android
- **Camera:** CameraX
- **Database:** Room
- **Preferences:** DataStore
- **Navigation:** Navigation Compose

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │   Screens   │ │  ViewModels │ │   Navigation    │   │
│  │  (Compose)  │ │   (MVVM)    │ │   (NavHost)     │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
├─────────────────────────────────────────────────────────┤
│                      Domain Layer                        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │  Use Cases  │ │   Models    │ │  Repositories   │   │
│  │             │ │  (Domain)   │ │  (Interfaces)   │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
├─────────────────────────────────────────────────────────┤
│                       Data Layer                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │    Room     │ │  DataStore  │ │   ML Engine     │   │
│  │  (History)  │ │   (Prefs)   │ │  (ONNX Runtime) │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Project Structure
```
app/src/main/java/com/example/intelli_pest/
├── data/                    # Data layer
│   ├── model/               # Entity classes
│   ├── repository/          # Repository implementations
│   └── source/local/        # Local data sources
├── di/                      # Dependency injection
├── domain/                  # Domain layer
│   ├── model/               # Domain models
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Use cases
├── ml/                      # Machine learning
│   ├── ImagePreprocessor    # Image preprocessing
│   ├── ImageValidator       # Image validation
│   ├── InferenceEngine      # ONNX inference
│   └── OnnxModelWrapper     # Model wrapper
├── presentation/            # Presentation layer
│   ├── camera/              # Camera screen
│   ├── common/              # Shared components
│   ├── detection/           # Detection ViewModel
│   ├── gallery/             # Gallery picker
│   ├── main/                # Main screen
│   ├── navigation/          # Navigation
│   └── results/             # Results screen
└── ui/theme/                # App theme
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android device or emulator (API 24+)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/SERVER-246/pest-detection-app.git
   cd pest-detection-app
   ```

2. **Open in Android Studio**
   ```
   File → Open → Select the cloned folder
   ```

3. **Sync Gradle**
   ```
   File → Sync Project with Gradle Files
   ```

4. **Download Model Files** ⚠️ Important
   
   Model files are not included in the repository due to size limits.
   
   **Option A: Download from Releases**
   - Go to [Releases](https://github.com/SERVER-246/pest-detection-app/releases)
   - Download `models.zip`
   - Extract to `app/src/main/assets/models/`
   
   **Option B: Use your own models**
   - Train your own models using PyTorch/TensorFlow
   - Export to ONNX format
   - Place in `app/src/main/assets/models/`
   
   **Required model structure:**
   ```
   app/src/main/assets/models/
   ├── super_ensemble.onnx    # Required (default model)
   └── [other models].onnx    # Optional
   ```

5. **Build the project**
   ```bash
   ./gradlew build
   ```

6. **Run on device/emulator**
   ```bash
   ./gradlew installDebug
   ```
   Or press `Shift+F10` in Android Studio

---

## 📱 Usage

### Basic Flow

1. **Launch the app** → Beautiful home screen appears
2. **Capture Image** → Tap "Capture Image" to use camera
3. **Or Select from Gallery** → Tap "Choose from Gallery"
4. **Wait for Detection** → AI processes the image (2-30 seconds)
5. **View Results** → See pest type with confidence score
6. **Take Action** → Save to history or detect another

### Tips for Best Results

- 📸 Use clear, well-lit images
- 🎯 Center the affected area in frame
- 📐 Maintain proper distance (not too close/far)
- 🌿 Ensure leaf/crop is clearly visible
- ☀️ Avoid extreme lighting conditions

---

## 📁 Model Files

### Location
```
app/src/main/assets/models/
├── super_ensemble.onnx      # Required (default)
├── alexnet.onnx             # Optional
├── attention_fusion.onnx    # Optional
├── concatination_fusion.onnx
├── cross_attention_fusion.onnx
├── darknet53.onnx
├── efficentnet_b0.onnx
├── inception_v3.onnx
├── mobilenet_v2.onnx
├── resnet50.onnx
└── yolo_11n.onnx
```

### Model Format
- **Input Shape:** `[1, 3, 224, 224]` (NCHW format)
- **Output Shape:** `[1, 11]` (11 pest classes)
- **Data Type:** float32
- **Format:** ONNX (opset 11-13)

### Training Your Own Models

If you want to train custom models:

1. Prepare dataset with 11 pest classes
2. Train using PyTorch/TensorFlow
3. Export to ONNX format:
   ```python
   torch.onnx.export(model, dummy_input, "model.onnx",
                     input_names=['input'],
                     output_names=['output'],
                     dynamic_axes={'input': {0: 'batch'},
                                   'output': {0: 'batch'}})
   ```
4. Place in `assets/models/` folder

---

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] App launches without crash
- [ ] Camera opens and captures images
- [ ] Gallery selection works
- [ ] Detection completes successfully
- [ ] Results display correctly
- [ ] Non-crop images are rejected
- [ ] All models load and work
- [ ] History saves correctly

---

## 🛠️ Build Variants

| Variant | Description |
|---------|-------------|
| **debug** | Development build with logging |
| **release** | Production build with ProGuard |

### Generate Release APK
```bash
./gradlew assembleRelease
```

### Generate App Bundle (AAB)
```bash
./gradlew bundleRelease
```

---

## 📊 Performance

### Benchmarks (Mid-range device)

| Model | Load Time | Inference | Memory |
|-------|:---------:|:---------:|:------:|
| YOLO 11n | 200ms | 50ms | 150MB |
| MobileNet V2 | 300ms | 80ms | 180MB |
| EfficientNet B0 | 400ms | 120ms | 200MB |
| Super Ensemble | 800ms | 450ms | 350MB |

### Optimization Tips

1. **For speed:** Use YOLO 11n or MobileNet V2
2. **For accuracy:** Use Super Ensemble
3. **For balance:** Use EfficientNet B0
4. **Memory issues:** Close background apps

---

## 🐛 Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| App crashes on launch | Check if model files exist in assets |
| Camera not working | Grant camera permission in settings |
| Detection always fails | Use clearer images with visible crops |
| Out of memory | Use smaller models (YOLO, MobileNet) |
| Slow performance | First run is slower (model loading) |

### Debug Mode

Enable verbose logging:
```kotlin
// In InferenceEngine.kt
private const val DEBUG = true
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable/function names
- Add comments for complex logic
- Write unit tests for new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 SERVER-246

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

- [ONNX Runtime](https://onnxruntime.ai/) by Microsoft
- [Material Design 3](https://m3.material.io/) by Google
- [Jetpack Compose](https://developer.android.com/jetpack/compose) by Google
- [CameraX](https://developer.android.com/training/camerax) by Google
- Sugarcane pest research community

---

## 📞 Contact

**Project Link:** [https://github.com/SERVER-246/pest-detection-app](https://github.com/SERVER-246/pest-detection-app)

---

## 📈 Roadmap

- [x] Core detection functionality
- [x] 11 AI models integration
- [x] Camera & gallery support
- [x] Material Design 3 UI
- [x] Image validation
- [x] Detection history
- [ ] Model management UI
- [ ] Settings screen
- [ ] Export reports
- [ ] Cloud sync
- [ ] Multi-language support
- [ ] iOS version

---

<p align="center">
  <b>Built with ❤️ for sugarcane farmers</b>
</p>

<p align="center">
  ⭐ Star this repo if you find it useful!
</p>

