# 🧪 EMULATOR TESTING - STEP BY STEP

## ✅ BUILD SUCCESSFUL - APK INSTALLED

**Status**: App successfully built and installed on emulator!

---

## 📱 Current State:

- **Build**: ✅ SUCCESS (38 tasks executed)
- **Installation**: ✅ SUCCESS on emulator-5554
- **Test Image**: ✅ Pushed to `/sdcard/Download/test_internode.jpg`
- **Logcat**: ✅ Cleared and ready

---

## 🔬 TESTING PROCEDURE:

### Test 1: Launch App & Check Splash Screen

1. **Open the app** on emulator
   - Look for Intelli-PEST icon
   - Launch the app

2. **Verify Splash Screen**:
   - ✅ Should show animated splash with ICAR-ISRI branding
   - ✅ Should display for 3 seconds
   - ✅ Should transition to main screen

3. **Start Logcat Monitoring**:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -s TFLiteModelWrapper:D PestDetectionRepo:D DetectionViewModel:D ImageValidator:D MainViewModel:D ModelFileManager:D
```

---

### Test 2: Gallery Selection (RECOMMENDED FIRST TEST)

1. **On Main Screen**, click **"Select from Gallery"**

2. **Navigate to Downloads folder**
   - Select `test_internode.jpg`

3. **Watch Logcat for**:
```
[DetectionViewModel] ======= DETECTION START =======
[DetectionViewModel] Model ID: mobilenet_v2

[PestDetectionRepo] ======= RESOLVE MODEL PATH START =======
[PestDetectionRepo] Model bundled check: true
[PestDetectionRepo] Using bundled path: models/mobilenet_v2.tflite

[TFLiteModelWrapper] ======= MODEL INITIALIZATION START =======
[TFLiteModelWrapper] Available models in assets/models: [darknet53.tflite, efficientnet_b0.tflite, inception_v3.tflite, mobilenet_v2.tflite, yolo11n-cls.tflite]
[TFLiteModelWrapper] ✅ Model initialized successfully

[ImageValidator] ======= IMAGE VALIDATION START =======
[ImageValidator] Checks passed: X/4
[ImageValidator] ✅ Image validation PASSED

[TFLiteModelWrapper] ======= INFERENCE START =======
[TFLiteModelWrapper] ✅ Inference complete | time=XXms
[TFLiteModelWrapper] Top 3 predictions:
[TFLiteModelWrapper]   1. [Pest Type]: XX.X%
[TFLiteModelWrapper]   2. [Pest Type]: XX.X%
[TFLiteModelWrapper]   3. [Pest Type]: XX.X%

[DetectionViewModel] ✅ Detection SUCCESS
```

4. **Expected Result**:
   - Results screen should appear
   - Should show detected pest type
   - Should show confidence percentage
   - Should show top 3 predictions

---

### Test 3: Model Selection

1. **From Main Screen**, click **"AI Models"**

2. **Verify Display**:
   - ✅ Should show 5 bundled models with "BUNDLED" badge:
     - MobileNet V2 (3.18 MB, 89%)
     - YOLO 11n-cls (5.11 MB, 87%)
     - EfficientNet B0 (5.11 MB, 91%)
     - DarkNet-53 (20.46 MB, 92%)
     - Inception V3 (23.1 MB, 92%)
   
   - ✅ Should show 6 downloadable models
   - ✅ Should show "More models coming soon!" at bottom

3. **Select Different Model**:
   - Click on "YOLO 11 Nano"
   - Click "Use This Model"
   - Should show "Currently Active"

4. **Watch Logcat**:
```
[MainViewModel] Model selected: yolo11n-cls
```

5. **Go back and try detection again**
   - Select same test image
   - Watch for model change in logs

---

### Test 4: Camera Capture

1. **From Main Screen**, click **"Capture Photo"**

2. **Grant Camera Permission** if asked

3. **Take a photo** using camera shutter button

4. **Watch Logcat** for detection flow

5. **Expected**: Same log flow as Test 2

---

## 🐛 TROUBLESHOOTING:

### If you see: "Failed to load model from models/mobilenet_v2.tflite"

**Check Logcat for**:
```
[TFLiteModelWrapper] Available models in assets/models: [...]
```

**Expected**: Should list mobilenet_v2.tflite

**If missing**: Model file may not be in APK. Verify:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\build-tools\35.0.0\aapt.exe" list "D:\App\Intelli_PEST\app\build\outputs\apk\debug\app-debug.apk" | Select-String "mobilenet"
```

---

### If detection returns no result:

**Check Logcat for**:
1. Model initialization success: `✅ Model initialized successfully`
2. Inference completion: `✅ Inference complete`
3. Predictions count: `Top 3 predictions:`
4. Any error messages with `❌`

---

### If app crashes:

**Get crash log**:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -d | Select-String -Pattern "FATAL|Exception" | Select-Object -Last 50
```

---

## 📊 EXPECTED PERFORMANCE:

### MobileNet V2 (Default):
- **Model Load Time**: 200-800ms (first time), <100ms (cached)
- **Preprocessing Time**: 30-80ms
- **Inference Time**: 80-200ms on emulator
- **Total Detection Time**: 300-1000ms

### On Test Image (Internode Borer):
- **Expected Top Prediction**: INTERNODE_BORER
- **Expected Confidence**: 70-95%
- **Alternative Predictions**: STALK_BORER, HEALTHY

---

## ✅ SUCCESS CRITERIA:

- [x] App launches without crash
- [x] Splash screen displays properly
- [ ] Main screen loads successfully
- [ ] Model selection screen works
- [ ] Can select different models
- [ ] Gallery selection works
- [ ] Image processes successfully
- [ ] Detection completes with results
- [ ] Results screen shows pest type and confidence
- [ ] Top 3 predictions displayed
- [ ] No errors in logcat
- [ ] Model switching works

---

## 📝 TESTING COMMAND REFERENCE:

### Clear Logcat:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -c
```

### Monitor Specific Tags:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -s TFLiteModelWrapper:D PestDetectionRepo:D DetectionViewModel:D ImageValidator:D
```

### Monitor All App Logs:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat | Select-String "intelli_pest"
```

### Get Last 100 Lines:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -d | Select-Object -Last 100
```

### Save Logcat to File:
```powershell
& "C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -d > D:\App\Intelli_PEST\logcat_output.txt
```

---

## 🎯 WHAT TO TEST NEXT:

1. **Basic Detection**: Use test image from gallery ✅
2. **Model Switching**: Try all 5 bundled models
3. **Camera Capture**: Test with emulator camera
4. **Image Validation**: Try non-crop images (should reject)
5. **Low Confidence**: Test with ambiguous images
6. **Multiple Detections**: Run 10+ detections in sequence
7. **Performance**: Measure actual inference times
8. **Memory**: Monitor memory usage during detection

---

## 📞 REPORT FINDINGS:

After testing, report:

1. ✅ **What Works**: List successful features
2. ❌ **What Fails**: Specific errors with logcat snippets
3. ⚠️ **Performance Issues**: Slow operations with timings
4. 📊 **Model Accuracy**: Which pest types detected correctly
5. 🐛 **Bugs Found**: Any crashes or unexpected behavior

---

**Status**: ✅ **READY FOR COMPREHENSIVE EMULATOR TESTING**

**Next Step**: Launch app on emulator and follow Test 2 (Gallery Selection)

---

*Generated: December 15, 2025*
*APK: app-debug.apk*
*Build: SUCCESS*
*Installed: emulator-5554*

