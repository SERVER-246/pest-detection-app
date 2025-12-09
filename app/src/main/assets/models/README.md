# 🤖 Model Files

This folder contains ONNX model files for pest detection.

## Bundled Model

| Model | File | Size | Status |
|-------|------|:----:|:------:|
| Super Ensemble | `super_ensemble.onnx` | 544MB | ✅ Included in APK |

## Downloadable Models

Available from [GitHub Releases](https://github.com/SERVER-246/pest-detection-app/releases/tag/v1.0.0-models):

| Model | File | Size | Accuracy |
|-------|------|:----:|:--------:|
| AlexNet | `alexnet.onnx` | 172MB | 88% |
| Attention Fusion | `attention_fusion.onnx` | 371MB | 94% |
| Concatenation Fusion | `concatination_fusion.onnx` | 373MB | 93% |
| Cross-Attention Fusion | `cross_attention_fusion.onnx` | 399MB | 95% |
| DarkNet-53 | `darknet53.onnx` | 81MB | 92% |
| EfficientNet B0 | `efficientnet_b0.onnx` | 18MB | 91% |
| Inception V3 | `inception_v3.onnx` | 91MB | 92% |
| MobileNet V2 | `mobilenet_v2.onnx` | 12MB | 89% |
| ResNet-50 | `resnet50.onnx` | 98MB | 93% |
| YOLO 11 Nano | `yolo_11n.onnx` | 18MB | 87% |

## Model Specifications

- **Input Shape:** `[1, 3, 224, 224]` (batch, channels, height, width)
- **Output Shape:** `[1, 11]` (11 pest classes)
- **Data Type:** float32
- **Format:** ONNX (Open Neural Network Exchange)

## 11 Pest Classes (Output)

| Index | Class |
|:-----:|-------|
| 0 | Armyworm |
| 1 | Healthy |
| 2 | Internode Borer |
| 3 | Mealy Bug |
| 4 | Pink Borer |
| 5 | Porcupine Damage |
| 6 | Rat Damage |
| 7 | Root Borer |
| 8 | Stalk Borer |
| 9 | Termite |
| 10 | Top Borer |

## Model Selection Guide

| Use Case | Recommended Model |
|----------|-------------------|
| Best Accuracy | Super Ensemble (bundled) |
| Fastest Detection | YOLO 11 Nano |
| Low Memory Device | MobileNet V2 |
| Balanced Performance | EfficientNet B0 |
| Offline Use | Super Ensemble (no download) |

