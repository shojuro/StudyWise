# PowerShell script to clean build with API key verification

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Clean Build with API Key Verification" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# First check API keys
Write-Host "`nChecking API keys in local.properties..." -ForegroundColor Yellow
.\check-api-keys.ps1

# Ask to continue
Write-Host "`nDo you want to continue with the build? (Y/N)" -ForegroundColor Yellow
$continue = Read-Host
if ($continue -ne 'Y' -and $continue -ne 'y') {
    Write-Host "Build cancelled." -ForegroundColor Red
    exit
}

# Clean build directory
Write-Host "`nCleaning build directories..." -ForegroundColor Yellow
if (Test-Path "app\build") {
    Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue
}
if (Test-Path ".gradle") {
    Remove-Item -Path ".gradle\buildOutputCleanup" -Recurse -Force -ErrorAction SilentlyContinue
}

# Clean with gradle
Write-Host "`nRunning gradle clean..." -ForegroundColor Yellow
.\gradlew.bat clean --no-daemon

# Build debug APK
Write-Host "`nBuilding debug APK with fresh BuildConfig..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug --no-daemon --rerun-tasks

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild Successful!" -ForegroundColor Green
    
    # Check BuildConfig again
    Write-Host "`nVerifying BuildConfig generation..." -ForegroundColor Yellow
    $buildConfigPath = "app\build\generated\source\buildConfig\debug\com\studywise\ai\BuildConfig.java"
    if (Test-Path $buildConfigPath) {
        $content = Get-Content $buildConfigPath | Select-String -Pattern "API_KEY"
        $content | ForEach-Object {
            Write-Host $_ -ForegroundColor Gray
        }
    }
    
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkInfo = Get-Item $apkPath
        Write-Host "`nAPK Details:" -ForegroundColor Cyan
        Write-Host "Location: $($apkInfo.FullName)" -ForegroundColor White
        Write-Host "Size: $([math]::Round($apkInfo.Length / 1MB, 2)) MB" -ForegroundColor White
        
        # Copy to Downloads
        $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
        $downloadsPath = "$env:USERPROFILE\Downloads\studywise-debug-$timestamp.apk"
        Copy-Item $apkPath -Destination $downloadsPath -Force
        Write-Host "`nAPK copied to: $downloadsPath" -ForegroundColor Green
    }
} else {
    Write-Host "`nBuild Failed!" -ForegroundColor Red
    Write-Host "Check the error messages above for details." -ForegroundColor Yellow
}