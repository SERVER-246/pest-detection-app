# 🚀 Complete GitHub Setup Guide for Pest Detection Models

## Overview
This guide will help you upload your model files to GitHub so the Android app can download them automatically.

---

## 📋 Prerequisites

- GitHub account (free)
- Git installed on your computer
- All model files in `D:\App\Pest1\app\src\main\assets\models`

---

## 🎯 Step-by-Step Setup

### Step 1: Create Model Zip Files (10 minutes)

**Run this PowerShell script to create all zip files:**

```powershell
cd D:\App\Pest1

# Create a backup/models directory
New-Item -ItemType Directory -Force -Path "models-backup"

# List of models to zip (excluding mobilenet_v2 which stays in APK)
$models = @(
    "darknet53",
    "resnet50",
    "yolo11n-cls",
    "inception_v3",
    "efficientnet_b0",
    "alexnet",
    "ensemble_attention",
    "ensemble_cross",
    "ensemble_concat",
    "super_ensemble"
)

Write-Host "Creating zip files for GitHub upload..." -ForegroundColor Cyan
Write-Host ""

foreach ($model in $models) {
    $sourcePath = "app\src\main\assets\models\$model"
    $destZip = "models-backup\$model.zip"
    
    if (Test-Path $sourcePath) {
        Write-Host "Zipping: $model..." -ForegroundColor Yellow
        
        # Create zip file
        Compress-Archive -Path "$sourcePath\*" -DestinationPath $destZip -Force
        
        # Check zip file size
        $zipSize = [math]::Round((Get-Item $destZip).Length / 1MB, 2)
        Write-Host "  ✓ Created: $model.zip ($zipSize MB)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Not found: $sourcePath" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "All zip files created in: models-backup\" -ForegroundColor Green
Write-Host ""

# List all created files
Write-Host "Files ready for upload:" -ForegroundColor Cyan
Get-ChildItem "models-backup\*.zip" | ForEach-Object {
    $size = [math]::Round($_.Length / 1MB, 2)
    Write-Host "  - $($_.Name): $size MB"
}

Write-Host ""
Write-Host "Next: Upload these to GitHub!" -ForegroundColor Yellow
```

Save this as `create-model-zips.ps1` and run it.

---

### Step 2: Create GitHub Repository (5 minutes)

**Option A: Via GitHub Website (Easier)**

1. Go to https://github.com/new
2. Repository name: `pest-detection-models`
3. Description: "ONNX models for Pest Detection Android app"
4. Visibility: **Public** (required for direct downloads)
5. Initialize: **Check** "Add a README file"
6. Click "Create repository"

**Option B: Via Git Command Line**

```powershell
cd D:\App\Pest1
mkdir pest-detection-models-repo
cd pest-detection-models-repo

# Initialize git repo
git init

# Copy the README we created
copy ..\models-repo-README.md README.md

# Create .gitignore
echo "*.onnx" > .gitignore
echo "*.zip" >> .gitignore

# Initial commit
git add .
git commit -m "Initial commit - Model repository"

# Create GitHub repo and push (replace USERNAME)
git remote add origin https://github.com/USERNAME/pest-detection-models.git
git branch -M main
git push -u origin main
```

---

### Step 3: Create GitHub Release with Model Files (15 minutes)

**Via GitHub Website (Recommended for large files):**

1. Go to your repository: `https://github.com/USERNAME/pest-detection-models`

2. Click "Releases" → "Create a new release"

3. Fill in release details:
   - **Tag version**: `v1.0`
   - **Release title**: `Model Files v1.0`
   - **Description**:
     ```
     Initial release of ONNX models for Pest Detection app
     
     Contains 10 pre-trained models for pest classification:
     - DarkNet53, ResNet50, YOLO11n, Inception V3, EfficientNet B0
     - AlexNet, and 4 ensemble models
     
     Each zip contains: model.onnx, labels.txt, metadata.json, class_mapping.json
     ```

4. **Upload model zip files**:
   - Drag and drop all zip files from `D:\App\Pest1\models-backup\` 
   - Or click "Attach binaries" and select all zips
   - Wait for uploads to complete (may take 10-20 minutes depending on internet speed)

5. Click "Publish release"

6. **Copy the download URLs** - they will be in format:
   ```
   https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/darknet53.zip
   ```

---

### Step 4: Update Android App Configuration (2 minutes)

**Edit:** `D:\App\Pest1\app\src\main\java\com\example\pest_1\data\model\ModelInfo.kt`

Find line 40 and update:

```kotlin
// OLD:
private const val MODEL_BASE_URL = "https://your-storage-url.com/models"

// NEW (replace USERNAME with your GitHub username):
private const val MODEL_BASE_URL = "https://github.com/USERNAME/pest-detection-models/releases/download/v1.0"
```

**Example:**
```kotlin
private const val MODEL_BASE_URL = "https://github.com/johndoe/pest-detection-models/releases/download/v1.0"
```

---

### Step 5: Test the URLs (2 minutes)

Test that URLs work by opening in browser:

```
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/darknet53.zip
```

Should start downloading the zip file immediately.

---

### Step 6: Clean Up Assets Folder (5 minutes)

**NOW you can safely delete models from assets:**

```powershell
cd D:\App\Pest1

# Run the cleanup script
powershell -ExecutionPolicy Bypass -File .\cleanup_assets.ps1
```

Or manually:
```powershell
cd D:\App\Pest1\app\src\main\assets\models

# Delete all except mobilenet_v2
Remove-Item -Recurse -Force darknet53, resnet50, yolo11n-cls, inception_v3, efficientnet_b0, alexnet, ensemble_attention, ensemble_cross, ensemble_concat, super_ensemble
```

**Verify only mobilenet_v2 remains:**
```powershell
Get-ChildItem "D:\App\Pest1\app\src\main\assets\models" -Directory
# Should show only: mobilenet_v2
```

---

## 🎯 Complete URLs Reference

After creating the release, your download URLs will be:

```
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/darknet53.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/resnet50.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/yolo11n-cls.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/inception_v3.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/efficientnet_b0.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/alexnet.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/ensemble_attention.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/ensemble_cross.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/ensemble_concat.zip
https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/super_ensemble.zip
```

---

## ✅ Verification Checklist

- [ ] All model zips created in `models-backup/` folder
- [ ] GitHub repository created and public
- [ ] Release v1.0 created
- [ ] All 10 zip files uploaded to release
- [ ] Release published (not draft)
- [ ] Download URL tested in browser (downloads zip)
- [ ] `ModelInfo.kt` updated with correct URL
- [ ] Assets folder cleaned (only mobilenet_v2 remains)
- [ ] App builds successfully
- [ ] APK size < 100 MB

---

## 🐛 Troubleshooting

### "Upload fails for large files"
- GitHub has 2GB limit per file (your files should be fine)
- Try uploading 2-3 files at a time instead of all at once
- Use Git LFS if files are too large (unlikely for your case)

### "Download URL returns 404"
- Make sure release is **published** (not draft)
- Repository must be **public**
- Check URL format is exact
- File name in URL must match uploaded file name exactly

### "App still downloads from wrong URL"
- Clean and rebuild app: `.\gradlew clean assembleDebug`
- Uninstall old APK from device before installing new one
- Check logcat for actual URL being used

---

## 📊 Expected Timeline

1. Create zips: **10 minutes**
2. Create GitHub repo: **5 minutes**
3. Upload to release: **15-30 minutes** (depending on internet)
4. Update app config: **2 minutes**
5. Clean assets: **5 minutes**
6. Test build: **10 minutes**

**Total: ~45-60 minutes**

---

## 🎉 Success!

Once complete:
- ✅ Models hosted on GitHub (free, reliable)
- ✅ Direct download URLs available
- ✅ APK reduced to ~45MB
- ✅ Users can download models on-demand
- ✅ Easy to update models (just create new release)

---

## 🔄 Updating Models in Future

To update models later:

1. Create new zip files with updated models
2. Create new release (v1.1, v2.0, etc.)
3. Upload new zips
4. Update `MODEL_BASE_URL` in app to point to new version
5. Release app update

---

## 📱 How It Works in the App

1. User opens app → MobileNet V2 loads (bundled in APK)
2. User selects ResNet50 → App checks if downloaded
3. If not downloaded → Shows dialog: "Download ResNet50 (97.8 MB)?"
4. User confirms → App downloads from GitHub
5. Downloads to: `/data/data/com.example.pest_1/files/models/resnet50/`
6. Extracts zip → Caches for future use
7. Loads model → Runs classification

**Benefits:**
- Small APK size
- Fast install
- Users only download models they need
- Models cached for offline use

---

## 💡 Alternative: GitHub LFS (If needed)

If you need better bandwidth/reliability for very large files:

```powershell
# Install Git LFS
git lfs install

# Track large files
git lfs track "*.zip"
git add .gitattributes

# Add and commit
git add models-backup/*.zip
git commit -m "Add model files via LFS"
git push
```

But for your use case, **GitHub Releases is simpler and sufficient**.

---

**Ready to proceed? Start with Step 1: Create the zip files!**

