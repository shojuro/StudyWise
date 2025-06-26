# PowerShell script to force clean and build

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "StudyWise Force Clean & Build" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan

# Kill any Java processes that might be holding locks
Write-Host "`nStopping any Java/Gradle processes..." -ForegroundColor Yellow
Get-Process | Where-Object {$_.ProcessName -like "*java*" -or $_.ProcessName -like "*gradle*"} | Stop-Process -Force -ErrorAction SilentlyContinue

# Wait a moment
Start-Sleep -Seconds 2

# Try to delete build directory manually
Write-Host "`nRemoving build directories..." -ForegroundColor Yellow
$buildDir = "app\build"
if (Test-Path $buildDir) {
    try {
        Remove-Item -Path $buildDir -Recurse -Force -ErrorAction Stop
        Write-Host "✓ Build directory removed" -ForegroundColor Green
    } catch {
        Write-Host "⚠ Could not fully remove build directory, but continuing..." -ForegroundColor Yellow
        # Try to at least remove the problematic lint-cache
        $lintCache = "app\build\intermediates\lint-cache"
        if (Test-Path $lintCache) {
            Remove-Item -Path $lintCache -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

# Also clean .gradle directory
$gradleCache = ".gradle\buildOutputCleanup"
if (Test-Path $gradleCache) {
    Remove-Item -Path $gradleCache -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "`nBuilding Debug APK..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug --no-daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild Successful!" -ForegroundColor Green
    
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkInfo = Get-Item $apkPath
        Write-Host "`nAPK Details:" -ForegroundColor Cyan
        Write-Host "Location: $($apkInfo.FullName)" -ForegroundColor White
        Write-Host "Size: $([math]::Round($apkInfo.Length / 1MB, 2)) MB" -ForegroundColor White
        Write-Host "Created: $($apkInfo.LastWriteTime)" -ForegroundColor White
        
        # Copy to Downloads
        $downloadsPath = "$env:USERPROFILE\Downloads\studywise-debug.apk"
        Copy-Item $apkPath -Destination $downloadsPath -Force
        Write-Host "`nAPK copied to: $downloadsPath" -ForegroundColor Green
    }
} else {
    Write-Host "`nBuild Failed!" -ForegroundColor Red
    Write-Host "Try closing Android Studio and any other programs that might be using the build directory." -ForegroundColor Yellow
}