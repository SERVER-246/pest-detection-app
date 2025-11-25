# ═══════════════════════════════════════════════════════════════
# PREPARE MODEL PACKAGES FOR GITHUB RELEASE
# ═══════════════════════════════════════════════════════════════
# This script packages model files into zip archives ready for upload
# to GitHub Releases
# ═══════════════════════════════════════════════════════════════

$ErrorActionPreference = "Stop"

Write-Host "╔═══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║      MODEL PACKAGE CREATOR FOR GITHUB RELEASE               ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Configuration
$sourceModelsPath = "D:\App\Pest1\app\src\main\assets\models"
$outputDir = "D:\App\Pest1\release_models"

# Create output directory
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
    Write-Host "✓ Created output directory: $outputDir" -ForegroundColor Green
} else {
    Write-Host "✓ Output directory exists: $outputDir" -ForegroundColor Green
}

Write-Host ""
Write-Host "Scanning for model directories..." -ForegroundColor Cyan
Write-Host ""

# Get all model directories
$modelDirs = Get-ChildItem -Path $sourceModelsPath -Directory -ErrorAction SilentlyContinue

if ($modelDirs.Count -eq 0) {
    Write-Host "✗ No model directories found in: $sourceModelsPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "ℹ Note: Model files were cleaned from assets to reduce APK size." -ForegroundColor Yellow
    Write-Host "   If you need to package models, restore them from backup first." -ForegroundColor Yellow
    exit 0
}

Write-Host "Found $($modelDirs.Count) model(s):" -ForegroundColor White
foreach ($dir in $modelDirs) {
    Write-Host "  - $($dir.Name)" -ForegroundColor Gray
}
Write-Host ""

# Package each model
$packaged = 0
$skipped = 0

foreach ($modelDir in $modelDirs) {
    $modelName = $modelDir.Name
    $zipPath = Join-Path $outputDir "$modelName.zip"

    # Skip mobilenet_v2 (bundled in APK)
    if ($modelName -eq "mobilenet_v2") {
        Write-Host "⊘ Skipping $modelName (bundled in APK)" -ForegroundColor Gray
        $skipped++
        continue
    }

    Write-Host "📦 Packaging: $modelName" -ForegroundColor Cyan

    try {
        # Check if required files exist
        $modelFile = Join-Path $modelDir.FullName "model.onnx"
        $labelsFile = Join-Path $modelDir.FullName "labels.txt"
        $metadataFile = Join-Path $modelDir.FullName "metadata.json"

        if (-not (Test-Path $modelFile)) {
            Write-Host "   ✗ Missing model.onnx" -ForegroundColor Red
            $skipped++
            continue
        }

        # Get model size
        $modelSize = (Get-Item $modelFile).Length / 1MB
        $modelSizeMB = [math]::Round($modelSize, 2)

        # Create zip archive
        if (Test-Path $zipPath) {
            Remove-Item $zipPath -Force
        }

        Compress-Archive -Path "$($modelDir.FullName)\*" -DestinationPath $zipPath -CompressionLevel Optimal

        # Get zip size
        $zipSize = (Get-Item $zipPath).Length / 1MB
        $zipSizeMB = [math]::Round($zipSize, 2)

        Write-Host "   ✓ Created: $zipPath" -ForegroundColor Green
        Write-Host "   Size: $zipSizeMB MB (original: $modelSizeMB MB)" -ForegroundColor Gray
        $packaged++

    } catch {
        Write-Host "   ✗ Error: $($_.Exception.Message)" -ForegroundColor Red
        $skipped++
    }

    Write-Host ""
}

# Summary
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "SUMMARY" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Packaged: $packaged models" -ForegroundColor Green
Write-Host "Skipped:  $skipped models" -ForegroundColor Yellow
Write-Host ""

if ($packaged -gt 0) {
    Write-Host "📁 Model packages saved to: $outputDir" -ForegroundColor White
    Write-Host ""
    Write-Host "NEXT STEPS:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Go to: https://github.com/SERVER-246/pest-detection-app/releases/new" -ForegroundColor White
    Write-Host ""
    Write-Host "2. Create new release:" -ForegroundColor White
    Write-Host "   - Tag: v1.0.0" -ForegroundColor Gray
    Write-Host "   - Title: Pest Detection Models v1.0.0" -ForegroundColor Gray
    Write-Host "   - Description: ONNX models for pest detection" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Upload these files from $outputDir :" -ForegroundColor White

    Get-ChildItem -Path $outputDir -Filter "*.zip" | ForEach-Object {
        Write-Host "   - $($_.Name)" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "4. Publish the release" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "⚠ No models were packaged." -ForegroundColor Yellow
    Write-Host ""
}

