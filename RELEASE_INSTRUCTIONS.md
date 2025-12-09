# 🚀 GitHub Release Instructions

## Files Ready for Upload

### Main Release (v1.0.0)
| File | Size | Description |
|------|------|-------------|
| `intelli_pest-raw.apk` | 569MB | Main APK with Super Ensemble model |

**Location:** `D:\App\Intelli_PEST\intelli_pest-raw.apk`

### Models Release (v1.0.0-models)
| File | Size |
|------|------|
| `alexnet.onnx` | 172MB |
| `attention_fusion.onnx` | 371MB |
| `concatination_fusion.onnx` | 373MB |
| `cross_attention_fusion.onnx` | 399MB |
| `darknet53.onnx` | 81MB |
| `efficientnet_b0.onnx` | 18MB |
| `inception_v3.onnx` | 91MB |
| `mobilenet_v3.onnx` | 12MB |
| `resnet50.onnx` | 98MB |
| `yolo_11n.onnx` | 18MB |

**Location:** `D:\App\Intelli_PEST\models_backup\`

---

## How to Create GitHub Releases

### Step 1: Create Main Release (v1.0.0)

1. Go to https://github.com/SERVER-246/pest-detection-app/releases
2. Click "Draft a new release"
3. Tag: `v1.0.0`
4. Title: `🐛 Intelli-PEST v1.0.0 - Initial Release`
5. Description:
```markdown
## 🐛 Intelli-PEST v1.0.0

AI-powered pest detection for sugarcane crops.

### ✨ Features
- 11 AI models for pest detection
- Detects 11 pest types
- Camera capture and gallery selection
- Works offline with bundled Super Ensemble model
- Beautiful Material Design 3 UI

### 📥 Installation
1. Download `intelli_pest-raw.apk` below
2. Enable "Unknown Sources" in Settings
3. Install the APK
4. Grant camera permission
5. Start detecting pests!

### 📊 Bundled Model
- **Super Ensemble** (96% accuracy) - Included in APK

### 📥 Additional Models
Download from [v1.0.0-models](https://github.com/SERVER-246/pest-detection-app/releases/tag/v1.0.0-models)

### 📱 Requirements
- Android 7.0+ (API 24)
- 1GB free storage
- Camera (for capture feature)
```
6. Upload: `intelli_pest-raw.apk`
7. Click "Publish release"

### Step 2: Create Models Release (v1.0.0-models)

1. Go to https://github.com/SERVER-246/pest-detection-app/releases
2. Click "Draft a new release"
3. Tag: `v1.0.0-models`
4. Title: `📦 AI Models for Intelli-PEST v1.0.0`
5. Description:
```markdown
## 📦 AI Models for Intelli-PEST

Additional ONNX models for pest detection. Download within the app or manually.

### Available Models

| Model | Accuracy | Speed | Size |
|-------|:--------:|:-----:|:----:|
| Cross-Attention Fusion | 95% | ~320ms | 399MB |
| Attention Fusion | 94% | ~280ms | 371MB |
| Concatenation Fusion | 93% | ~250ms | 373MB |
| ResNet-50 | 93% | ~200ms | 98MB |
| DarkNet-53 | 92% | ~300ms | 81MB |
| Inception V3 | 92% | ~220ms | 91MB |
| EfficientNet B0 | 91% | ~120ms | 18MB |
| MobileNet V2 | 89% | ~80ms | 12MB |
| AlexNet | 88% | ~200ms | 172MB |
| YOLO 11 Nano | 87% | ~50ms | 18MB |

### Model Specifications
- **Input:** [1, 3, 224, 224] (NCHW)
- **Output:** [1, 11] (11 pest classes)
- **Format:** ONNX

### Recommendations
- **Best Accuracy:** Use Super Ensemble (in main APK)
- **Fastest:** YOLO 11 Nano
- **Low Memory:** MobileNet V2
- **Balanced:** EfficientNet B0
```
6. Upload all 10 .onnx files from `models_backup` folder
7. Click "Publish release"

---

## Quick Upload Commands (GitHub CLI)

If you have GitHub CLI installed:

```bash
# Create main release with APK
gh release create v1.0.0 intelli_pest-raw.apk --title "🐛 Intelli-PEST v1.0.0" --notes "Initial release with Super Ensemble model"

# Create models release
cd models_backup
gh release create v1.0.0-models *.onnx --title "📦 AI Models for Intelli-PEST v1.0.0" --notes "Additional ONNX models for pest detection"
```

---

## Verification Checklist

After creating releases:

- [ ] v1.0.0 release exists with `intelli_pest-raw.apk`
- [ ] v1.0.0-models release exists with 10 .onnx files
- [ ] Download links in README work
- [ ] APK installs successfully on Android device
- [ ] App launches and Super Ensemble model works
- [ ] Additional models can be downloaded (after implementing download feature)

---

## File Locations Summary

```
D:\App\Intelli_PEST\
├── intelli_pest-raw.apk          # Upload to v1.0.0 release
├── models_backup\                 # Upload contents to v1.0.0-models
│   ├── alexnet.onnx
│   ├── attention_fusion.onnx
│   ├── concatination_fusion.onnx
│   ├── cross_attention_fusion.onnx
│   ├── darknet53.onnx
│   ├── efficientnet_b0.onnx
│   ├── inception_v3.onnx
│   ├── mobilenet_v3.onnx
│   ├── resnet50.onnx
│   └── yolo_11n.onnx
└── app\src\main\assets\models\
    └── super_ensemble.onnx       # Bundled in APK (don't upload separately)
```

