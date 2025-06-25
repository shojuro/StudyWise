# Building StudyWise APK

## Prerequisites
- Java 17 installed and JAVA_HOME set
- Android SDK installed
- Android Studio (recommended) or command line tools

## Building Debug APK

### Option 1: Using Command Line
```bash
# On Windows
gradlew.bat assembleDebug

# On Mac/Linux
./gradlew assembleDebug
```

The debug APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Using Android Studio
1. Open the project in Android Studio
2. Select `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
3. Click "locate" when the build completes to find the APK

## Building Release APK

### Step 1: Create a Keystore (First time only)
```bash
keytool -genkey -v -keystore studywise-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias studywise
```

### Step 2: Add Signing Configuration
Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../studywise-release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your-password"
            keyAlias = "studywise"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "your-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // existing configuration...
        }
    }
}
```

### Step 3: Build Release APK
```bash
# On Windows
gradlew.bat assembleRelease

# On Mac/Linux
./gradlew assembleRelease
```

The release APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

## Installing on Device

### Enable Developer Options on Android Device
1. Go to Settings → About Phone
2. Tap "Build Number" 7 times
3. Go back to Settings → Developer Options
4. Enable "USB Debugging"
5. Enable "Install via USB" (if present)

### Install Using ADB
```bash
# Connect device via USB
adb devices

# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Install release APK
adb install app/build/outputs/apk/release/app-release.apk
```

### Install Manually
1. Copy the APK to your device
2. On device: Settings → Security → Enable "Unknown Sources"
3. Use a file manager to locate and tap the APK
4. Follow installation prompts

## Troubleshooting

### JAVA_HOME not set
- Windows: Set JAVA_HOME to your JDK 17 path (e.g., `C:\Program Files\Java\jdk-17`)
- Mac/Linux: Add to ~/.bashrc or ~/.zshrc: `export JAVA_HOME=/path/to/jdk17`

### Build fails with "SDK location not found"
Create `local.properties` file in project root:
```
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### APK not installing
- Check minimum Android version (7.0/API 24)
- Ensure "Unknown Sources" is enabled
- Uninstall any previous versions first

## APK Details
- **Package Name**: com.studywise.ai
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35
- **Architecture**: Universal (all ABIs)
- **Size**: ~10-15MB (debug), ~5-8MB (release with ProGuard)

## Testing Checklist
- [ ] Login/Register flow works
- [ ] Dashboard loads with subject cards
- [ ] Navigation between screens
- [ ] Theme switching (Light/Dark)
- [ ] Text scaling
- [ ] Screen reader compatibility
- [ ] Offline functionality