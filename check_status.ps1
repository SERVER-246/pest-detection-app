# Quick check of current state
Write-Host "Checking current project state..." -ForegroundColor Cyan

# Check APK sizes
$apkPath = "D:\App\Pest1\app\build\outputs\apk\debug\app-universal-debug.apk"
if (Test-Path $apkPath) {
    $size = (Get-Item $apkPath).Length / 1MB
    Write-Host "Current APK size: $([math]::Round($size, 2)) MB" -ForegroundColor Yellow
} else {
    Write-Host "No APK found yet" -ForegroundColor Red
}

# Check models in assets
$assetsPath = "D:\App\Pest1\app\src\main\assets\models"
$models = Get-ChildItem $assetsPath -Directory
Write-Host "`nModels in assets: $($models.Count)" -ForegroundColor Yellow
$models | ForEach-Object { Write-Host "  - $($_.Name)" }

Write-Host "`n==> Need to remove 10 models to reduce APK size!" -ForegroundColor Red

