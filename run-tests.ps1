# PowerShell script to run tests with proper JAVA_HOME

# Set JAVA_HOME for this session
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Running Unit Tests..." -ForegroundColor Cyan
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Yellow

# Clean build directory
Write-Host "`nCleaning build directory..." -ForegroundColor Yellow
.\gradlew.bat clean

# Run unit tests
Write-Host "`nRunning unit tests..." -ForegroundColor Yellow
.\gradlew.bat test

# Check if tests passed
if ($LASTEXITCODE -eq 0) {
    Write-Host "`nAll tests passed successfully!" -ForegroundColor Green
    
    # Show test report location
    $reportPath = "app\build\reports\tests\test\index.html"
    if (Test-Path $reportPath) {
        Write-Host "`nTest report available at:" -ForegroundColor Cyan
        Write-Host (Resolve-Path $reportPath).Path -ForegroundColor White
    }
} else {
    Write-Host "`nTests failed! Check the test report for details." -ForegroundColor Red
    
    # Show test report location
    $reportPath = "app\build\reports\tests\test\index.html"
    if (Test-Path $reportPath) {
        Write-Host "`nTest report available at:" -ForegroundColor Yellow
        Write-Host (Resolve-Path $reportPath).Path -ForegroundColor White
    }
    
    exit 1
}