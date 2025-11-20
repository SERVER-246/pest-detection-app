# Pest App - Quick Cleanup Script
# This script removes all models except mobilenet_v2 from assets
# Run this from PowerShell to reduce APK size from ~2GB to ~45MB

Write-Host "=== Pest App Asset Cleanup ===" -ForegroundColor Cyan
Write-Host ""

$assetsPath = "D:\App\Pest1\app\src\main\assets\models"

if (-not (Test-Path $assetsPath)) {
    Write-Host "Error: Assets path not found: $assetsPath" -ForegroundColor Red
    exit 1
}

Write-Host "Current models in assets:" -ForegroundColor Yellow
Get-ChildItem $assetsPath -Directory | ForEach-Object {
    $size = (Get-ChildItem $_.FullName -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "  - $($_.Name): $([math]::Round($size, 2)) MB"
}

Write-Host ""
Write-Host "Models to DELETE:" -ForegroundColor Red
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

foreach ($model in $modelsToDelete) {
    $path = Join-Path $assetsPath $model
    if (Test-Path $path) {
        $size = (Get-ChildItem $path -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
        Write-Host "  - $model ($([math]::Round($size, 2)) MB)"
    }
}

Write-Host ""
Write-Host "Model to KEEP:" -ForegroundColor Green
Write-Host "  - mobilenet_v2 (default bundled model)"
Write-Host ""

$confirm = Read-Host "Do you want to proceed with deletion? (yes/no)"

if ($confirm -ne "yes") {
    Write-Host "Cancelled." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Deleting models..." -ForegroundColor Yellow

$deletedCount = 0
$freedSpace = 0

foreach ($model in $modelsToDelete) {
    $path = Join-Path $assetsPath $model
    if (Test-Path $path) {
        $size = (Get-ChildItem $path -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
        try {
            Remove-Item -Path $path -Recurse -Force
            Write-Host "  ✓ Deleted: $model" -ForegroundColor Green
            $deletedCount++
            $freedSpace += $size
        } catch {
            Write-Host "  ✗ Failed to delete: $model - $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  - Not found: $model (already deleted?)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "=== Cleanup Complete ===" -ForegroundColor Cyan
Write-Host "  Models deleted: $deletedCount" -ForegroundColor Green
Write-Host "  Space freed: $([math]::Round($freedSpace, 2)) MB" -ForegroundColor Green
Write-Host ""

# Show remaining size
$remainingSize = (Get-ChildItem $assetsPath -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
Write-Host "Remaining assets size: $([math]::Round($remainingSize, 2)) MB" -ForegroundColor Cyan

if ($remainingSize -lt 50) {
    Write-Host ""
    Write-Host "✅ SUCCESS! Assets folder is now small enough." -ForegroundColor Green
    Write-Host "   Expected APK size: ~45-60 MB" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "  1. Upload deleted models to cloud storage (Firebase, AWS S3, etc.)"
    Write-Host "  2. Update MODEL_BASE_URL in ModelInfo.kt with your storage URL"
    Write-Host "  3. Build and test the app"
    Write-Host ""
    Write-Host "See MIGRATION_GUIDE.md for detailed instructions."
} else {
    Write-Host ""
    Write-Host "⚠️  WARNING: Assets folder still large!" -ForegroundColor Yellow
    Write-Host "   Current size: $([math]::Round($remainingSize, 2)) MB"
    Write-Host "   Expected: < 50 MB"
    Write-Host ""
    Write-Host "Check for duplicate models or large files in assets."
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

