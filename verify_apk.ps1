# ============================================
# APK VERIFICATION & STATUS CHECK
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  CHECKING APK BUILD STATUS" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$projectRoot = "D:\App\Pest1"
cd $projectRoot

# Check for Universal APK (preferred - works on all devices)
$universalApk = "app\build\outputs\apk\universal\release\app-universal-release-unsigned.apk"

# Check for regular release APK (fallback)
$releaseApk = "app\build\outputs\apk\release\app-release-unsigned.apk"

# Check for signed APK
$signedApk = "app\build\outputs\apk\release\app-release.apk"

Write-Host "Searching for APK files..." -ForegroundColor Yellow
Write-Host ""

$foundApk = $null
$apkType = ""

if (Test-Path $universalApk) {
    $foundApk = Get-Item $universalApk
    $apkType = "Universal (All Architectures)"
    Write-Host "[FOUND] Universal APK" -ForegroundColor Green
} elseif (Test-Path $releaseApk) {
    $foundApk = Get-Item $releaseApk
    $apkType = "Release (Unsigned)"
    Write-Host "[FOUND] Release APK" -ForegroundColor Green
} elseif (Test-Path $signedApk) {
    $foundApk = Get-Item $signedApk
    $apkType = "Release (Signed)"
    Write-Host "[FOUND] Signed APK" -ForegroundColor Green
} else {
    Write-Host "[ERROR] No APK found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Possible locations checked:" -ForegroundColor Yellow
    Write-Host "  - $universalApk" -ForegroundColor Gray
    Write-Host "  - $releaseApk" -ForegroundColor Gray
    Write-Host "  - $signedApk" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Build may have failed. Check build logs." -ForegroundColor Red
    Write-Host ""
    Write-Host "To rebuild:" -ForegroundColor Yellow
    Write-Host "  cd D:\App\Pest1" -ForegroundColor Gray
    Write-Host '  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"' -ForegroundColor Gray
    Write-Host "  .\gradlew.bat clean assembleRelease" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  APK DETAILS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Type:     $apkType" -ForegroundColor Cyan
Write-Host "Location: $($foundApk.FullName)" -ForegroundColor Gray
Write-Host "Size:     $([math]::Round($foundApk.Length / 1MB, 2)) MB" -ForegroundColor Cyan
Write-Host "Created:  $($foundApk.LastWriteTime)" -ForegroundColor Gray
Write-Host ""

$sizeMB = [math]::Round($foundApk.Length / 1MB, 2)

if ($sizeMB -lt 100) {
    Write-Host "[SUCCESS] APK size is optimal!" -ForegroundColor Green
    Write-Host "The APK is small enough to install on all devices." -ForegroundColor Green
} else {
    Write-Host "[WARNING] APK is larger than expected (>100 MB)" -ForegroundColor Yellow
    Write-Host "This may cause installation issues on some devices." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  NEXT STEPS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. TEST ON DEVICE (Optional but recommended):" -ForegroundColor Yellow
Write-Host "   adb install `"$($foundApk.FullName)`"" -ForegroundColor Gray
Write-Host ""
Write-Host "2. COMMIT TO GIT:" -ForegroundColor Yellow
Write-Host "   git add ." -ForegroundColor Gray
Write-Host '   git commit -m "Release v1.0.0 - Production APK"' -ForegroundColor Gray
Write-Host "   git push origin main" -ForegroundColor Gray
Write-Host ""
Write-Host "3. CREATE GITHUB RELEASE:" -ForegroundColor Yellow
Write-Host "   https://github.com/SERVER-246/pest-detection-app/releases/new" -ForegroundColor Gray
Write-Host "   - Tag: v1.0.0" -ForegroundColor Gray
Write-Host "   - Upload: $($foundApk.Name)" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================`n" -ForegroundColor Cyan

