# ============================================================================
# Pest Detection App - Pre-Build Validation Script
# ============================================================================
# Validates that all prerequisites are met before building the app
# ============================================================================

$ErrorActionPreference = "Continue"

Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║        Pest Detection App - Pre-Build Validation             ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "D:\App\Pest1"
$allChecksPassed = $true

# ============================================================================
# Check 1: Assets Folder - Only mobilenet_v2 should exist
# ============================================================================

Write-Host "✓ Check 1: Assets Folder Validation" -ForegroundColor Yellow
$assetsPath = "$projectRoot\app\src\main\assets\models"

if (Test-Path $assetsPath) {
    $models = Get-ChildItem $assetsPath -Directory
    $modelNames = $models | ForEach-Object { $_.Name }

    if ($modelNames.Count -eq 1 -and $modelNames[0] -eq "mobilenet_v2") {
        Write-Host "  ✅ PASS: Only mobilenet_v2 exists in assets" -ForegroundColor Green
        $mobilenetPath = "$assetsPath\mobilenet_v2"
        $mobilenetSize = (Get-ChildItem $mobilenetPath -Recurse -File | Measure-Object -Property Length -Sum).Sum / 1MB
        Write-Host "     Size: $([math]::Round($mobilenetSize, 2)) MB" -ForegroundColor Gray
    } else {
        Write-Host "  ❌ FAIL: Unexpected models in assets folder" -ForegroundColor Red
        Write-Host "     Found: $($modelNames -join ', ')" -ForegroundColor Red
        Write-Host "     Expected: mobilenet_v2 only" -ForegroundColor Yellow
        Write-Host "     ACTION: Run cleanup_assets.ps1 or COMPREHENSIVE_SETUP.ps1" -ForegroundColor Yellow
        $allChecksPassed = $false
    }

    # Check mobilenet_v2 contents
    $mobilenetPath = "$assetsPath\mobilenet_v2"
    if (Test-Path $mobilenetPath) {
        $requiredFiles = @("model.onnx", "labels.txt", "metadata.json", "class_mapping.json")
        $missingFiles = @()

        foreach ($file in $requiredFiles) {
            if (-not (Test-Path "$mobilenetPath\$file")) {
                $missingFiles += $file
            }
        }

        if ($missingFiles.Count -eq 0) {
            Write-Host "  ✅ PASS: All required files present in mobilenet_v2" -ForegroundColor Green
        } else {
            Write-Host "  ❌ FAIL: Missing files in mobilenet_v2: $($missingFiles -join ', ')" -ForegroundColor Red
            $allChecksPassed = $false
        }
    }
} else {
    Write-Host "  ❌ FAIL: Assets models folder not found" -ForegroundColor Red
    $allChecksPassed = $false
}
Write-Host ""

# ============================================================================
# Check 2: Gradle Build Files
# ============================================================================

Write-Host "✓ Check 2: Gradle Configuration" -ForegroundColor Yellow

$gradleFiles = @(
    "$projectRoot\build.gradle.kts",
    "$projectRoot\app\build.gradle.kts",
    "$projectRoot\settings.gradle.kts",
    "$projectRoot\gradlew.bat"
)

$allGradleFilesExist = $true
foreach ($file in $gradleFiles) {
    if (Test-Path $file) {
        Write-Host "  ✅ Found: $(Split-Path $file -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Missing: $(Split-Path $file -Leaf)" -ForegroundColor Red
        $allGradleFilesExist = $false
        $allChecksPassed = $false
    }
}

if ($allGradleFilesExist) {
    Write-Host "  ✅ PASS: All Gradle files present" -ForegroundColor Green
}
Write-Host ""

# ============================================================================
# Check 3: Key Source Files
# ============================================================================

Write-Host "✓ Check 3: Source Code Files" -ForegroundColor Yellow

$sourceFiles = @(
    "$projectRoot\app\src\main\java\com\example\pest_1\MainActivity.kt",
    "$projectRoot\app\src\main\java\com\example\pest_1\OnnxModelManager.kt",
    "$projectRoot\app\src\main\java\com\example\pest_1\data\model\ModelInfo.kt",
    "$projectRoot\app\src\main\java\com\example\pest_1\data\model\ModelRepository.kt",
    "$projectRoot\app\src\main\java\com\example\pest_1\data\model\ModelDownloader.kt",
    "$projectRoot\app\src\main\java\com\example\pest_1\domain\model\PredictionResult.kt"
)

$allSourceFilesExist = $true
foreach ($file in $sourceFiles) {
    if (Test-Path $file) {
        Write-Host "  ✅ Found: $(Split-Path $file -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Missing: $(Split-Path $file -Leaf)" -ForegroundColor Red
        $allSourceFilesExist = $false
        $allChecksPassed = $false
    }
}

if ($allSourceFilesExist) {
    Write-Host "  ✅ PASS: All source files present" -ForegroundColor Green
}
Write-Host ""

# ============================================================================
# Check 4: Resource Files
# ============================================================================

Write-Host "✓ Check 4: Resource Files" -ForegroundColor Yellow

$resourceFiles = @(
    "$projectRoot\app\src\main\res\layout\activity_main.xml",
    "$projectRoot\app\src\main\res\values\strings.xml",
    "$projectRoot\app\src\main\res\values\colors.xml",
    "$projectRoot\app\src\main\AndroidManifest.xml"
)

$allResourceFilesExist = $true
foreach ($file in $resourceFiles) {
    if (Test-Path $file) {
        Write-Host "  ✅ Found: $(Split-Path $file -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Missing: $(Split-Path $file -Leaf)" -ForegroundColor Red
        $allResourceFilesExist = $false
        $allChecksPassed = $false
    }
}

if ($allResourceFilesExist) {
    Write-Host "  ✅ PASS: All resource files present" -ForegroundColor Green
}
Write-Host ""

# ============================================================================
# Check 5: Model URL Configuration
# ============================================================================

Write-Host "✓ Check 5: Model Download Configuration" -ForegroundColor Yellow

$modelInfoPath = "$projectRoot\app\src\main\java\com\example\pest_1\data\model\ModelInfo.kt"
if (Test-Path $modelInfoPath) {
    $content = Get-Content $modelInfoPath -Raw

    if ($content -match 'MODEL_BASE_URL = "https://your-storage-url.com/models"') {
        Write-Host "  ⚠️  WARNING: MODEL_BASE_URL is placeholder" -ForegroundColor Yellow
        Write-Host "     This is OK for testing bundled model only" -ForegroundColor Gray
        Write-Host "     To enable downloads, update URL to GitHub releases" -ForegroundColor Gray
    } elseif ($content -match 'MODEL_BASE_URL = "(https://[^"]+)"') {
        $url = $matches[1]
        Write-Host "  ✅ INFO: MODEL_BASE_URL is configured" -ForegroundColor Green
        Write-Host "     URL: $url" -ForegroundColor Gray
    } else {
        Write-Host "  ⚠️  WARNING: MODEL_BASE_URL pattern not found" -ForegroundColor Yellow
    }
}
Write-Host ""

# ============================================================================
# Check 6: Build Output Cleanup
# ============================================================================

Write-Host "✓ Check 6: Build Output Status" -ForegroundColor Yellow

$buildPath = "$projectRoot\app\build"
if (Test-Path $buildPath) {
    $buildSize = (Get-ChildItem $buildPath -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "  ⚠️  Build folder exists (Size: $([math]::Round($buildSize, 2)) MB)" -ForegroundColor Yellow
    Write-Host "     Recommend running: .\gradlew.bat clean" -ForegroundColor Gray
} else {
    Write-Host "  ✅ Build folder clean (no old artifacts)" -ForegroundColor Green
}

$apkPath = "$projectRoot\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "  ℹ️  Existing APK found (Size: $([math]::Round($apkSize, 2)) MB)" -ForegroundColor Cyan

    if ($apkSize -gt 100) {
        Write-Host "     ⚠️  APK is large - may have old models bundled" -ForegroundColor Yellow
        Write-Host "     Recommend rebuilding after asset cleanup" -ForegroundColor Gray
    } elseif ($apkSize -lt 60) {
        Write-Host "     ✅ APK size looks good!" -ForegroundColor Green
    }
}
Write-Host ""

# ============================================================================
# Check 7: Android SDK and Java
# ============================================================================

Write-Host "✓ Check 7: Development Environment" -ForegroundColor Yellow

# Check Java
try {
    $javaVersion = & java -version 2>&1 | Select-Object -First 1
    if ($javaVersion -match "version") {
        Write-Host "  ✅ Java is installed: $javaVersion" -ForegroundColor Green
    }
} catch {
    Write-Host "  ⚠️  Cannot verify Java installation" -ForegroundColor Yellow
    Write-Host "     Gradle will use built-in JDK" -ForegroundColor Gray
}

# Check Android SDK (via ANDROID_HOME)
if ($env:ANDROID_HOME) {
    Write-Host "  ✅ ANDROID_HOME set: $env:ANDROID_HOME" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  ANDROID_HOME not set (may be OK if using Android Studio)" -ForegroundColor Yellow
}

Write-Host ""

# ============================================================================
# Check 8: Documentation Files
# ============================================================================

Write-Host "✓ Check 8: Documentation" -ForegroundColor Yellow

$docFiles = @(
    "$projectRoot\TESTING_DEPLOYMENT_GUIDE.md",
    "$projectRoot\COMPREHENSIVE_SETUP.ps1",
    "$projectRoot\cleanup_assets.ps1",
    "$projectRoot\FIXES_IMPLEMENTED.md"
)

$docCount = 0
foreach ($file in $docFiles) {
    if (Test-Path $file) {
        $docCount++
    }
}

Write-Host "  ℹ️  Found $docCount/$($docFiles.Count) documentation files" -ForegroundColor Cyan
Write-Host ""

# ============================================================================
# Final Summary
# ============================================================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
if ($allChecksPassed) {
    Write-Host "✅ VALIDATION PASSED - Ready to Build!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "  1. Run: .\gradlew.bat clean assembleDebug" -ForegroundColor Cyan
    Write-Host "  2. Check APK size in: app\build\outputs\apk\debug\" -ForegroundColor Cyan
    Write-Host "  3. Install on device: adb install app-debug.apk" -ForegroundColor Cyan
    Write-Host "  4. Test classification with MobileNet V2" -ForegroundColor Cyan
} else {
    Write-Host "❌ VALIDATION FAILED - Issues Found" -ForegroundColor Red
    Write-Host ""
    Write-Host "Recommended Actions:" -ForegroundColor Yellow
    Write-Host "  1. Run: .\COMPREHENSIVE_SETUP.ps1 (fixes most issues)" -ForegroundColor Cyan
    Write-Host "  2. Review errors above and fix manually" -ForegroundColor Cyan
    Write-Host "  3. Re-run this validation script" -ForegroundColor Cyan
}
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Return exit code for automation
if ($allChecksPassed) {
    exit 0
} else {
    exit 1
}

