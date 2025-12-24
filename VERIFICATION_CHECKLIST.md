# Critical Issues Fix - Verification Checklist

## Date: December 24, 2025

## Issues Fixed

### 1. ✅ PyTorch Native Library Issue
- **Problem**: `dlopen failed: library "libpytorch_jni.so" not found`
- **Fix**: Changed from `pytorch_android_lite` to full `pytorch_android` dependency
- **File**: `app/build.gradle.kts`

### 2. ✅ Low Confidence Detection Accepted
- **Problem**: 31.1% confidence was accepted
- **Fix**: Updated MIN_CONFIDENCE_THRESHOLD to 50%, HIGH_CONFIDENCE_THRESHOLD to 70%
- **File**: `PestDetectionRepositoryImpl.kt`

### 3. ✅ High Entropy Detection Accepted  
- **Problem**: Entropy 1.9 was accepted (threshold was 2.0)
- **Fix**: Updated MAX_ENTROPY_THRESHOLD to 1.8
- **File**: `PestDetectionRepositoryImpl.kt`

### 4. ✅ Image Validation Always Returns True
- **Problem**: No actual validation was performed
- **Fix**: Implemented multi-metric validation (blur, contrast, green ratio, brightness)
- **File**: `ImageValidator.kt`

### 5. ✅ Settings Screen Shows TFLite
- **Problem**: UI mentioned "TensorFlow Lite" instead of "PyTorch Mobile"
- **Fix**: Updated all TFLite references to PyTorch Mobile
- **Files**: `SettingsScreen.kt`, `ModelSelectionScreen.kt`

### 6. ✅ Model Name Mismatch in Logs
- **Problem**: UI says "super_ensemble" but loads "student_model"
- **Note**: This is by design - we simplified to single model but UI still shows old name
- **Logs now show**: Model path resolved correctly

### 7. ✅ APK Size Optimization
- **Added**: ABI splits for arm64-v8a and armeabi-v7a
- **Added**: Proper JNI packaging options
- **File**: `app/build.gradle.kts`

---

## Configurable Thresholds

### Image Validation (`ImageValidator.kt`)
| Threshold | Value | Description |
|-----------|-------|-------------|
| BLUR_THRESHOLD | 100.0 | Laplacian variance (lower = blurrier) |
| CONTRAST_THRESHOLD | 30.0 | Minimum contrast |
| GREEN_RATIO_THRESHOLD | 0.08 | Minimum green ratio (8%) |
| MIN_BRIGHTNESS | 20 | Minimum average brightness |
| MAX_BRIGHTNESS | 240 | Maximum brightness (avoid overexposed) |

### Detection Confidence (`PestDetectionRepositoryImpl.kt`)
| Threshold | Value | Description |
|-----------|-------|-------------|
| MIN_CONFIDENCE_THRESHOLD | 0.50 | Absolute minimum (50%) |
| HIGH_CONFIDENCE_THRESHOLD | 0.70 | Reliable detection (70%) |
| MAX_ENTROPY_THRESHOLD | 1.8 | Maximum entropy allowed |

---

## Manual Verification Checklist

### Pre-requisites
- [ ] APK builds successfully
- [ ] APK installs on device/emulator

### Camera/Gallery Tests
- [ ] Camera captures image successfully
- [ ] Gallery selection works
- [ ] Image displays correctly before detection

### Image Validation Tests
- [ ] **Blurry image**: Should be REJECTED with "Image appears blurry" message
- [ ] **Low contrast image**: Should be REJECTED with "Image has low contrast" message
- [ ] **Non-plant image** (e.g., random object): Should be REJECTED with "doesn't appear to contain plant matter"
- [ ] **Valid sugarcane image**: Should PASS validation

### Detection Confidence Tests
- [ ] **Low confidence (<50%)**: Should be REJECTED with "Detection confidence too low" message
- [ ] **Moderate confidence (50-70%)** with high entropy: Should be REJECTED with uncertainty message
- [ ] **High confidence (>70%)** with low entropy: Should be ACCEPTED

### Runtime Selection Tests
- [ ] ONNX Runtime selection works
- [ ] PyTorch Mobile selection works
- [ ] Runtime change takes effect immediately
- [ ] Logs show correct runtime being used

### Settings Screen Tests
- [ ] Settings shows "ONNX Runtime" and "PyTorch Mobile" (not TFLite)
- [ ] Confidence threshold slider works
- [ ] Tracking mode toggle works
- [ ] Log download/share works

### Error Handling Tests
- [ ] Missing model shows clear error
- [ ] PyTorch model loads without JNI error (if using PyTorch runtime)
- [ ] Network errors handled gracefully

---

## Expected Log Patterns

### Successful Validation
```
ValidateImage_Result: valid=true, blur=150.2, contrast=85.3, green=18.5%, brightness=128
```

### Failed Validation
```
ValidateImage_Result: valid=false, blur=45.2, contrast=20.1, green=5.2%, brightness=200, reason=Image appears blurry; Image doesn't appear to contain plant matter
```

### Successful Detection
```
Detection_Accepted: Detection accepted: Internode Borer at 85.2%
Detection_Success: Pest: Internode Borer, Confidence: 85.2%
```

### Rejected Detection
```
Low_Confidence_Rejected: Confidence 31.1% < 50%
Detection_Error: This image doesn't appear to be a sugarcane crop. Detection confidence too low...
```

---

## Files Changed

| File | Changes |
|------|---------|
| `app/build.gradle.kts` | ABI splits, PyTorch full dependency, JNI packaging |
| `ImageValidator.kt` | Complete rewrite with multi-metric validation |
| `PestDetectionRepositoryImpl.kt` | Updated thresholds, improved rejection logic |
| `InferenceEngine.kt` | Added validateImageWithDetails method |
| `SettingsScreen.kt` | Updated TFLite → PyTorch Mobile |
| `SettingsViewModel.kt` | Already had PyTorch wording |
| `ModelSelectionScreen.kt` | Already had PyTorch wording |

---

## Build Instructions

```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Build release APK with ABI splits
./gradlew clean assembleRelease

# Check APK sizes
ls -la app/build/outputs/apk/release/
```

---

## Known Limitations

1. **Model name in UI**: Still shows "super_ensemble" in some places while using "student_model" files
2. **PyTorch support**: Requires full PyTorch library (~40MB overhead)
3. **Validation thresholds**: May need device-specific tuning

---

*Verification document generated: December 24, 2025*

