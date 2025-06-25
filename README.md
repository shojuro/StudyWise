# StudyWise

An AI-powered educational Android app for K-12 students using the Socratic method and content-agnostic learning approach.

## Features

- 🎓 **Multi-User Support**: Student, Parent, and Teacher accounts
- 📚 **Content-Agnostic Learning**: Works with any book or text the student has
- 🤔 **Socratic Method**: Guides students to discover answers through strategic questioning
- 📈 **Progressive Learning**: Skills advance systematically by grade level
- ♿ **Fully Accessible**: WCAG 2.1 AA compliant with screen reader support
- 🔒 **Privacy First**: COPPA compliant, secure data handling
- 📱 **Offline First**: Full functionality without internet connection

## Technical Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room
- **DI**: Hilt
- **Async**: Coroutines & Flow
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35
- **Java**: 17

## Project Status

MVP implementation includes:
- ✅ Multi-user authentication system
- ✅ Accessible UI components
- ✅ Database schema for skills and progress tracking
- ✅ Socratic question engine
- ✅ Student dashboard
- ✅ Navigation structure
- ✅ Security configuration (ProGuard)
- 🚧 Learning session implementation
- 🚧 Progress tracking views
- 🚧 Offline sync
- 🚧 Testing suite

## Building the Project

```bash
# Debug build
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on device
./gradlew installDebug
```

## Package Structure

```
com.studywise.ai/
├── data/           # Data layer (Repository implementations, Room, API)
├── domain/         # Business logic (Use cases, models, repository interfaces)
├── presentation/   # UI layer (Screens, ViewModels, Composables)
└── di/            # Dependency injection modules
```

## Privacy

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for our privacy policy.

## License

Copyright © 2024 StudyWise. All rights reserved.