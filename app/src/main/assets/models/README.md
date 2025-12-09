# Model Files

This folder contains all ONNX model files for pest detection.

## Available Models (11 total)

| Model | File | Description |
|-------|------|-------------|
| Super Ensemble | super_ensemble.onnx | Main ensemble model (best accuracy) |
| AlexNet | alexnet.onnx | Classic CNN architecture |
| Attention Fusion | attention_fusion.onnx | Attention-based fusion |
| Concatenation Fusion | concatination_fusion.onnx | Concatenation-based fusion |
| Cross-Attention Fusion | cross_attention_fusion.onnx | Cross-attention fusion |
| DarkNet-53 | darknet53.onnx | YOLO backbone network |
| EfficientNet B0 | efficentnet_b0.onnx | Efficient architecture |
| Inception V3 | inception_v3.onnx | Google Inception |
| MobileNet V2 | mobilenet_v2.onnx | Mobile optimized |
| ResNet-50 | resnet50.onnx | Deep residual network |
| YOLO 11n | yolo_11n.onnx | Ultra-fast detection |

## Model Format

- **Input:** `[1, 3, 224, 224]` (batch, channels, height, width)
- **Output:** `[1, 11]` (11 pest classes)
- **Format:** ONNX
- **Data Type:** float32

## 11 Pest Classes

1. Armyworm
2. Healthy
3. Internode Borer
4. Mealy Bug
5. Pink Borer
6. Porcupine Damage
7. Rat Damage
8. Root Borer
9. Stalk Borer
10. Termite
11. Top Borer

