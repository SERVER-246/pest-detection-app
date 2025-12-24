# Model Loading & Inference Testing Guide

## Recent Fixes Applied:

### 1. ✅ Fixed Default Model Issue
**Problem**: Default model was set to "resnet50" (not bundled)
**Solution**: Changed to "mobilenet_v2" (bundled, 3.18MB, 89% accuracy)

**Files Modified**:
- `DetectionViewModel.kt` - Changed DEFAULT_MODEL constant to "mobilenet_v2"
- `DetectionUiState.kt` - Changed default selectedModelId to "mobilenet_v2"

### 2. ✅ Added Comprehensive Logging
Added detailed logging to track the entire detection pipeline:

#### Model Resolution (`PestDetectionRepositoryImpl.kt`):
```
======= RESOLVE MODEL PATH START =======
Model ID: mobilenet_v2
Model bundled check: true
Model downloaded check: false
Using bundled path: models/mobilenet_v2.tflite
Final resolved path: models/mobilenet_v2.tflite
======= RESOLVE MODEL PATH END =======
```

#### Model Initialization (`TFLiteModelWrapper.kt`):
```
======= MODEL INITIALIZATION START =======
initializeModel() | path=models/mobilenet_v2.tflite
Loading from: ASSETS
Attempting to load from assets: models/mobilenet_v2.tflite
Available models in assets/models: [darknet53.tflite, efficientnet_b0.tflite, inception_v3.tflite, mobilenet_v2.tflite, README.md, yolo11n-cls.tflite]
Asset opened | offset=X, length=Y
Model buffer loaded successfully | size=X bytes
Configuring interpreter options | threads=4
Creating TFLite interpreter...
✅ Model initialized successfully | path=models/mobilenet_v2.tflite
======= MODEL INITIALIZATION END =======
```

#### Inference Process (`TFLiteModelWrapper.kt`):
```
======= INFERENCE START =======
Interpreter ready | model=models/mobilenet_v2.tflite
Input bitmap | size=WxH, config=ARGB_8888
Preprocessing image...
✅ Preprocessing complete | time=Xms, array_size=150528
Creating input buffer...
✅ Input buffer created | capacity=602112 bytes
Preparing output buffer...
Output shape: 1x11, classes=11
Running inference...
✅ Inference complete | time=Xms
Raw output: [...]
Softmax probabilities (top 5): [ARMYWORM=0.xxxx, HEALTHY=0.xxxx, ...]
✅ Inference complete | total_predictions=11
Top 3 predictions:
  1. Healthy: 85.3%
  2. Armyworm: 8.7%
  3. Mealy Bug: 3.2%
======= INFERENCE END =======
```

#### Detection ViewModel (`DetectionViewModel.kt`):
```
======= DETECTION START =======
Model ID: mobilenet_v2
Bitmap: WxH, config=ARGB_8888
✅ Detection SUCCESS
Result: Healthy (85.3%)
======= DETECTION END =======
```

---

## How to Test in Emulator:

### Step 1: Launch Emulator
1. Start Android Emulator in Android Studio
2. Wait for it to fully boot

### Step 2: Install APK
```powershell
cd D:\App\Intelli_PEST
.\gradlew installDebug
```

### Step 3: Open Logcat
In Android Studio:
1. Go to View → Tool Windows → Logcat
2. Filter by package: `com.example.intelli_pest`
3. Or filter by tags: `TFLiteModelWrapper`, `PestDetectionRepo`, `DetectionViewModel`

### Step 4: Test Camera Capture
1. Open app
2. Click "Capture Photo"
3. Click camera shutter button
4. **Watch Logcat** for:
   - Model path resolution
   - Model loading
   - Inference execution
   - Prediction results

### Step 5: Test Gallery Import
1. First, push test image to emulator:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" push "D:\Test-images\Internode borer\Real time Cane\IMG_0742.JPG" /sdcard/Download/test_internode_borer.jpg
```

2. In app, click "Select from Gallery"
3. Navigate to Downloads folder
4. Select test_internode_borer.jpg
5. **Watch Logcat** for entire detection pipeline

### Step 6: Test Model Selection
1. Click "AI Models" button on main screen
2. Verify 5 bundled models appear with "BUNDLED" badge
3. Select a different model (e.g., "YOLO 11 Nano")
4. Go back and try detection again
5. **Watch Logcat** to verify new model is loaded

---

## Expected Log Flow (Successful Detection):

```
[DetectionViewModel] ======= DETECTION START =======
[DetectionViewModel] Model ID: mobilenet_v2
[DetectionViewModel] Bitmap: 1024x768, config=ARGB_8888

[PestDetectionRepo] detectPest() start | model=mobilenet_v2 | bitmap=1024x768 config=ARGB_8888

[PestDetectionRepo] ======= RESOLVE MODEL PATH START =======
[PestDetectionRepo] Model ID: mobilenet_v2
[ModelFileManager] MobileNet V2 bundled check: true
[PestDetectionRepo] Model bundled check: true
[PestDetectionRepo] Model downloaded check: false
[PestDetectionRepo] Using bundled path: models/mobilenet_v2.tflite
[PestDetectionRepo] Final resolved path: models/mobilenet_v2.tflite
[PestDetectionRepo] ======= RESOLVE MODEL PATH END =======

[PestDetectionRepo] Model resolved | path=models/mobilenet_v2.tflite
[PestDetectionRepo] Loading model into memory | path=models/mobilenet_v2.tflite

[TFLiteModelWrapper] ======= MODEL INITIALIZATION START =======
[TFLiteModelWrapper] initializeModel() | path=models/mobilenet_v2.tflite
[TFLiteModelWrapper] Loading from: ASSETS
[TFLiteModelWrapper] Attempting to load from assets: models/mobilenet_v2.tflite
[TFLiteModelWrapper] Available models in assets/models: [darknet53.tflite, efficientnet_b0.tflite, inception_v3.tflite, mobilenet_v2.tflite, README.md, yolo11n-cls.tflite]
[TFLiteModelWrapper] Asset opened | offset=0, length=3333056
[TFLiteModelWrapper] ✅ Model loaded from assets | capacity=3333056 bytes
[TFLiteModelWrapper] Model buffer loaded successfully | size=3333056 bytes
[TFLiteModelWrapper] Configuring interpreter options | threads=4
[TFLiteModelWrapper] Creating TFLite interpreter...
[TFLiteModelWrapper] ✅ Model initialized successfully | path=models/mobilenet_v2.tflite
[TFLiteModelWrapper] ======= MODEL INITIALIZATION END =======

[ImageValidator] ======= IMAGE VALIDATION START =======
[ImageValidator] Bitmap size: 1024x768, config=ARGB_8888
[ImageValidator] ✅ Pixel access successful
[ImageValidator] Green content: 25.40%
[ImageValidator] Validation checks:
[ImageValidator]   - Green content: true
[ImageValidator]   - Color distribution: true
[ImageValidator]   - Texture variation: true
[ImageValidator]   - Quality: true
[ImageValidator] Checks passed: 4/4
[ImageValidator] ✅ Image validation PASSED
[ImageValidator] ======= IMAGE VALIDATION END =======

[PestDetectionRepo] validateImage() | valid=true quality=true
[PestDetectionRepo] Confidence threshold=0.7

[TFLiteModelWrapper] ======= INFERENCE START =======
[TFLiteModelWrapper] Interpreter ready | model=models/mobilenet_v2.tflite
[TFLiteModelWrapper] Input bitmap | size=1024x768, config=ARGB_8888
[TFLiteModelWrapper] Preprocessing image...
[TFLiteModelWrapper] ✅ Preprocessing complete | time=45ms, array_size=150528
[TFLiteModelWrapper] Creating input buffer...
[TFLiteModelWrapper] ✅ Input buffer created | capacity=602112 bytes
[TFLiteModelWrapper] Preparing output buffer...
[TFLiteModelWrapper] Output shape: 1x11, classes=11
[TFLiteModelWrapper] Running inference...
[TFLiteModelWrapper] ✅ Inference complete | time=125ms
[TFLiteModelWrapper] Raw output: [-2.3456, 5.7890, -1.2345, ...]
[TFLiteModelWrapper] Softmax probabilities (top 5): [ARMYWORM=0.0234, HEALTHY=0.8532, INTERNODE_BORER=0.0987, ...]
[TFLiteModelWrapper] ✅ Inference complete | total_predictions=11
[TFLiteModelWrapper] Top 3 predictions:
[TFLiteModelWrapper]   1. Healthy: 85.3%
[TFLiteModelWrapper]   2. Internode Borer: 9.9%
[TFLiteModelWrapper]   3. Armyworm: 2.3%
[TFLiteModelWrapper] ======= INFERENCE END =======

[PestDetectionRepo] Detection success | pest=HEALTHY | confidence=0.853 | uri=/data/user/0/com.server246.intelli_pest/files/detections/detection_1234567890.jpg

[DetectionViewModel] ✅ Detection SUCCESS
[DetectionViewModel] Result: Healthy (85.3%)
[DetectionViewModel] ======= DETECTION END =======
```

---

## Troubleshooting Guide:

### Issue: "Failed to load model from models/mobilenet_v2.tflite"

**Check Logcat for**:
1. `Available models in assets/models:` - Does it list mobilenet_v2.tflite?
2. `Model bundled check:` - Should be `true`
3. Asset loading errors

**Possible Causes**:
- Model file missing from assets (check `app/src/main/assets/models/`)
- Asset path incorrect
- File corrupted

**Solution**:
```powershell
# Verify model exists
dir D:\App\Intelli_PEST\app\src\main\assets\models\mobilenet_v2.tflite

# If missing, copy from backup
copy D:\path\to\backup\mobilenet_v2.tflite D:\App\Intelli_PEST\app\src\main\assets\models\
```

### Issue: "Detection returned empty result"

**Check Logcat for**:
1. Inference completion: `✅ Inference complete`
2. Top predictions: Should show 3 predictions
3. Confidence values: Should have at least one > 0.0

**Possible Causes**:
- Model output mismatch (wrong number of classes)
- Preprocessing error
- Model not compatible with input format

**Solution**: Check model expects 11 output classes (pest types)

### Issue: App crashes on detection

**Check Logcat for**:
1. Exception stack traces
2. Last successful log before crash
3. Memory issues (OOM)

**Possible Causes**:
- Bitmap too large
- Out of memory
- Model incompatible with TFLite version

**Solution**: Check bitmap dimensions and config in logs

---

## Model Performance Expectations:

### MobileNet V2 (Default):
- **Load Time**: 200-500ms (first time), <50ms (cached)
- **Inference Time**: 80-150ms on emulator, 30-80ms on device
- **Accuracy**: ~89% on test set
- **Best for**: Fast, lightweight detection

### YOLO 11n-cls:
- **Load Time**: 250-600ms (first time)
- **Inference Time**: 50-100ms
- **Accuracy**: ~87%
- **Best for**: Fastest inference

### EfficientNet B0:
- **Load Time**: 250-600ms
- **Inference Time**: 120-200ms
- **Accuracy**: ~91%
- **Best for**: Balanced accuracy and speed

### DarkNet-53:
- **Load Time**: 800-1500ms (larger model)
- **Inference Time**: 300-500ms
- **Accuracy**: ~92%
- **Best for**: Higher accuracy (slower)

### Inception V3:
- **Load Time**: 900-1600ms
- **Inference Time**: 220-400ms
- **Accuracy**: ~92%
- **Best for**: High accuracy, complex features

---

## Test Checklist:

- [ ] App launches without crashes
- [ ] Splash screen displays ICAR-ISRI branding
- [ ] Main screen loads successfully
- [ ] "AI Models" button opens model selection screen
- [ ] 5 bundled models shown with "BUNDLED" badge
- [ ] Can select different models
- [ ] Camera permission requested
- [ ] Camera preview works
- [ ] Can capture image from camera
- [ ] Image processing shows loading animation
- [ ] Detection completes successfully
- [ ] Results screen shows pest type and confidence
- [ ] Top 3 predictions displayed
- [ ] Can select image from gallery
- [ ] Gallery selection works
- [ ] Unrelated images filtered (if validation enabled)
- [ ] Low confidence results handled properly
- [ ] Can return to main screen
- [ ] Logcat shows detailed inference logs
- [ ] No errors in Logcat
- [ ] Model switching works correctly
- [ ] All models load successfully

---

## Building Release APK:

Once all tests pass:

```powershell
cd D:\App\Intelli_PEST
.\gradlew assembleRelease
```

Output will be at:
`D:\App\Intelli_PEST\app\build\outputs\apk\release\app-release.apk`

Expected size: ~65-70 MB (with 5 bundled TFLite models)

---

*Last Updated: December 15, 2025*

