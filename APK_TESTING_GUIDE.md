# StudyWise APK Testing Guide

## Build Status: ✅ SUCCESS

Successfully built APK after fixing all compilation errors!

### APK Location
- **Latest APK**: `StudyWise-debug-20250630-180705.apk` (102MB)
- **Location**: `C:\DEV\StudyWise-Fresh\`

## Installation Instructions

### Method 1: Using ADB (Recommended)
1. Connect your Android device via USB
2. Enable Developer Options and USB Debugging on your device
3. Run the installation command:
   ```batch
   cd C:\DEV\StudyWise-Fresh
   adb install StudyWise-debug-20250630-180705.apk
   ```

### Method 2: Direct Transfer
1. Copy the APK to your device:
   - Connect device via USB
   - Transfer `StudyWise-debug-20250630-180705.apk` to your device
   - Use a file manager on your device to install
   - Enable "Install from Unknown Sources" if prompted

### Method 3: Using the Build Script
```batch
cd C:\DEV\StudyWise-Fresh
build_and_install.bat
```

## Testing Checklist

### Initial Launch Tests
- [ ] App launches without crashes
- [ ] Splash screen displays correctly
- [ ] User registration flow works
- [ ] Login functionality works
- [ ] Remember Me feature works

### Core Features to Test

#### 1. Learning Sessions
- [ ] Book text input (manual typing)
- [ ] Photo capture for text extraction
- [ ] Document upload
- [ ] Voice input
- [ ] Socratic questioning flow
- [ ] Hint system
- [ ] Progress tracking
- [ ] Session completion

#### 2. Dashboard Features
- [ ] Subject cards display
- [ ] Progress indicators
- [ ] Navigation between screens
- [ ] Streak counter
- [ ] Points display

#### 3. Analytics Dashboard
- [ ] Weekly progress charts
- [ ] Achievement badges
- [ ] Skill progression
- [ ] Performance metrics

#### 4. Settings
- [ ] Theme switching (Light/Dark/System)
- [ ] Text size adjustment
- [ ] High contrast mode
- [ ] Notification settings
- [ ] Study reminder time

#### 5. Accessibility
- [ ] Screen reader compatibility
- [ ] Touch targets (minimum 48dp)
- [ ] Color contrast
- [ ] Text scaling up to 200%

### Performance Testing
- [ ] App startup time (< 2 seconds)
- [ ] Screen transitions smooth
- [ ] No memory leaks
- [ ] Battery usage normal
- [ ] Offline functionality

### Edge Cases
- [ ] No internet connectivity
- [ ] Low memory conditions
- [ ] Screen rotation
- [ ] Background/foreground transitions
- [ ] Permission denials

## Known Issues to Watch For
1. OCR text extraction requires good lighting
2. Voice input needs quiet environment
3. First sync may take longer

## Debug Information
- Build Type: Debug
- Min SDK: 24 (Android 7.0)
- Target SDK: 35
- Kotlin Version: 2.0.21
- Compose BOM: 2024.09.00

## Next Steps After Testing
1. Report any crashes or issues
2. Note performance bottlenecks
3. Identify UI/UX improvements
4. Test on different device sizes
5. Verify offline functionality

## Crash Reporting
If the app crashes:
1. Connect device to computer
2. Run: `adb logcat > crash_log.txt`
3. Share the crash_log.txt file

---

**Build completed successfully on June 30, 2025 at 6:07 PM**