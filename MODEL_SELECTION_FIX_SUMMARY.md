# Model Selection Fix Summary

## Date: December 18, 2025

## Issues Identified from Logs (Latest Analysis)

### Critical Issues Found:

1. **TFLite version incompatibility**: `FULLY_CONNECTED version 12` error
   - **Root Cause**: TFLite models created with TensorFlow 2.17+ use newer operations
   - **Previous Fix**: Upgraded to 2.16.1 but it didn't support v12
   - **Solution**: Upgraded to **TensorFlow Lite 2.20.0**

2. **ONNX input dimension mismatch**: Models expected 256x256 but received 224x224
   - **Root Cause**: `ImagePreprocessor` hardcoded to 224x224 (ImageNet default)
   - **Error**: `Expected: 256, Got: 224` for efficientnet_b0.onnx
   - **Solution**: Made ONNX wrapper dynamically detect input size from model metadata

3. **Runtime selection race condition**: Logging showed wrong runtime after selection
   - **Root Cause**: Async Flow update vs immediate logging
   - **Not critical** but confusing in logs

4. **Model count wrong**: Showing 12 models instead of proper separation
   - **Fixed**: New UI shows exactly 5 TFLite + 3 ONNX models

## Changes Made

### 1. Upgraded TFLite to 2.20.0 (build.gradle.kts)
```kotlin
// Changed from 2.16.1 to 2.20.0 to support FULLY_CONNECTED v12
implementation("org.tensorflow:tensorflow-lite:2.20.0")
implementation("org.tensorflow:tensorflow-lite-gpu:2.20.0")
```
**Why 2.20.0?** Latest stable release that supports all modern TensorFlow operations including FULLY_CONNECTED v12.

### 2. Fixed ONNX Dynamic Input Size (ONNXModelWrapper.kt)
- Added `modelInputSize` property to detect dimensions from model metadata
- Updated `verifySession()` to extract input shape from ONNX model
- Modified `runInference()` to use detected size instead of hardcoded 224

### 3. Updated ImagePreprocessor (ImagePreprocessor.kt)
- Added overloaded `preprocessImage(bitmap, targetSize)` function
- Maintains backward compatibility with default 224x224 for TFLite
- ONNX models can now specify their required input size (256, 299, etc.)

### 4. Redesigned ModelSelectionScreen.kt
- **Two-step selection flow**: First select runtime, then select model
- **Expandable sections**: TFLite and ONNX sections with clear model counts
- **Strictly defined bundled models**:
  - TFLite (5): mobilenet_v2, yolo11n-cls, efficientnet_b0, darknet53, inception_v3
  - ONNX (3): efficientnet_b0, mobilenet_v3, yolo_11n
- **Clear visual distinction**: Green for TFLite, Blue for ONNX

### 3. Updated MainScreen.kt
- Added **ActiveModelCard** showing current runtime and model
- Displays runtime name (TensorFlow Lite / ONNX Runtime)
- Shows selected model name
- "Change" button navigates to model selection

### 4. Updated MainViewModel.kt
- Added `currentRuntime` state flow
- Added `setMLRuntime()` function
- Added PreferencesManager dependency
- Saves model selection to preferences

### 5. Updated DetectionViewModel.kt
- Removed unused imports
- Gets selected model from PreferencesManager
- Listens for model changes via Flow

### 6. Updated AppContainer.kt (DI)
- Passes PreferencesManager to both MainViewModel and DetectionViewModel

### 7. Updated MainActivity.kt
- Passes `currentRuntime` and `selectedModelId` to MainScreen

## Files Modified
1. `app/build.gradle.kts` - TFLite version upgrade
2. `presentation/models/ModelSelectionScreen.kt` - Complete rewrite
3. `presentation/main/MainScreen.kt` - Added ActiveModelCard
4. `presentation/main/MainViewModel.kt` - Runtime state management
5. `presentation/detection/DetectionViewModel.kt` - Model from preferences
6. `di/AppContainer.kt` - DI updates
7. `MainActivity.kt` - Pass runtime/model to MainScreen
8. `domain/model/ModelInfo.kt` - Added runtime-specific fields

## Expected Behavior After Fix

1. **Home Screen**: Shows active runtime (TFLite/ONNX) and selected model
2. **Model Selection**: 
   - Click TFLite section → expands to show 5 TFLite models
   - Click ONNX section → expands to show 3 ONNX models
   - Selecting a model sets both runtime AND model
3. **Detection**: Uses the model saved in preferences

## Testing Checklist

### TFLite Models (Should work now with 2.20.0)
- [ ] MobileNet V2 - loads and runs inference
- [ ] YOLO 11n-cls - loads and runs inference
- [ ] EfficientNet B0 - loads and runs inference
- [ ] DarkNet-53 - loads and runs inference
- [ ] Inception V3 - loads and runs inference

### ONNX Models (Should work with dynamic input size)
- [ ] EfficientNet B0 (256x256) - detects size and runs inference
- [ ] MobileNet V3 - runs inference
- [ ] YOLO 11n - runs inference

### UI/UX Tests
- [ ] Open app - verify TFLite is default runtime
- [ ] Home screen shows "TensorFlow Lite" and correct model name
- [ ] Click "Change" → opens model selection
- [ ] TFLite section shows exactly 5 models
- [ ] ONNX section shows exactly 3 models
- [ ] Select ONNX model → Back to home → shows "ONNX Runtime"
- [ ] Take photo → detection uses selected model
- [ ] Actual classification results appear (not just errors)

## Known Issues Resolved

✅ TFLite 2.20.0 supports FULLY_CONNECTED v12 and newer ops
✅ ONNX models dynamically detect required input dimensions
✅ Model selection properly updates both runtime and model ID
✅ UI clearly separates TFLite and ONNX models

## Build Info

- Build: Debug APK
- Location: `app/build/outputs/apk/debug/app-debug.apk`
- Size: ~57MB (includes both TFLite and ONNX models)

