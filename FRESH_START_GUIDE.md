# StudyWise Fresh Start Guide

## Step 1: Create GitHub Repository

1. Go to https://github.com/new
2. Repository name: `StudyWise`
3. Description: "AI-powered educational app for personalized learning"
4. Make it Private (initially)
5. Don't initialize with README (we'll push from local)
6. Click "Create repository"

## Step 2: Create New Android Project

1. Open Android Studio
2. File → New → New Project
3. Select "Empty Activity" (with Compose)
4. Configure:
   - Name: StudyWise
   - Package name: com.studywise.ai
   - Save location: C:\DEV\StudyWise-Fresh
   - Language: Kotlin
   - Minimum SDK: API 24
   - Build configuration language: Kotlin DSL

## Step 3: Initial Git Setup

Open PowerShell in C:\DEV\StudyWise-Fresh:

```powershell
# Initialize git
git init

# Add all files
git add .

# Initial commit
git commit -m "Initial commit: Clean Android project setup"

# Add your GitHub remote (replace with your repo URL)
git remote add origin https://github.com/YOUR_USERNAME/StudyWise.git

# Push to GitHub
git branch -M main
git push -u origin main
```

## Step 4: Setup Core Dependencies

Create `gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.3.2"
kotlin = "1.9.22"
compose-bom = "2024.02.00"
hilt = "2.48.1"
room = "2.6.1"
retrofit = "2.9.0"
navigation = "2.7.7"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.1.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Retrofit
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

## Step 5: Project Structure

Create the following package structure:

```
app/src/main/java/com/studywise/ai/
├── StudyWiseApplication.kt
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   └── StudyWiseDatabase.kt
│   │   ├── dao/
│   │   │   └── LessonDao.kt
│   │   └── entity/
│   │       └── LessonEntity.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── StudyWiseApi.kt
│   │   └── dto/
│   │       └── LessonDto.kt
│   └── repository/
│       └── LessonRepository.kt
├── domain/
│   ├── model/
│   │   └── Lesson.kt
│   └── usecase/
│       └── GetLessonsUseCase.kt
├── presentation/
│   ├── navigation/
│   │   └── StudyWiseNavigation.kt
│   ├── theme/
│   │   └── Theme.kt
│   └── screens/
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   └── HomeViewModel.kt
│       └── lesson/
│           ├── LessonScreen.kt
│           └── LessonViewModel.kt
└── di/
    ├── AppModule.kt
    ├── DatabaseModule.kt
    └── NetworkModule.kt
```

## Step 6: Incremental Development

After each step, commit to GitHub:

```powershell
git add .
git commit -m "Add Room database setup"
git push

git add .
git commit -m "Add Hilt dependency injection"
git push

git add .
git commit -m "Add home screen with lesson list"
git push
```

## Benefits of This Approach

1. **Clean History**: Every change is tracked
2. **No Conflicts**: Start with working code
3. **Proper Structure**: Following best practices from the start
4. **Easy Collaboration**: Others can clone and contribute
5. **Rollback Safety**: Can always go back to working state

## Next Steps

1. Create the GitHub repo
2. Create new Android project in Android Studio
3. Follow the setup guide above
4. We'll build features incrementally

This approach will give you a working app much faster than fixing the current broken codebase!