# PowerShell script to compile a single test file

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Compiling HybridAIRepositoryTest..." -ForegroundColor Cyan

# Just compile the test task
.\gradlew.bat :app:testDebugUnitTest --tests "com.studywise.ai.data.repository.HybridAIRepositoryTest" --info

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nTest compiled and ran successfully!" -ForegroundColor Green
} else {
    Write-Host "`nTest compilation/run failed!" -ForegroundColor Red
}