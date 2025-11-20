# ============================================================================
# Comprehensive Pest Detection App Setup and Build Script
# ============================================================================
# This script performs all necessary steps to fix the 2GB APK issue and
# prepare the app for deployment on mobile devices.
# ============================================================================

param(
    [switch]$SkipCleanup,
    [switch]$SkipBuild,
    [switch]$SkipTest
)

$ErrorActionPreference = "Stop"

Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Pest Detection App - Comprehensive Setup & Build Script    ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "D:\App\Pest1"
$assetsPath = "$projectRoot\app\src\main\assets\models"

# ============================================================================
# STEP 1: Cleanup Large Models from Assets
# ============================================================================

if (-not $SkipCleanup) {
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host "STEP 1: Cleaning up large models from assets folder" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host ""

    if (-not (Test-Path $assetsPath)) {
        Write-Host "❌ Error: Assets path not found: $assetsPath" -ForegroundColor Red
        exit 1
    }

    Write-Host "📊 Current models in assets:" -ForegroundColor Cyan
    $totalSize = 0
    Get-ChildItem $assetsPath -Directory | ForEach-Object {
        $size = (Get-ChildItem $_.FullName -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum / 1MB
        $totalSize += $size
        Write-Host "   • $($_.Name): $([math]::Round($size, 2)) MB" -ForegroundColor White
    }
    Write-Host "   📦 Total size: $([math]::Round($totalSize, 2)) MB" -ForegroundColor Yellow
    Write-Host ""

    $modelsToDelete = @(
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

    Write-Host "🗑️  Models to DELETE (will be available for download):" -ForegroundColor Red
    $deleteSize = 0
    foreach ($model in $modelsToDelete) {
        $path = Join-Path $assetsPath $model
        if (Test-Path $path) {
            $size = (Get-ChildItem $path -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum / 1MB
            $deleteSize += $size
            Write-Host "   ✗ $model ($([math]::Round($size, 2)) MB)" -ForegroundColor Red
        }
    }
    Write-Host ""

    Write-Host "✅ Model to KEEP (bundled in APK):" -ForegroundColor Green
    $keepPath = Join-Path $assetsPath "mobilenet_v2"
    if (Test-Path $keepPath) {
        $keepSize = (Get-ChildItem $keepPath -Recurse -File | Measure-Object -Property Length -Sum).Sum / 1MB
        Write-Host "   ✓ mobilenet_v2 ($([math]::Round($keepSize, 2)) MB)" -ForegroundColor Green
    }
    Write-Host ""

    Write-Host "📉 Expected APK size reduction: $([math]::Round($deleteSize, 2)) MB" -ForegroundColor Magenta
    Write-Host ""

    $confirm = Read-Host "❓ Proceed with deletion? (yes/no)"

    if ($confirm -eq "yes") {
        $deletedCount = 0
        foreach ($model in $modelsToDelete) {
            $path = Join-Path $assetsPath $model
            if (Test-Path $path) {
                try {
                    Remove-Item -Path $path -Recurse -Force
                    Write-Host "   ✓ Deleted: $model" -ForegroundColor Green
                    $deletedCount++
                } catch {
                    Write-Host "   ✗ Failed to delete: $model - $($_.Exception.Message)" -ForegroundColor Red
                }
            }
        }
        Write-Host ""
        Write-Host "✅ Successfully deleted $deletedCount model(s)" -ForegroundColor Green
        Write-Host ""

        # Verify cleanup
        Write-Host "📊 Remaining models in assets:" -ForegroundColor Cyan
        Get-ChildItem $assetsPath -Directory | ForEach-Object {
            Write-Host "   • $($_.Name)" -ForegroundColor Green
        }
        Write-Host ""
    } else {
        Write-Host "⚠️  Cleanup cancelled by user" -ForegroundColor Yellow
        Write-Host ""
    }
} else {
    Write-Host "⏭️  Skipping cleanup (--SkipCleanup flag)" -ForegroundColor Yellow
    Write-Host ""
}

# ============================================================================
# STEP 2: Clean Build Artifacts
# ============================================================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "STEP 2: Cleaning build artifacts" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

Set-Location $projectRoot

if (Test-Path ".\gradlew.bat") {
    Write-Host "🧹 Running Gradle clean..." -ForegroundColor Cyan
    .\gradlew.bat clean
    Write-Host ""
    Write-Host "✅ Build artifacts cleaned" -ForegroundColor Green
} else {
    Write-Host "⚠️  gradlew.bat not found, skipping Gradle clean" -ForegroundColor Yellow
}
Write-Host ""

# ============================================================================
# STEP 3: Verify Code Configuration
# ============================================================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "STEP 3: Verifying code configuration" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

$modelInfoPath = "$projectRoot\app\src\main\java\com\example\pest_1\data\model\ModelInfo.kt"
$content = Get-Content $modelInfoPath -Raw

if ($content -match 'MODEL_BASE_URL = "https://your-storage-url.com/models"') {
    Write-Host "⚠️  WARNING: MODEL_BASE_URL is still set to placeholder!" -ForegroundColor Red
    Write-Host "   File: $modelInfoPath" -ForegroundColor Yellow
    Write-Host "   You need to:" -ForegroundColor Yellow
    Write-Host "   1. Upload models to GitHub Releases (use create-github-models.ps1)" -ForegroundColor Yellow
    Write-Host "   2. Update MODEL_BASE_URL to your GitHub releases URL" -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host "✅ MODEL_BASE_URL is configured" -ForegroundColor Green
    Write-Host ""
}

# ============================================================================
# STEP 4: Build Debug APK
# ============================================================================

if (-not $SkipBuild) {
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host "STEP 4: Building Debug APK" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host ""

    if (Test-Path ".\gradlew.bat") {
        Write-Host "🔨 Building APK (this may take a few minutes)..." -ForegroundColor Cyan
        Write-Host ""

        .\gradlew.bat assembleDebug

        Write-Host ""
        $apkPath = "$projectRoot\app\build\outputs\apk\debug\app-debug.apk"

        if (Test-Path $apkPath) {
            $apkSize = (Get-Item $apkPath).Length / 1MB
            Write-Host "✅ APK built successfully!" -ForegroundColor Green
            Write-Host "   📁 Location: $apkPath" -ForegroundColor Cyan
            Write-Host "   📦 Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
            Write-Host ""

            if ($apkSize -gt 100) {
                Write-Host "⚠️  WARNING: APK size is still large (>100MB)" -ForegroundColor Red
                Write-Host "   This might indicate models were not properly removed from assets" -ForegroundColor Yellow
                Write-Host ""
            } elseif ($apkSize -lt 60) {
                Write-Host "🎉 Excellent! APK size is optimized (<60MB)" -ForegroundColor Green
                Write-Host ""
            }
        } else {
            Write-Host "❌ APK not found at expected location" -ForegroundColor Red
            Write-Host ""
        }
    } else {
        Write-Host "⚠️  gradlew.bat not found, cannot build APK" -ForegroundColor Yellow
        Write-Host ""
    }
} else {
    Write-Host "⏭️  Skipping build (--SkipBuild flag)" -ForegroundColor Yellow
    Write-Host ""
}

# ============================================================================
# STEP 5: Summary and Next Steps
# ============================================================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "SETUP COMPLETE!" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

Write-Host "📋 Next Steps:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. 📤 Upload Models to GitHub (if not done yet):" -ForegroundColor White
Write-Host "   .\create-github-models.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. ⚙️  Update Model URL in code:" -ForegroundColor White
Write-Host "   File: app\src\main\java\com\example\pest_1\data\model\ModelInfo.kt" -ForegroundColor Cyan
Write-Host "   Change MODEL_BASE_URL to your GitHub releases URL" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. 📱 Install APK on Android device:" -ForegroundColor White
Write-Host "   adb install app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
Write-Host ""
Write-Host "4. ✅ Test on device:" -ForegroundColor White
Write-Host "   • Open app" -ForegroundColor Cyan
Write-Host "   • Take/upload a pest image" -ForegroundColor Cyan
Write-Host "   • Test classification with MobileNet V2 (bundled)" -ForegroundColor Cyan
Write-Host "   • Try downloading another model (tests network feature)" -ForegroundColor Cyan
Write-Host ""
Write-Host "5. 🚀 For production, build release APK:" -ForegroundColor White
Write-Host "   .\gradlew.bat assembleRelease" -ForegroundColor Cyan
Write-Host ""

Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                   Setup Script Complete!                     ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

