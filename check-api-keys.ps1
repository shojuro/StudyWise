# PowerShell script to check API key configuration

Write-Host "Checking API Key Configuration" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

# Check local.properties
Write-Host "`nChecking local.properties..." -ForegroundColor Yellow
$localProps = Get-Content "local.properties" -Raw

if ($localProps -match "OPENAI_API_KEY=(.+)") {
    $openaiKey = $matches[1].Trim()
    if ($openaiKey -like "sk-proj-*") {
        Write-Host "✓ OpenAI key found: $($openaiKey.Substring(0, 10))..." -ForegroundColor Green
    } else {
        Write-Host "✗ OpenAI key format incorrect" -ForegroundColor Red
    }
} else {
    Write-Host "✗ OpenAI key not found" -ForegroundColor Red
}

if ($localProps -match "MISTRAL_API_KEY=(.+)") {
    $mistralKey = $matches[1].Trim()
    if ($mistralKey -ne "YOUR_MISTRAL_API_KEY_HERE") {
        Write-Host "✓ Mistral key found: $($mistralKey.Substring(0, 10))..." -ForegroundColor Green
    } else {
        Write-Host "✗ Mistral key is placeholder" -ForegroundColor Red
    }
} else {
    Write-Host "✗ Mistral key not found" -ForegroundColor Red
}

# Check if BuildConfig is generated correctly
Write-Host "`nChecking BuildConfig generation..." -ForegroundColor Yellow
$buildConfigPath = "app\build\generated\source\buildConfig\debug\com\studywise\ai\BuildConfig.java"

if (Test-Path $buildConfigPath) {
    $buildConfig = Get-Content $buildConfigPath -Raw
    
    if ($buildConfig -match 'OPENAI_API_KEY = "([^"]+)"') {
        $configOpenAI = $matches[1]
        if ($configOpenAI -like "sk-proj-*") {
            Write-Host "✓ OpenAI key in BuildConfig: $($configOpenAI.Substring(0, 10))..." -ForegroundColor Green
        } else {
            Write-Host "✗ OpenAI key in BuildConfig is incorrect" -ForegroundColor Red
        }
    }
    
    if ($buildConfig -match 'MISTRAL_API_KEY = "([^"]+)"') {
        $configMistral = $matches[1]
        if ($configMistral -ne "YOUR_MISTRAL_API_KEY_HERE") {
            Write-Host "✓ Mistral key in BuildConfig: $($configMistral.Substring(0, 10))..." -ForegroundColor Green
        } else {
            Write-Host "✗ Mistral key in BuildConfig is placeholder" -ForegroundColor Red
        }
    }
} else {
    Write-Host "⚠ BuildConfig not found. Run a build first." -ForegroundColor Yellow
}

Write-Host "`nRecommendations:" -ForegroundColor Cyan
Write-Host "1. Make sure local.properties has the correct API keys" -ForegroundColor White
Write-Host "2. Run './gradlew clean' to clear old build files" -ForegroundColor White
Write-Host "3. Rebuild the app to regenerate BuildConfig" -ForegroundColor White
Write-Host "4. Check logcat for API authentication errors" -ForegroundColor White