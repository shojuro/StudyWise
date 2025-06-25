# PowerShell script to set up clean StudyWise project structure
param(
    [string]$ProjectPath = "C:\DEV\StudyWise-Fresh"
)

Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "StudyWise Clean Project Setup" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# Check if Android Studio created the project
if (-not (Test-Path $ProjectPath)) {
    Write-Host "Please create the project in Android Studio first!" -ForegroundColor Red
    Write-Host "Location: $ProjectPath" -ForegroundColor Red
    exit
}

Set-Location $ProjectPath

# Create package structure
Write-Host "Creating package structure..." -ForegroundColor Yellow

$basePackage = "app\src\main\java\com\studywise\ai"

# Create directories
$directories = @(
    "$basePackage\data\local\database",
    "$basePackage\data\local\dao", 
    "$basePackage\data\local\entity",
    "$basePackage\data\remote\api",
    "$basePackage\data\remote\dto",
    "$basePackage\data\repository",
    "$basePackage\domain\model",
    "$basePackage\domain\usecase",
    "$basePackage\presentation\navigation",
    "$basePackage\presentation\theme",
    "$basePackage\presentation\screens\home",
    "$basePackage\presentation\screens\lesson",
    "$basePackage\di"
)

foreach ($dir in $directories) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

Write-Host "Package structure created!" -ForegroundColor Green

# Initialize Git
Write-Host ""
Write-Host "Initializing Git repository..." -ForegroundColor Yellow
git init

# Create .gitignore if it doesn't exist
if (-not (Test-Path ".gitignore")) {
    @"
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
"@ | Out-File -FilePath ".gitignore" -Encoding UTF8
}

# Initial commit
git add .
git commit -m "Initial commit: Clean Android project structure"

Write-Host "Git repository initialized!" -ForegroundColor Green

# Create README
@"
# StudyWise

AI-powered educational app for personalized learning.

## Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run the app

## Architecture

- **MVVM** with Clean Architecture
- **Jetpack Compose** for UI
- **Hilt** for dependency injection
- **Room** for local database
- **Retrofit** for networking
- **Coroutines & Flow** for async operations

## Package Structure

```
com.studywise.ai/
├── data/           # Data layer
├── domain/         # Business logic
├── presentation/   # UI layer
└── di/            # Dependency injection
```
"@ | Out-File -FilePath "README.md" -Encoding UTF8

git add README.md
git commit -m "Add README"

Write-Host ""
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Add GitHub remote:" -ForegroundColor White
Write-Host "   git remote add origin https://github.com/YOUR_USERNAME/StudyWise.git" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Push to GitHub:" -ForegroundColor White
Write-Host "   git branch -M main" -ForegroundColor Gray
Write-Host "   git push -u origin main" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Open in Android Studio and start coding!" -ForegroundColor White

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")