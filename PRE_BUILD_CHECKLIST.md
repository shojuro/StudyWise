# Pre-Build Checklist

## Before Building the APK

### 1. API Key Setup (Optional for Testing)
- [ ] If you want to test ChatGPT-4 features, add your OpenAI API key to `local.properties`:
  ```
  OPENAI_API_KEY=sk-your-api-key-here
  ```
- [ ] If no API key is provided, the app will use offline fallbacks

### 2. Build Requirements
- [ ] Java 17 installed (Eclipse Adoptium OpenJDK)
- [ ] Android SDK installed
- [ ] JAVA_HOME environment variable set (or will be set by build script)

### 3. Features to Test After Build

#### Core Features
- [ ] Demo Mode login (quick access without registration)
- [ ] Full registration flow with all screens
- [ ] Student dashboard navigation
- [ ] Learning session with text input
- [ ] Profile screen with statistics
- [ ] Settings screen with theme switching

#### Multi-Input Features
- [ ] Photo capture and analysis
- [ ] Photo selection from gallery
- [ ] Voice input (requires RECORD_AUDIO permission)
- [ ] Document text extraction (using OCR)

#### ChatGPT-4 Features (if API key provided)
- [ ] Object identification from photos
- [ ] Grade-appropriate sentence generation
- [ ] 5-minute Socratic lessons
- [ ] Voice-to-text transcription
- [ ] Text-to-speech responses

#### Offline Features (when no API key or no internet)
- [ ] Offline object descriptions
- [ ] Offline sentence examples
- [ ] Offline Socratic questions
- [ ] Offline conversation responses

## Build Commands

### Windows Command Prompt:
```cmd
build-apk.bat
```

### Or manually:
```cmd
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
gradlew.bat clean assembleDebug
```

## Post-Build

### APK Location:
```
app\build\outputs\apk\debug\app-debug.apk
```

### Install on Device:
```cmd
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Permissions to Grant:
- Camera (for photo learning)
- Microphone (for voice input)
- Storage (for document selection) - only on older Android versions

## Known Issues
- Voice input requires a real device (not emulator)
- Camera capture works best on real devices
- Some ML Kit features may be slower on older devices