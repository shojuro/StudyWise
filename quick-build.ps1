# PowerShell script for quick build without clean

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "StudyWise Quick Build (No Clean)" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

Write-Host "`nBuilding Debug APK..." -ForegroundColor Yellow
Write-Host "(Skipping clean step to avoid file lock issues)" -ForegroundColor Gray

# Build without cleaning
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
    Write-Host "Try running .\force-clean-build.ps1 to force clean the build directory." -ForegroundColor Yellow
}