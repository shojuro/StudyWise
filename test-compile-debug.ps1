# PowerShell script to compile tests with detailed output

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Compiling Unit Tests with Detailed Output..." -ForegroundColor Cyan
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Yellow

# Compile debug unit tests with detailed output
Write-Host "`nCompiling debug unit tests..." -ForegroundColor Yellow
.\gradlew.bat compileDebugUnitTestKotlin --stacktrace

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nCompilation failed! Checking for common issues..." -ForegroundColor Red
    
    # List test files
    Write-Host "`nTest files:" -ForegroundColor Yellow
    Get-ChildItem -Path "app\src\test" -Recurse -Filter "*.kt" | ForEach-Object {
        Write-Host $_.FullName -ForegroundColor Gray
    }
}