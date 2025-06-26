# StudyWise APK Build Script
# This script builds both debug and release APKs

Write-Host "StudyWise APK Build Script" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan

# Set JAVA_HOME to the known installation path
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"

# Check if JAVA_HOME is set and valid
if (-not $env:JAVA_HOME) {
    Write-Host "ERROR: JAVA_HOME is not set!" -ForegroundColor Red
    Write-Host "Please ensure Java is installed at: C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Host "ERROR: Java not found at JAVA_HOME location!" -ForegroundColor Red
    Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Yellow
    Write-Host "Please ensure Java 17 is installed correctly" -ForegroundColor Yellow
    exit 1
}

Write-Host "`nJAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green

# Clean previous builds
Write-Host "`nCleaning previous builds..." -ForegroundColor Yellow
.\gradlew.bat clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "Clean failed!" -ForegroundColor Red
    exit 1
}

# Build debug APK
Write-Host "`nBuilding Debug APK..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "Debug build failed!" -ForegroundColor Red
    exit 1
}

# Build release APK
Write-Host "`nBuilding Release APK..." -ForegroundColor Yellow
.\gradlew.bat assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "Release build failed!" -ForegroundColor Red
    exit 1
}

# Display APK locations
Write-Host "`nBuild completed successfully!" -ForegroundColor Green
Write-Host "`nAPK locations:" -ForegroundColor Cyan

$debugApk = "app\build\outputs\apk\debug\app-debug.apk"
$releaseApk = "app\build\outputs\apk\release\app-release-unsigned.apk"
$debugApkFullPath = "C:\DEV\StudyWise-Fresh\app\build\outputs\apk\debug\app-debug.apk"

if (Test-Path $debugApk) {
    $debugSize = (Get-Item $debugApk).Length / 1MB
    Write-Host "Debug APK: $debugApkFullPath ($('{0:N2}' -f $debugSize) MB)" -ForegroundColor Green
} else {
    Write-Host "Debug APK not found!" -ForegroundColor Red
}

if (Test-Path $releaseApk) {
    $releaseSize = (Get-Item $releaseApk).Length / 1MB
    Write-Host "Release APK: $releaseApk ($('{0:N2}' -f $releaseSize) MB)" -ForegroundColor Green
} else {
    Write-Host "Release APK not found!" -ForegroundColor Red
}

Write-Host "`nNote: The release APK is unsigned. You'll need to sign it before distribution." -ForegroundColor Yellow
Write-Host "To install on a device, use: adb install app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan