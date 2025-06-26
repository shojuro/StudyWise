# Quick test compilation

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Testing compilation..." -ForegroundColor Cyan
.\gradlew.bat compileDebugKotlin --no-daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nCompilation successful!" -ForegroundColor Green
    Write-Host "Run .\rebuild-with-new-keys.ps1 to build the full APK" -ForegroundColor Yellow
} else {
    Write-Host "`nCompilation failed!" -ForegroundColor Red
}