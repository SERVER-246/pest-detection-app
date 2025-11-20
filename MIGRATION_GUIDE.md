# Pest Detection App - Migration Guide

## 🔴 CRITICAL: APK Size Reduction Required

Your app currently has ~2GB of models in the `assets` folder, making the APK impossible to deploy. Follow this guide to fix it.

---

## Step 1: Clean Up Assets Folder (REQUIRED)

### Keep Only ONE Model in Assets

Navigate to `app/src/main/assets/models/` and **DELETE** all folders except `mobilenet_v2`:

```
app/src/main/assets/models/
├── mobilenet_v2/          ← KEEP THIS ONLY
│   ├── model.onnx
│   ├── labels.txt
│   ├── metadata.json
│   └── class_mapping.json
├── darknet53/             ← DELETE
├── resnet50/              ← DELETE
├── yolo11n-cls/           ← DELETE
├── inception_v3/          ← DELETE
├── efficientnet_b0/       ← DELETE
├── alexnet/               ← DELETE
├── ensemble_attention/    ← DELETE
├── ensemble_cross/        ← DELETE
├── ensemble_concat/       ← DELETE
└── super_ensemble/        ← DELETE
```

**Action Required:**
```powershell
# Navigate to your project
cd D:\App\Pest1\app\src\main\assets\models

# Delete all models except mobilenet_v2
Remove-Item -Recurse -Force darknet53, resnet50, yolo11n-cls, inception_v3, efficientnet_b0, alexnet, ensemble_attention, ensemble_cross, ensemble_concat, super_ensemble
```

**Result:** APK size will drop from ~2GB to ~45MB! ✅

---

## Step 2: Upload Models to Cloud Storage

You need to upload the deleted models to a cloud storage service so users can download them on-demand.

### Option A: Firebase Storage (Recommended)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing
3. Enable Firebase Storage
4. Create folder structure:
   ```
   models/
   ├── darknet53.zip
   ├── resnet50.zip
   ├── yolo11n-cls.zip
   ├── inception_v3.zip
   ├── efficientnet_b0.zip
   ├── alexnet.zip
   ├── ensemble_attention.zip
   ├── ensemble_cross.zip
   ├── ensemble_concat.zip
   └── super_ensemble.zip
   ```

5. **Prepare Zip Files:**
   ```powershell
   # Zip each model folder
   Compress-Archive -Path "D:\App\Pest1\app\src\main\assets\models\darknet53\*" -DestinationPath "darknet53.zip"
   Compress-Archive -Path "D:\App\Pest1\app\src\main\assets\models\resnet50\*" -DestinationPath "resnet50.zip"
   # ... repeat for all models
   ```

6. Upload to Firebase Storage
7. Make files publicly accessible and copy download URLs

### Option B: AWS S3, Azure Blob, or Google Cloud Storage

Similar process - upload zip files and get public URLs.

### Option C: GitHub Releases (Free, for small projects)

1. Create a new repository on GitHub
2. Create a release
3. Upload zip files as release assets
4. Copy download URLs (they look like: `https://github.com/username/repo/releases/download/v1.0/darknet53.zip`)

---

## Step 3: Update Model URLs in Code

Edit `app/src/main/java/com/example/pest_1/data/model/ModelInfo.kt`:

Find this line:
```kotlin
private const val MODEL_BASE_URL = "https://your-storage-url.com/models"
```

Replace with your actual URL:
```kotlin
// For Firebase Storage:
private const val MODEL_BASE_URL = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/models"

// For GitHub Releases:
private const val MODEL_BASE_URL = "https://github.com/username/repo/releases/download/v1.0"

// For AWS S3:
private const val MODEL_BASE_URL = "https://your-bucket.s3.amazonaws.com/models"
```

---

## Step 4: Verify the Changes

### Check APK Size Before Building

```powershell
# Check assets folder size
Get-ChildItem "D:\App\Pest1\app\src\main\assets\models" -Recurse | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="SizeMB";Expression={[math]::Round($_.Sum / 1MB, 2)}}
```

**Expected Result:** Should be ~14 MB (only mobilenet_v2)

### Build the App

```powershell
cd D:\App\Pest1
.\gradlew assembleDebug
```

Check the APK size:
```powershell
Get-Item "app\build\outputs\apk\debug\app-debug.apk" | 
    Select-Object Name, @{Name="SizeMB";Expression={[math]::Round($_.Length / 1MB, 2)}}
```

**Expected Result:** ~45-60 MB (depending on dependencies)

---

## Step 5: Test the App

1. Install the APK on your device
2. Open the app
3. Select MobileNet V2 (default) - should work immediately
4. Select another model (e.g., ResNet50) - app will prompt to download
5. Verify download and classification work

---

## How the New System Works

### Default Model (Bundled)
- **MobileNet V2** is included in APK (~14MB)
- Works immediately, no download needed
- Good balance of accuracy (98.74%) and speed

### On-Demand Models (Downloaded)
- User selects a model not yet downloaded
- App shows download dialog with model size
- Downloads zip file from cloud storage
- Extracts to app's private storage
- Caches for future use

### Flow Diagram
```
User selects model
    ↓
Is model bundled? → YES → Load immediately → Classify
    ↓ NO
Is model cached? → YES → Load from storage → Classify
    ↓ NO
Show download dialog
    ↓ User confirms
Download from cloud (progress shown)
    ↓
Extract and cache
    ↓
Load and use → Classify
```

---

## Storage Management

### Where Downloaded Models Are Stored
```
/data/data/com.example.pest_1/files/models/
├── darknet53/
├── resnet50/
└── ...
```

### User Can Delete Models
The app automatically manages storage. In future versions, you can add:
- Settings screen to view downloaded models
- Delete button to free up space
- Auto-cleanup of old models

---

## Troubleshooting

### "Model not available" error
- Check internet connection
- Verify MODEL_BASE_URL is correct
- Check cloud storage URLs are public

### "Download failed" error
- Check file exists at URL
- Verify zip file structure (should contain model.onnx, labels.txt, etc.)
- Check device storage space

### Classification fails
- Check logcat for specific error messages
- Verify model.onnx is valid ONNX format
- Ensure labels.txt matches model output classes

### APK still too large
- Verify you deleted all models except mobilenet_v2
- Clean and rebuild: `.\gradlew clean assembleDebug`
- Check no duplicate models in src/androidTest or src/test

---

## Performance Expectations

| Metric | Before Fix | After Fix |
|--------|-----------|-----------|
| APK Size | ~2000 MB | ~45 MB |
| First Install Time | Never completes | < 1 minute |
| First Launch | Crash/OOM | < 5 seconds |
| Model Switch (cached) | Slow | 2-3 seconds |
| Model Download | N/A | 30-60 sec (depends on network) |
| Memory Usage | 1-2 GB | 200-400 MB |
| Classification Speed | N/A (didn't work) | 0.5-2 seconds |

---

## Next Steps (Optional Enhancements)

1. **Add ViewModel and LiveData** for better architecture
2. **Implement offline mode** with better error messages
3. **Add settings screen** for model management
4. **Cache downloaded models** with expiration
5. **Compress images** before classification to save memory
6. **Add batch classification** for multiple images
7. **Implement model versioning** for updates

---

## Support

If you encounter issues:
1. Check logcat output: `adb logcat | Select-String "OnnxModelManager|MainActivity"`
2. Review FIXES_IMPLEMENTED.md for details on what was changed
3. Verify all steps in this guide were followed

---

**✅ Once you complete Steps 1-3, your app will be deployable and functional!**

