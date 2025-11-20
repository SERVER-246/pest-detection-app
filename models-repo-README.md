# Pest Detection Models Repository

This repository contains the ONNX models for the Pest Detection Android application.

## 📦 Available Models

| Model | Accuracy | Size | Inference Time | Download |
|-------|----------|------|----------------|----------|
| MobileNet V2 | 98.74% | 14.2 MB | 150ms | Bundled in APK |
| DarkNet53 | 99.38% | 162.5 MB | 450ms | [Download](../../releases/latest/download/darknet53.zip) |
| ResNet50 | 98.74% | 97.8 MB | 300ms | [Download](../../releases/latest/download/resnet50.zip) |
| YOLO11n | 98.80% | 6.5 MB | 120ms | [Download](../../releases/latest/download/yolo11n-cls.zip) |
| Inception V3 | 98.58% | 91.2 MB | 350ms | [Download](../../releases/latest/download/inception_v3.zip) |
| EfficientNet B0 | 98.50% | 20.3 MB | 180ms | [Download](../../releases/latest/download/efficientnet_b0.zip) |
| AlexNet | 98.03% | 233.1 MB | 200ms | [Download](../../releases/latest/download/alexnet.zip) |
| Ensemble (Attention) | 99.88% | 145.0 MB | 800ms | [Download](../../releases/latest/download/ensemble_attention.zip) |
| Ensemble (Cross) | 99.79% | 152.0 MB | 850ms | [Download](../../releases/latest/download/ensemble_cross.zip) |
| Ensemble (Concat) | 99.76% | 148.0 MB | 820ms | [Download](../../releases/latest/download/ensemble_concat.zip) |
| Super Ensemble | 99.96% | 280.0 MB | 1500ms | [Download](../../releases/latest/download/super_ensemble.zip) |

## 📋 Model Structure

Each model zip contains:
- `model.onnx` - The ONNX model file
- `labels.txt` - List of pest class names
- `metadata.json` - Model configuration (image size, etc.)
- `class_mapping.json` - Index to class name mapping

## 🚀 Usage

These models are automatically downloaded by the Pest Detection Android app when selected by the user.

### Direct Download URLs

Replace `USERNAME` and `REPO` with your GitHub details:

```
https://github.com/USERNAME/REPO/releases/download/v1.0/darknet53.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/resnet50.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/yolo11n-cls.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/inception_v3.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/efficientnet_b0.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/alexnet.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/ensemble_attention.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/ensemble_cross.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/ensemble_concat.zip
https://github.com/USERNAME/REPO/releases/download/v1.0/super_ensemble.zip
```

## 📱 Android App Integration

In your Android app, update `ModelInfo.kt`:

```kotlin
private const val MODEL_BASE_URL = "https://github.com/USERNAME/REPO/releases/download/v1.0"
```

## 🔧 Model Format

All models are:
- Format: ONNX (Open Neural Network Exchange)
- Input: RGB image (various sizes, see metadata.json)
- Output: Class probabilities (38 pest classes)
- Framework: Exported from PyTorch/TensorFlow

## 📄 License

[Add your license here]

## 🐛 Issues

If you encounter any issues with model downloads or usage, please open an issue in the main app repository.

