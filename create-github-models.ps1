# Automated GitHub Model Upload Script
# This script zips models and provides instructions for GitHub upload

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Pest Detection - GitHub Model Setup" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "D:\App\Pest1"
$assetsPath = "$projectRoot\app\src\main\assets\models"
$backupPath = "$projectRoot\models-backup"

# Check if assets path exists
if (-not (Test-Path $assetsPath)) {
    Write-Host "Error: Assets path not found: $assetsPath" -ForegroundColor Red
    Write-Host "Please run this script from the project root." -ForegroundColor Yellow
    exit 1
}

# Create backup directory
Write-Host "Step 1: Creating backup directory..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path $backupPath | Out-Null
Write-Host "  ✓ Created: $backupPath" -ForegroundColor Green
Write-Host ""

# Models to zip (excluding mobilenet_v2 which stays in APK)
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

Write-Host "Step 2: Creating model zip files..." -ForegroundColor Yellow
Write-Host ""

$totalSize = 0
$successCount = 0
$failCount = 0

foreach ($model in $models) {
    $sourcePath = "$assetsPath\$model"
    $destZip = "$backupPath\$model.zip"

    Write-Host "  Processing: $model..." -ForegroundColor Cyan

    if (Test-Path $sourcePath) {
        try {
            # Remove existing zip if it exists
            if (Test-Path $destZip) {
                Remove-Item $destZip -Force
            }

            # Create zip file
            Compress-Archive -Path "$sourcePath\*" -DestinationPath $destZip -CompressionLevel Optimal -Force

            # Check result
            if (Test-Path $destZip) {
                $zipSize = (Get-Item $destZip).Length / 1MB
                $totalSize += $zipSize
                Write-Host "    ✓ Created: $model.zip ($([math]::Round($zipSize, 2)) MB)" -ForegroundColor Green
                $successCount++
            } else {
                Write-Host "    ✗ Failed to create zip file" -ForegroundColor Red
                $failCount++
            }
        } catch {
            Write-Host "    ✗ Error: $($_.Exception.Message)" -ForegroundColor Red
            $failCount++
        }
    } else {
        Write-Host "    ⚠ Not found: $sourcePath (skipping)" -ForegroundColor Yellow
        $failCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Success: $successCount models" -ForegroundColor Green
Write-Host "  Failed:  $failCount models" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host "  Total Size: $([math]::Round($totalSize, 2)) MB" -ForegroundColor Cyan
Write-Host ""

if ($successCount -gt 0) {
    Write-Host "Files created in: $backupPath" -ForegroundColor Green
    Write-Host ""
    Write-Host "Created files:" -ForegroundColor Yellow
    Get-ChildItem "$backupPath\*.zip" | ForEach-Object {
        $size = [math]::Round($_.Length / 1MB, 2)
        Write-Host "  - $($_.Name): $size MB"
    }
    Write-Host ""
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Next Steps - GitHub Upload" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Create GitHub Repository:" -ForegroundColor Yellow
Write-Host "   - Go to: https://github.com/new" -ForegroundColor White
Write-Host "   - Name: pest-detection-models" -ForegroundColor White
Write-Host "   - Visibility: PUBLIC (required for downloads)" -ForegroundColor White
Write-Host "   - Click 'Create repository'" -ForegroundColor White
Write-Host ""
Write-Host "2. Create Release:" -ForegroundColor Yellow
Write-Host "   - In your repo, click 'Releases' → 'Create a new release'" -ForegroundColor White
Write-Host "   - Tag: v1.0" -ForegroundColor White
Write-Host "   - Title: Model Files v1.0" -ForegroundColor White
Write-Host "   - Upload all zip files from: $backupPath" -ForegroundColor White
Write-Host "   - Click 'Publish release'" -ForegroundColor White
Write-Host ""
Write-Host "3. Copy Your Download URL Pattern:" -ForegroundColor Yellow
Write-Host "   https://github.com/USERNAME/pest-detection-models/releases/download/v1.0/MODEL.zip" -ForegroundColor White
Write-Host ""
Write-Host "4. Update Your App Code:" -ForegroundColor Yellow
Write-Host "   File: app\src\main\java\com\example\pest_1\data\model\ModelInfo.kt" -ForegroundColor White
Write-Host "   Line ~40: Update MODEL_BASE_URL with your GitHub URL" -ForegroundColor White
Write-Host ""
Write-Host "5. Clean Assets Folder:" -ForegroundColor Yellow
Write-Host "   Run: .\cleanup_assets.ps1" -ForegroundColor White
Write-Host "   This keeps only mobilenet_v2 in the APK" -ForegroundColor White
Write-Host ""
Write-Host "6. Build & Test:" -ForegroundColor Yellow
Write-Host "   .\gradlew clean assembleDebug" -ForegroundColor White
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "For detailed instructions, see:" -ForegroundColor Cyan
Write-Host "  GITHUB_SETUP_GUIDE.md" -ForegroundColor White
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Ask if user wants to open the backup folder
$openFolder = Read-Host "Open backup folder now? (Y/N)"
if ($openFolder -eq "Y" -or $openFolder -eq "y") {
    explorer $backupPath
}

Write-Host ""
Write-Host "✅ Zip creation complete! Follow the steps above to upload to GitHub." -ForegroundColor Green
Write-Host ""

