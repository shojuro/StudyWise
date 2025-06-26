# PowerShell script to rebuild with updated API keys

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Rebuilding StudyWise with Updated API Keys" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Check current API keys
Write-Host "`nChecking API keys in local.properties..." -ForegroundColor Yellow
$localProps = Get-Content "local.properties" -Raw

$hasValidOpenAI = $localProps -match "OPENAI_API_KEY=sk-proj-\w+"
$hasValidMistral = $localProps -match "MISTRAL_API_KEY=\w+" -and $localProps -notmatch "YOUR_MISTRAL_API_KEY_HERE"

if ($hasValidOpenAI) {
    Write-Host "✓ OpenAI API key found" -ForegroundColor Green
} else {
    Write-Host "✗ OpenAI API key missing or invalid" -ForegroundColor Red
}

if ($hasValidMistral) {
    Write-Host "✓ Mistral API key found" -ForegroundColor Green
} else {
    Write-Host "✗ Mistral API key missing or invalid" -ForegroundColor Red
}

if (-not $hasValidOpenAI -or -not $hasValidMistral) {
    Write-Host "`nPlease update your API keys in local.properties before building." -ForegroundColor Red
    exit 1
}

# Clean build directory
Write-Host "`nCleaning build directories..." -ForegroundColor Yellow
if (Test-Path "app\build\generated") {
    Remove-Item -Path "app\build\generated" -Recurse -Force -ErrorAction SilentlyContinue
}

# Clean gradle cache
Write-Host "Cleaning gradle cache..." -ForegroundColor Yellow
.\gradlew.bat clean --no-daemon

# Build debug APK
Write-Host "`nBuilding debug APK with updated API keys..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug --no-daemon --rerun-tasks

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild Successful!" -ForegroundColor Green
    
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkInfo = Get-Item $apkPath
        Write-Host "`nAPK Details:" -ForegroundColor Cyan
        Write-Host "Location: $($apkInfo.FullName)" -ForegroundColor White
        Write-Host "Size: $([math]::Round($apkInfo.Length / 1MB, 2)) MB" -ForegroundColor White
        Write-Host "Modified: $($apkInfo.LastWriteTime)" -ForegroundColor White
        
        # Copy to Downloads with timestamp
        $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
        $downloadsPath = "$env:USERPROFILE\Downloads\studywise-fixed-$timestamp.apk"
        Copy-Item $apkPath -Destination $downloadsPath -Force
        Write-Host "`nAPK copied to: $downloadsPath" -ForegroundColor Green
        
        Write-Host "`nWhat's Fixed:" -ForegroundColor Cyan
        Write-Host "✓ Grammar in manual object entry" -ForegroundColor Green
        Write-Host "✓ ML Kit confidence restored to 70%" -ForegroundColor Green
        Write-Host "✓ Common object detection improved" -ForegroundColor Green
        Write-Host "✓ Manual entry now uses AI for content" -ForegroundColor Green
        Write-Host "✓ Better error messages" -ForegroundColor Green
        Write-Host "✓ Suggestion chips for common objects" -ForegroundColor Green
    }
} else {
    Write-Host "`nBuild Failed!" -ForegroundColor Red
    Write-Host "Check the error messages above." -ForegroundColor Yellow
}