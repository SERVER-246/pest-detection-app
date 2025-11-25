param(
    [switch]$SkipBuild,
    [switch]$SkipGit
)
$ErrorActionPreference = "Stop"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " PEST DETECTION APP - DEPLOYMENT SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
$projectRoot = "D:\App\Pest1"
Set-Location $projectRoot
Write-Host "STEP 1: VERIFYING ENVIRONMENT" -ForegroundColor Yellow
Write-Host ""
try {
    $gitVersion = git --version
    Write-Host "[OK] Git installed: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Git not found" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path ".git")) {
    Write-Host "[ERROR] Not a git repository" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Git repository initialized" -ForegroundColor Green
$modelsPath = "app\src\main\assets\models"
$modelDirs = Get-ChildItem -Path $modelsPath -Directory -ErrorAction SilentlyContinue
if ($modelDirs.Count -eq 0) {
    Write-Host "[ERROR] No models found in assets" -ForegroundColor Red
    exit 1
}
if ($modelDirs.Count -eq 1 -and $modelDirs[0].Name -eq "mobilenet_v2") {
    Write-Host "[OK] Assets cleaned (mobilenet_v2 only)" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Found: $($modelDirs.Name -join ', ')" -ForegroundColor Yellow
}
Write-Host ""
if (-not $SkipBuild) {
    Write-Host "STEP 2: BUILDING RELEASE APK" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Cleaning..." -ForegroundColor Cyan
    & .\gradlew.bat clean | Out-Null
    Write-Host "Building APK (5-10 minutes)..." -ForegroundColor Cyan
    & .\gradlew.bat assembleRelease
    if (-not $?) {
        Write-Host "[ERROR] Build failed" -ForegroundColor Red
        exit 1
    }
    $apkPath = "app\build\outputs\apk\release\app-release-unsigned.apk"
    if (-not (Test-Path $apkPath)) {
        $apkPath = "app\build\outputs\apk\release\app-release.apk"
    }
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length
        $apkSizeMB = [math]::Round($apkSize / 1MB, 2)
        Write-Host "[OK] APK built: $apkSizeMB MB" -ForegroundColor Green
        Write-Host "    Location: $apkPath" -ForegroundColor Gray
        if ($apkSizeMB -gt 100) {
            Write-Host "[WARNING] APK larger than expected" -ForegroundColor Yellow
        }
    } else {
        Write-Host "[ERROR] APK not found" -ForegroundColor Red
        exit 1
    }
    Write-Host ""
} else {
    Write-Host "[SKIP] Skipping build" -ForegroundColor Gray
    Write-Host ""
}
if (-not $SkipGit) {
    Write-Host "STEP 3: GIT OPERATIONS" -ForegroundColor Yellow
    Write-Host ""
    $status = git status --porcelain
    if ($status) {
        Write-Host "Changes detected:" -ForegroundColor Cyan
        git status -s
        Write-Host ""
        $confirm = Read-Host "Commit changes? (yes/no)"
        if ($confirm -eq "yes") {
            git add .
            $commitMsg = Read-Host "Commit message (or Enter for default)"
            if ([string]::IsNullOrWhiteSpace($commitMsg)) {
                $commitMsg = "Release v1.0.0 - Optimized APK"
            }
            git commit -m $commitMsg
            Write-Host "[OK] Committed" -ForegroundColor Green
        }
    } else {
        Write-Host "[OK] No changes to commit" -ForegroundColor Green
    }
    Write-Host ""
    try {
        $remote = git remote get-url origin 2>$null
        if ($remote) {
            Write-Host "[OK] Remote: $remote" -ForegroundColor Green
            $push = Read-Host "Push to GitHub? (yes/no)"
            if ($push -eq "yes") {
                git push
                if ($?) {
                    Write-Host "[OK] Pushed successfully" -ForegroundColor Green
                }
            }
        }
    } catch {
        Write-Host "[WARNING] No remote configured" -ForegroundColor Yellow
    }
    Write-Host ""
}
Write-Host "========================================" -ForegroundColor Green
Write-Host " DEPLOYMENT COMPLETE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "NEXT STEPS:" -ForegroundColor Yellow
Write-Host "1. Test APK on device: adb install app\build\outputs\apk\release\app-release.apk" -ForegroundColor White
Write-Host "2. Create GitHub release at: https://github.com/SERVER-246/pest-detection-app/releases/new" -ForegroundColor White
Write-Host "3. Upload app-release.apk to the release" -ForegroundColor White
Write-Host ""
