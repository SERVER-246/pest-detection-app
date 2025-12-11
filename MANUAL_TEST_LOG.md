# 📋 MANUAL TEST LOG - Intelli_PEST Camera & Gallery Validation

**Date:** December 10, 2025  
**Tester:** Automated Agent + User Verification  
**Build:** Debug APK (app-debug.apk)  
**Test Device:** Medium_Phone_API_36.1 Emulator (Android 16)  
**Sample Image:** `D:\Test-images\Internode borer\Real time Cane\IMG_0742.JPG`

---

## 🎯 TEST OBJECTIVES
1. Verify camera capture does NOT crash the application
2. Verify gallery import does NOT crash the application
3. Confirm images are processed through the detection pipeline successfully
4. Validate bitmap conversion to software format prevents hardware bitmap crashes
5. Ensure structured logging captures all steps for debugging

---

## 📱 TEST ENVIRONMENT
- **Emulator:** Medium_Phone_API_36.1 (AVD) - Android 16
- **APK:** app-debug.apk (installed successfully)
- **Package:** com.server246.intelli_pest.debug
- **Model Bundled:** super_ensemble.onnx
- **Test Image:** Internode borer sample (sugarcane pest)

---

## 🧪 TEST CASES

### Test Case 1: Camera Capture Flow
**Objective:** Capture image via camera without application crash

**Steps:**
1. Launch Intelli_PEST app on emulator
2. Grant camera permission when prompted
3. Tap "Camera" button on main screen
4. Position camera preview
5. Tap capture button
6. Wait for image processing
7. Observe results screen

**Expected Result:**
- Camera preview displays correctly
- Capture completes without crash
- Image is converted to software bitmap (logged)
- Detection result displays with pest type and confidence
- No "pixel access is not supported on Config#HARDWARE" errors

**Status:** ⏳ PENDING USER EXECUTION

**Actual Result:** 

**Logs Captured:** 

**Issues Found:** 

---

### Test Case 2: Gallery Import Flow
**Objective:** Import image from gallery without application crash

**Steps:**
1. Launch Intelli_PEST app on emulator
2. Tap "Gallery" button on main screen
3. Navigate to test image location: `D:\Test-images\Internode borer\Real time Cane\IMG_0742.JPG`
4. Select the image
5. Wait for image processing
6. Observe results screen

**Expected Result:**
- Gallery picker launches correctly
- Image loads without "unable to getPixel()" error
- Bitmap converted to ARGB_8888 software format (logged)
- Detection completes successfully
- Result shows "Internode borer" with confidence score
- No application crash

**Status:** ⏳ PENDING USER EXECUTION

**Actual Result:** 

**Logs Captured:** 

**Issues Found:** 

---

### Test Case 3: Bitmap Conversion Verification
**Objective:** Verify BitmapUtils.toSoftwareBitmap() is called and succeeds

**Steps:**
1. Enable logcat filtering: `adb logcat | findstr "BitmapUtils DetectPestUseCase PestDetectionRepo"`
2. Import test image from gallery
3. Capture logs during processing

**Expected Log Entries:**
```
D/DetectPestUseCase: invoke() called | model=super_ensemble | bitmap=WxH, config=...
D/BitmapUtils: Converting bitmap: WxH, config: ...
D/BitmapUtils: Successfully converted to software bitmap
D/DetectPestUseCase: Bitmap converted to software config=ARGB_8888
D/PestDetectionRepo: detectPest() start | model=super_ensemble | bitmap=WxH config=ARGB_8888
D/PestDetectionRepo: Detection success | pest=... | confidence=...
```

**Status:** ⏳ PENDING USER EXECUTION

**Actual Result:** 

---

### Test Case 4: Error Handling Verification
**Objective:** Confirm structured errors display correctly when issues occur

**Steps:**
1. Test with invalid/corrupted image (if available)
2. Observe error message displayed to user
3. Verify app does not crash, returns to main screen

**Expected Result:**
- Clear error message shown in UI
- Error logged with exception details
- App remains stable, allows retry

**Status:** ⏳ PENDING USER EXECUTION

**Actual Result:** 

---

## 📊 TEST RESULTS SUMMARY

| Test Case | Status | Pass/Fail | Notes |
|-----------|--------|-----------|-------|
| Camera Capture | PENDING | - | Awaiting execution |
| Gallery Import | PENDING | - | Awaiting execution |
| Bitmap Conversion | PENDING | - | Awaiting execution |
| Error Handling | PENDING | - | Awaiting execution |

---

## 🐛 ISSUES IDENTIFIED

### Critical Issues
*None identified yet - tests pending*

### Warnings
*None identified yet - tests pending*

### Observations
*To be filled after test execution*

---

## ✅ VALIDATION CHECKLIST

- [ ] App launches without crash
- [ ] Camera permission flow works correctly
- [ ] Camera preview displays properly
- [ ] Camera capture completes without crash
- [ ] Gallery picker opens correctly
- [ ] Gallery image import completes without crash
- [ ] Hardware bitmap conversion executes successfully
- [ ] Detection pipeline processes images end-to-end
- [ ] Results display correctly with pest type and confidence
- [ ] Structured logging captures all pipeline steps
- [ ] No "Config#HARDWARE" or "getPixel()" errors occur
- [ ] App handles errors gracefully without crashing

---

## 📝 NOTES FOR TESTER

**To Execute Tests:**
1. Launch the emulator and ensure app is installed
2. Open a terminal and run: `adb logcat -c && adb logcat | findstr "BitmapUtils DetectPestUseCase PestDetectionRepo CameraScreen GalleryPicker"`
3. Open the app on the emulator
4. Execute Test Case 1 (Camera) first
5. Execute Test Case 2 (Gallery) with the provided test image
6. Copy relevant logcat output into this document
7. Fill in "Actual Result" and "Issues Found" sections
8. Update the Test Results Summary table

**Sample Image Location:**
- Host Machine: `D:\Test-images\Internode borer\Real time Cane\IMG_0742.JPG`
- To push to emulator: `adb push "D:\Test-images\Internode borer\Real time Cane\IMG_0742.JPG" /sdcard/Download/`

---

## 🔄 NEXT STEPS AFTER VALIDATION

1. If tests PASS:
   - Update this document with success evidence
   - Proceed with signed release APK build
   - Update README with installation instructions
   - Push changes to GitHub

2. If tests FAIL:
   - Document exact failure point and error messages
   - Analyze logs to identify root cause
   - Apply targeted fixes
   - Rerun clean build and tests
   - Repeat validation

---

**Status:** 🟡 IN PROGRESS - Manual execution required  
**Last Updated:** December 10, 2025

