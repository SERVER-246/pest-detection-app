# 📱 Pest Detection App - Testing & Deployment Guide

## ✅ Pre-Flight Checklist

Before testing on a mobile device, ensure all these steps are completed:

### 1. Assets Cleanup (CRITICAL)
- [ ] Only `mobilenet_v2` folder exists in `app/src/main/assets/models/`
- [ ] All other model folders have been deleted
- [ ] Expected APK size: 40-60 MB (was ~2GB before cleanup)

**Verification Command:**
```powershell
Get-ChildItem "D:\App\Pest1\app\src\main\assets\models" -Directory
# Should only show: mobilenet_v2
```

### 2. Code Configuration
- [ ] `ModelInfo.kt` MODEL_BASE_URL is configured (or left as placeholder for testing bundled model only)
- [ ] No compilation errors
- [ ] Gradle build succeeds

### 3. Build Preparation
- [ ] Gradle clean completed
- [ ] Build artifacts cleared
- [ ] APK successfully generated

---

## 🔨 Building the App

### Quick Build (Debug APK)
```powershell
cd D:\App\Pest1
.\gradlew.bat clean assembleDebug
```

**Output Location:** `app\build\outputs\apk\debug\app-debug.apk`

### Production Build (Release APK)
```powershell
cd D:\App\Pest1
.\gradlew.bat clean assembleRelease
```

**Output Location:** `app\build\outputs\apk\release\app-release-unsigned.apk`

> **Note:** Release builds require signing configuration. For testing, use debug APK.

---

## 📦 APK Size Verification

After building, verify the APK size:

```powershell
$apk = "D:\App\Pest1\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apk) {
    $size = (Get-Item $apk).Length / 1MB
    Write-Host "APK Size: $([math]::Round($size, 2)) MB"
}
```

**Expected Results:**
- ✅ **Good:** 40-60 MB (only mobilenet_v2 bundled)
- ⚠️ **Warning:** 60-100 MB (check for extra resources)
- ❌ **Problem:** >100 MB (models not properly removed)

---

## 📱 Installing on Android Device

### Prerequisites
1. Android device with API 24+ (Android 7.0 Nougat or higher)
2. USB debugging enabled on device
3. ADB (Android Debug Bridge) installed on computer

### Enable USB Debugging on Device
1. Go to **Settings** → **About Phone**
2. Tap **Build Number** 7 times to enable Developer Options
3. Go to **Settings** → **Developer Options**
4. Enable **USB Debugging**
5. Connect device to computer via USB
6. Accept "Allow USB Debugging" prompt on device

### Install via ADB
```powershell
# Check if device is connected
adb devices

# Install APK
adb install "D:\App\Pest1\app\build\outputs\apk\debug\app-debug.apk"

# If app already exists, reinstall:
adb install -r "D:\App\Pest1\app\build\outputs\apk\debug\app-debug.apk"
```

### Install via File Transfer
1. Copy `app-debug.apk` to device storage
2. On device, open **Files** app
3. Navigate to the APK file
4. Tap to install
5. Allow installation from unknown sources if prompted

---

## 🧪 Testing Checklist

### Test 1: App Launch ✅
- [ ] App installs successfully (no "App too large" error)
- [ ] App icon appears on home screen
- [ ] App opens without crashing
- [ ] Main screen displays correctly with UI elements

**Expected:** Clean interface with:
- Title: "Pest Detection"
- Image placeholder area
- "Choose Image" and "Take Photo" buttons
- Model selection spinner (showing all 11 models)
- "Analyze Pest" button (disabled until image selected)

### Test 2: Image Selection ✅
- [ ] "Choose Image" button opens gallery
- [ ] Can select an image from gallery
- [ ] Image displays in preview area
- [ ] "Analyze Pest" button becomes enabled

**Expected:** Smooth image loading, no crashes

### Test 3: Camera Capture ✅
- [ ] "Take Photo" button opens camera
- [ ] Can capture a photo
- [ ] Photo displays in preview area
- [ ] "Analyze Pest" button becomes enabled

**Expected:** Camera permission granted, photo captured successfully

### Test 4: Bundled Model Classification (MobileNet V2) ✅
**This is the CRITICAL test for core functionality**

Steps:
1. Select or capture an image of a pest
2. Ensure "MobileNet V2" is selected in model spinner
3. Tap "Analyze Pest" button
4. Observe loading indicator
5. Wait for results

**Expected Results:**
- [ ] Loading indicator appears
- [ ] Classification completes in 100-300ms
- [ ] Results display shows:
  - Model name: "MobileNet V2 (98.74%)"
  - Inference time: ~150ms
  - Top prediction with confidence %
  - List of top 5 predictions with confidences
- [ ] Predictions are reasonable for the image
- [ ] Confidence scores add up to ~100%
- [ ] No crash or error messages

**Common Issues:**
- ❌ "Failed to load model" → Check if mobilenet_v2 folder exists in assets
- ❌ "Classification failed" → Check OnnxModelManager logs
- ❌ All predictions show 0% → Preprocessing issue
- ❌ Wrong predictions → Model may need different normalization

### Test 5: Model Download Feature ✅
**Only test this if MODEL_BASE_URL is configured**

Steps:
1. Select "ResNet50" or another non-bundled model from spinner
2. Select an image
3. Tap "Analyze Pest"
4. Observe download prompt

**Expected Results:**
- [ ] Dialog appears: "Model 'ResNet50' is not available. Would you like to download it?"
- [ ] Shows model size (e.g., "Size: 97.8 MB")
- [ ] Tap "Download" starts download
- [ ] Progress indicator shows download progress
- [ ] Download completes successfully
- [ ] Model is cached for future use
- [ ] Classification works with downloaded model

**Note:** If MODEL_BASE_URL is placeholder, you'll see:
- "Download failed: Unable to resolve host" (expected)

### Test 6: Permissions ✅
- [ ] Camera permission requested when needed
- [ ] Gallery permission requested when needed (Android 13+)
- [ ] Internet permission works (for model downloads)
- [ ] App handles permission denial gracefully

### Test 7: Edge Cases ✅
- [ ] Tapping "Analyze" without selecting image shows error message
- [ ] Rotating device doesn't crash app
- [ ] Switching between models works correctly
- [ ] Low confidence results (<80%) show warning
- [ ] Network errors display user-friendly messages

### Test 8: Performance ✅
- [ ] App runs smoothly, no lag
- [ ] Memory usage is reasonable (check in Settings → Apps)
- [ ] Battery drain is acceptable
- [ ] No excessive heating during use

---

## 🐛 Troubleshooting

### Problem: APK is still 1-2 GB
**Cause:** Models not removed from assets

**Solution:**
```powershell
cd D:\App\Pest1\app\src\main\assets\models
# Manually delete all folders except mobilenet_v2
Remove-Item -Recurse darknet53, resnet50, yolo11n-cls, inception_v3, efficientnet_b0, alexnet, ensemble_attention, ensemble_cross, ensemble_concat, super_ensemble
# Rebuild
cd D:\App\Pest1
.\gradlew.bat clean assembleDebug
```

### Problem: "Failed to load model"
**Possible Causes:**
1. mobilenet_v2 folder missing from assets
2. model.onnx file corrupted
3. ONNX Runtime library not included

**Solution:**
1. Verify assets folder structure:
   ```
   app/src/main/assets/models/mobilenet_v2/
   ├── model.onnx
   ├── labels.txt
   ├── metadata.json
   └── class_mapping.json
   ```
2. Check build.gradle dependencies include ONNX Runtime
3. Check logcat for detailed error messages

### Problem: Classification returns wrong results
**Possible Causes:**
1. Image preprocessing not matching model training
2. Softmax calculation issues
3. Wrong class labels loaded

**Solution:**
1. Check OnnxModelManager preprocessing (should use ImageNet normalization)
2. Verify labels.txt matches model output
3. Check logcat for "Raw confidences" to see if model outputs are reasonable

### Problem: App crashes on launch
**Possible Causes:**
1. Incompatible Android version (need API 24+)
2. Missing dependencies
3. Corrupted APK

**Solution:**
1. Check device Android version: Settings → About Phone
2. Rebuild APK with clean build
3. Check logcat for crash stack trace:
   ```powershell
   adb logcat | Select-String "pest_1"
   ```

### Problem: Cannot download models
**Possible Causes:**
1. MODEL_BASE_URL not configured
2. No internet connection
3. GitHub rate limiting
4. Invalid download URLs

**Solution:**
1. Update ModelInfo.kt with correct GitHub URL
2. Check device internet connection
3. Test URL in browser first
4. Use WiFi instead of mobile data for large downloads

---

## 📊 Monitoring and Debugging

### View Real-Time Logs
```powershell
# All app logs
adb logcat | Select-String "pest_1"

# OnnxModelManager logs only
adb logcat | Select-String "OnnxModelManager"

# Error logs only
adb logcat *:E | Select-String "pest_1"
```

### Check App Storage Usage
On device:
1. Settings → Apps → Pest Detection
2. Storage & cache
3. Should see:
   - App size: ~40-60 MB
   - Data: grows as models downloaded
   - Cache: temporary files

### Performance Profiling
```powershell
# CPU usage
adb shell top | Select-String "pest_1"

# Memory usage
adb shell dumpsys meminfo com.example.pest_1
```

---

## ✅ Sign-Off Checklist

Before declaring the app "production ready":

- [ ] APK size is under 60 MB
- [ ] Installs successfully on multiple devices (Android 7.0+)
- [ ] Bundled model (MobileNet V2) classification works 100% of the time
- [ ] Results are accurate and consistent
- [ ] UI is responsive and user-friendly
- [ ] No crashes during normal operation
- [ ] Permissions handled correctly
- [ ] Model download feature works (if configured)
- [ ] Low confidence warnings display properly
- [ ] Error messages are user-friendly
- [ ] Performance is acceptable (inference < 500ms)
- [ ] Memory usage is reasonable (< 200MB)

---

## 🚀 Production Deployment

Once testing is complete:

### 1. Configure Release Signing
Edit `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/keystore.jks")
            storePassword = "your-store-password"
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other config
        }
    }
}
```

### 2. Build Release APK
```powershell
.\gradlew.bat assembleRelease
```

### 3. Verify Release Build
- Check APK size
- Test on multiple devices
- Verify all features work
- Check ProGuard didn't break anything

### 4. Prepare for Distribution
- Create App Store listing
- Prepare screenshots
- Write description
- Set up model hosting (GitHub Releases or cloud storage)
- Create privacy policy
- Prepare support documentation

---

## 📞 Support & Resources

**Documentation:**
- `FIXES_IMPLEMENTED.md` - What was fixed
- `ACTION_CHECKLIST.md` - Quick reference
- `GITHUB_SETUP_GUIDE.md` - Model hosting setup
- `models-repo-README.md` - Model repository info

**Key Files:**
- `COMPREHENSIVE_SETUP.ps1` - Automated setup script
- `cleanup_assets.ps1` - Asset cleanup only
- `create-github-models.ps1` - Model packaging script

**Need Help?**
- Check logcat output for detailed errors
- Review ONNX Runtime documentation
- Test on Android emulator first if no physical device available

---

**Last Updated:** 2025-11-20
**Version:** 1.0
**Status:** Ready for Testing ✅

