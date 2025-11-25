# 🐛 Pest Detection App

An AI-powered Android application for detecting and classifying pest damage in sugarcane crops using advanced machine learning models.

## 📱 Features

- 🎯 **11 AI Models** - Choose from multiple models for different accuracy/speed tradeoffs
- 📸 **Easy to Use** - Simply take a photo or select from gallery
- 📊 **Instant Results** - Get pest classification in seconds
- 🔌 **Works Offline** - MobileNet V2 model bundled for offline use
- 📥 **On-Demand Models** - Download additional models as needed
- 🎨 **Modern UI** - Clean, intuitive Material Design interface

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

## 📲 Installation

### Download APK

**Latest Version: v1.0.1** (November 25, 2025)

**What's New in v1.0.1:**
- ✅ Fixed download dialog not showing for additional models
- ✅ Improved confidence scores (now matches system testing)
- ✅ All architecture-specific APKs available

**Download from:** [Releases Folder](https://github.com/SERVER-246/pest-detection-app/tree/main/releases)

**Choose your APK:**
- **app-universal-debug.apk** (75 MB) - **Recommended** - Works on all devices
- app-arm64-v8a-debug.apk (31 MB) - Modern ARM phones (2017+)
- app-armeabi-v7a-debug.apk (27 MB) - Older ARM phones
- app-x86-debug.apk (33 MB) - Intel tablets
- app-x86_64-debug.apk (33 MB) - Intel 64-bit devices

### Requirements
- Android 7.0 (Nougat) or higher
- 100 MB free storage space
- Camera permission (for capturing images)
- Internet connection (for downloading additional models)

### Steps
1. Download the latest APK from [Releases](https://github.com/SERVER-246/pest-detection-app/releases)
2. Enable "Install from Unknown Sources" in Android settings
3. Open the downloaded APK file
4. Follow installation prompts
5. Grant camera and storage permissions when prompted

## 🎯 How to Use

1. **Launch the app**
2. **Select a model** from the dropdown (start with MobileNet V2)
3. **Capture or select** a pest image using the buttons
4. **Tap "Analyze Pest"** to get results
5. **View detection results** with confidence scores

## 🤖 Available Models

| Model | Accuracy | Speed | Size | Notes |
|-------|----------|-------|------|-------|
| **MobileNet V2** | 98.74% | Fast (150ms) | 14 MB | ✅ Bundled - Works offline |
| YOLO11n | 98.80% | Very Fast (120ms) | 7 MB | Download required |
| EfficientNet B0 | 98.50% | Fast (180ms) | 20 MB | Download required |
| ResNet50 | 98.74% | Medium (300ms) | 98 MB | Download required |
| DarkNet53 | 99.38% | Slower (450ms) | 163 MB | Download required |
| Inception V3 | 98.58% | Medium (350ms) | 91 MB | Download required |
| AlexNet | 98.03% | Fast (200ms) | 233 MB | Download required |
| Ensemble Models | 99.76-99.96% | Slow (800-1500ms) | 145-280 MB | Highest accuracy |

*Note: Speed and size may vary by device*

## 💡 Tips

- **First time users:** Start with MobileNet V2 (bundled, no download needed)
- **Need speed:** Use YOLO11n (fastest inference)
- **Need accuracy:** Use Super Ensemble (99.96% but slower)
- **Limited storage:** Stick with smaller models (<50 MB)
- **No internet:** Only bundled MobileNet V2 works offline initially

## 🔧 Technical Details

- **Framework:** ONNX Runtime for Android
- **ML Models:** PyTorch models converted to ONNX format
- **Architecture:** Clean Architecture with Repository pattern
- **UI:** Material Design 3
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)

## 📊 Model Performance

All models trained on a comprehensive sugarcane pest dataset with validated accuracy metrics. Performance may vary based on:
- Image quality and lighting conditions
- Pest visibility and positioning
- Device hardware capabilities

## ❓ FAQ

**Q: Which model should I use?**  
A: Start with MobileNet V2 (bundled). It's fast and accurate for most cases.

**Q: Do I need internet?**  
A: Only for downloading additional models. MobileNet V2 works offline.

**Q: Why are results showing low confidence?**  
A: Ensure good lighting, clear image, and visible pest damage. Try different models.

**Q: How do I download additional models?**  
A: Select a model from the dropdown and tap "Analyze Pest". If not downloaded, you'll be prompted.

**Q: Can I delete downloaded models?**  
A: Go to Android Settings > Apps > Pest Detection > Storage > Clear Data (will remove all downloaded models).

## 🐛 Troubleshooting

**App won't install:**
- Enable "Install from Unknown Sources" in Settings
- Ensure you have enough storage space (at least 100 MB)
- Try uninstalling previous versions first

**Classification not working:**
- Check that camera/storage permissions are granted
- Ensure image is clear and pest is visible
- Try a different model from the dropdown

**Download fails:**
- Check internet connection
- Ensure sufficient storage space
- Try again later (server might be busy)

## 📞 Support & Contributing

- **Issues:** Report bugs at [GitHub Issues](https://github.com/SERVER-246/pest-detection-app/issues)
- **Contributions:** Pull requests welcome!
- **Contact:** Open an issue for questions

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- ONNX Runtime by Microsoft
- Material Design by Google
- Sugarcane pest research community

## 📊 Version History

### v1.0.0 (Current)
- Initial release
- 11 AI models available
- Offline capability with MobileNet V2
- Dynamic model downloading
- Material Design UI

---

**Made with ❤️ for sustainable agriculture**

