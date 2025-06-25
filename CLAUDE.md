# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

StudyWise is an Android educational app (K-12) with AI-powered personalization. The project uses Kotlin, Jetpack Compose, and follows MVVM with Clean Architecture.

## Core Educational Systems

### 1. Content-Agnostic System
- Works with ANY book or text the student already has
- Universal prompts: "In your story..." instead of specific book references
- Supports all text types: fiction, nonfiction, poetry, etc.

### 2. Socratic Method
- Guides students to discover answers through strategic questioning
- No direct answers - encourages critical thinking
- Builds on prior knowledge progressively

### 3. Progressive Design
- Skills advance systematically by grade (4-12 for MVP)
- Cognitive complexity follows Bloom's Taxonomy
- 80 English Language Arts skills with grade-specific prompts

## Common Commands

```bash
# Build and install
./gradlew clean assembleDebug
./gradlew installDebug

# Run tests
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests

# Code quality
./gradlew lint

# Run app on connected device
./gradlew installDebug && adb shell am start -n com.studywise.ai/.MainActivity
```

## Architecture

The codebase follows Clean Architecture with three main layers:

```
app/src/main/java/com/studywise/ai/
├── data/          # Repository implementations, database (Room), API (Retrofit)
├── domain/        # Business logic, use cases, domain models
├── presentation/  # UI (Compose), ViewModels, navigation
└── di/            # Dependency injection (Hilt)
```

## Key Technical Details

- **Minimum SDK**: 24 (Android 7.0)
- **Target/Compile SDK**: 35
- **Kotlin**: 2.0.21
- **Compose BOM**: 2024.09.00
- **Java**: 17 (Required for Google Play Store deployment - already installed)

## Accessibility Requirements (WCAG 2.1 AA)

### Must-Have Features (Phase 1)
- **4.5:1 contrast ratio** for normal text
- **3:1 contrast ratio** for large text (18pt+)
- **44×44 pixel minimum touch targets** (iOS) / 48×48 dp (Android)
- **Screen reader compatibility** (TalkBack)
- **Text scaling support** up to 200%
- **Alternative text** for all images
- **Proper focus indicators**
- **Non-color information conveyance**

### Implementation Notes
- Use Material Design 3 accessibility guidelines
- Test with TalkBack enabled
- Ensure all interactive elements are labeled
- Support keyboard navigation

## Google Play Store Requirements

### Critical Compliance Areas
1. **Security & Privacy**
   - HTTPS enforcement
   - Data encryption (AES-256)
   - COPPA compliance for users under 13
   - Privacy policy integration
   - Secure authentication

2. **Performance Standards**
   - App startup < 2 seconds
   - Crash rate < 0.1%
   - Memory efficient
   - Battery optimization

3. **Technical Requirements**
   - Android App Bundle (.aab) format
   - ProGuard/R8 configuration
   - Proper app signing
   - API Level 34+ targeting

4. **Content Policy**
   - Age-appropriate content
   - No prohibited content
   - Proper content ratings
   - Educational category compliance

## Current State and Next Steps

The project is in initial setup phase. When implementing features:

1. Follow the MVP_IMPLEMENTATION_GUIDE.md for the 2-week development plan
2. Add dependencies incrementally as needed (Hilt, Room, Retrofit, Navigation Compose)
3. Implement offline-first architecture from the start
4. Ensure COPPA compliance for users under 13
5. Maintain accessibility standards throughout development

## Important Files

- `MVP_IMPLEMENTATION_GUIDE.md` - Detailed implementation plan with code examples
- `STUDYWISE_COMPLETE_SPEC.md` - Full product specification
- `PROJECT_ROADMAP.md` - Long-term development roadmap

## Development Priorities

1. Authentication system (multi-user support: Student, Parent, Teacher)
2. Student Dashboard with subject cards
3. Learning Sessions with Socratic questioning
4. Progress tracking and analytics
5. Offline data sync
6. Accessibility features
7. Security implementation

## Testing Requirements

- Minimum 80% unit test coverage
- UI tests with accessibility checks
- Performance testing for Play Store vitals
- Security vulnerability scanning
- Cross-device compatibility testing

Always check existing patterns in the codebase before adding new features, and maintain consistency with the established architecture.