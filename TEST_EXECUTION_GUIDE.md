# StudyWise Test Execution Guide

## Prerequisites
- Java 17 installed (Eclipse Adoptium recommended)
- Set JAVA_HOME environment variable
- Android SDK installed

## 1. Running Unit Tests

### Windows
```batch
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot
gradlew.bat test
```

### Linux/Mac
```bash
export JAVA_HOME=/path/to/java17
./gradlew test
```

## 2. Running Instrumented Tests

### Prerequisites
- Android device connected via USB with debugging enabled
- OR Android emulator running

### Execute Tests
```batch
gradlew.bat connectedAndroidTest
```

## 3. Generate Test Reports

### Unit Test Report
```batch
gradlew.bat test --continue
```
Reports available at: `app/build/reports/tests/testDebugUnitTest/index.html`

### Code Coverage Report
```batch
gradlew.bat createDebugCoverageReport
```
Reports available at: `app/build/reports/coverage/debug/index.html`

## 4. Test Categories to Verify

### ✅ Unit Tests (app/src/test/)
- [ ] TemplateProcessorTest
- [ ] QuestionSelectorTest
- [ ] SkillProgressionManagerTest
- [ ] TomSawyerEngineTest
- [ ] AIEvaluationServiceTest
- [ ] GamificationIntegrationServiceTest
- [ ] EnhancedLearningSessionViewModelTest
- [ ] StudentAnalyticsViewModelTest

### ✅ Integration Tests (app/src/androidTest/)
- [ ] EducationalFlowIntegrationTest
- [ ] ContentDatabaseIntegrationTest
- [ ] EnhancedLearningSessionScreenTest
- [ ] StudentAnalyticsDashboardTest

## 5. Performance Testing

### Memory Profiling
```batch
gradlew.bat connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.studywise.ai.performance.MemoryLeakTest
```

### UI Performance
Use Android Studio Profiler while running:
```batch
gradlew.bat installDebug
adb shell am start -n com.studywise.ai/.MainActivity
```

## 6. Build APK for Device Testing

### Debug Build
```batch
gradlew.bat assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build (Signed)
```batch
gradlew.bat assembleRelease
```

## 7. Testing Checklist

### Functional Testing
- [ ] User Authentication (Student, Parent, Teacher)
- [ ] Learning Session Flow
- [ ] Socratic Question System
- [ ] Progress Tracking
- [ ] Analytics Dashboard
- [ ] Export Functionality (PDF, CSV, JSON)
- [ ] Gamification Features
- [ ] Offline Mode
- [ ] Settings and Preferences

### Performance Testing
- [ ] App Launch Time < 2 seconds
- [ ] Screen Transitions < 300ms
- [ ] Memory Usage < 150MB
- [ ] No Memory Leaks
- [ ] Smooth Scrolling (60 FPS)

### Accessibility Testing
- [ ] TalkBack Navigation
- [ ] Text Scaling to 200%
- [ ] Touch Target Size (48x48 dp)
- [ ] Color Contrast Ratios
- [ ] Focus Indicators

## 8. Common Test Commands

### Run Specific Test Class
```batch
gradlew.bat test --tests "com.studywise.ai.domain.usecase.education.SkillProgressionManagerTest"
```

### Run Tests in Parallel
```batch
gradlew.bat test --parallel --max-workers=4
```

### Debug Failed Tests
```batch
gradlew.bat test --debug-jvm
```

### Clean and Rebuild
```batch
gradlew.bat clean test
```

## 9. Continuous Integration

For CI/CD pipelines, use:
```yaml
# GitHub Actions example
- name: Run Tests
  run: |
    ./gradlew test
    ./gradlew connectedCheck
    ./gradlew lint
```

## 10. Troubleshooting

### Out of Memory
Add to gradle.properties:
```
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m
```

### Test Timeout
Add to test class:
```kotlin
@Test(timeout = 5000) // 5 seconds
```

### Flaky Tests
Use retry rule:
```kotlin
@Rule
@JvmField
val retryRule = RetryRule(3)
```