# ═══════════════════════════════════════════════════════════════
# CLEANUP ASSETS - REMOVE NON-BUNDLED MODELS
# ═══════════════════════════════════════════════════════════════
# Removes all models except mobilenet_v2 to reduce APK size

Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║           CLEANUP ASSETS - REDUCE APK SIZE                   ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$modelsPath = "D:\App\Pest1\app\src\main\assets\models"

if (-not (Test-Path $modelsPath)) {
    Write-Host "✗ Models path not found: $modelsPath" -ForegroundColor Red
    exit 1
}

# Models to remove (all except mobilenet_v2)
$modelsToRemove = @(
    "alexnet",
    "darknet53",
    "efficientnet_b0",
    "ensemble_attention",
    "ensemble_concat",
    "ensemble_cross",
    "inception_v3",
    "resnet50",
    "super_ensemble",
    "yolo11n-cls"
)

Write-Host "⚠ WARNING: This will permanently delete the following models:" -ForegroundColor Yellow
Write-Host ""
foreach ($model in $modelsToRemove) {
    $modelPath = Join-Path $modelsPath $model
    if (Test-Path $modelPath) {
        $size = (Get-ChildItem -Path $modelPath -Recurse | Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        Write-Host "  - $model ($sizeMB MB)" -ForegroundColor Gray
    }
}
Write-Host ""
Write-Host "Only 'mobilenet_v2' will remain (bundled in APK)" -ForegroundColor Green
Write-Host ""

# Confirm
$confirmation = Read-Host "Type 'yes' to proceed"
if ($confirmation -ne 'yes') {
    Write-Host "✗ Operation cancelled" -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "Removing models..." -ForegroundColor Cyan
Write-Host ""

$removedCount = 0
$savedSpace = 0

foreach ($model in $modelsToRemove) {
    $modelPath = Join-Path $modelsPath $model

    if (Test-Path $modelPath) {
        try {
            $size = (Get-ChildItem -Path $modelPath -Recurse | Measure-Object -Property Length -Sum).Sum
            Remove-Item -Path $modelPath -Recurse -Force
            $removedCount++
            $savedSpace += $size
            Write-Host "  ✓ Removed: $model" -ForegroundColor Green
        }
        catch {
            Write-Host "  ✗ Failed to remove: $model - $_" -ForegroundColor Red
        }
    }
    else {
        Write-Host "  ⊘ Not found: $model (already removed)" -ForegroundColor Gray
    }
}

$savedSpaceMB = [math]::Round($savedSpace / 1MB, 2)
$savedSpaceGB = [math]::Round($savedSpace / 1GB, 2)

Write-Host ""
Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                     CLEANUP COMPLETE                         ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "✓ Removed Models: $removedCount" -ForegroundColor Green
Write-Host "✓ Space Saved: $savedSpaceMB MB ($savedSpaceGB GB)" -ForegroundColor Green
Write-Host ""

# Verify what's left
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "REMAINING MODELS IN ASSETS:" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

$remainingModels = Get-ChildItem -Path $modelsPath -Directory

if ($remainingModels.Count -eq 0) {
    Write-Host "  ⚠ WARNING: No models found! mobilenet_v2 should remain!" -ForegroundColor Yellow
}
else {
    foreach ($model in $remainingModels) {
        $size = (Get-ChildItem -Path $model.FullName -Recurse | Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        Write-Host "  ✓ $($model.Name) ($sizeMB MB)" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "📋 NEXT STEPS:" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Build the APK:" -ForegroundColor White
Write-Host "   cd D:\App\Pest1" -ForegroundColor Gray
Write-Host "   .\gradlew clean" -ForegroundColor Gray
Write-Host "   .\gradlew assembleRelease" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Expected APK size: 20-30 MB (down from ~2GB!)" -ForegroundColor White
Write-Host ""
Write-Host "3. APK location:" -ForegroundColor White
Write-Host "   app\build\outputs\apk\release\app-release.apk" -ForegroundColor Gray
Write-Host ""

