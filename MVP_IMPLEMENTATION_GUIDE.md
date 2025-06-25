# StudyWise MVP Implementation Guide

## 🎯 MVP Quick Start (Week 1-2)

### Day 1-2: Project Foundation

#### 1. Create Base Project Structure
```kotlin
// StudyWiseApplication.kt
@HiltAndroidApp
class StudyWiseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

#### 2. Setup Dependencies (build.gradle.kts)
```kotlin
dependencies {
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation)
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    
    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    
    // Timber
    implementation(libs.timber)
}
```

#### 3. Create Package Structure
```
✅ data/local/database/StudyWiseDatabase.kt
✅ data/local/dao/UserDao.kt, LessonDao.kt, ProgressDao.kt
✅ data/local/entity/UserEntity.kt, LessonEntity.kt, ProgressEntity.kt
✅ data/repository/AuthRepository.kt, LessonRepository.kt
✅ domain/model/User.kt, Lesson.kt, Progress.kt
✅ domain/usecase/LoginUseCase.kt, GetLessonsUseCase.kt
✅ presentation/screens/login/LoginScreen.kt, LoginViewModel.kt
✅ presentation/screens/home/HomeScreen.kt, HomeViewModel.kt
✅ presentation/navigation/StudyWiseNavigation.kt
✅ di/DatabaseModule.kt, RepositoryModule.kt
```

### Day 3-4: Database & Models

#### Room Database Setup
```kotlin
// StudyWiseDatabase.kt
@Database(
    entities = [UserEntity::class, LessonEntity::class, ProgressEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun progressDao(): ProgressDao
}

// UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val grade: Int,
    val createdAt: Date
)

// LessonEntity.kt
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val gradeLevel: Int,
    val content: String,
    val questions: String, // JSON
    val duration: Int,
    val points: Int
)
```

#### Repository Implementation
```kotlin
// AuthRepository.kt
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String, grade: Int): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val prefs: SharedPreferences
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        // For MVP, simple local validation
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null && validatePassword(password)) {
                prefs.edit().putString("current_user_id", user.id).apply()
                Result.success(user.toDomainModel())
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Day 5-6: Authentication Flow

#### Login Screen
```kotlin
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to StudyWise",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Email field
        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password field
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Login button
        Button(
            onClick = { viewModel.login() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Login")
            }
        }
        
        // Register link
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
    
    // Handle navigation
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }
}
```

### Day 7-8: Student Dashboard

#### Home Screen
```kotlin
@Composable
fun HomeScreen(
    onNavigateToLesson: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Section
        item {
            WelcomeCard(userName = uiState.userName)
        }
        
        // Subjects Grid
        item {
            Text(
                text = "Choose a Subject",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(uiState.subjects) { subject ->
            SubjectCard(
                subject = subject,
                onClick = { viewModel.onSubjectSelected(subject) }
            )
        }
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(subject.color))
            ) {
                Icon(
                    imageVector = getSubjectIcon(subject.id),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${subject.lessonCount} lessons",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null
            )
        }
    }
}
```

### Day 9-10: Learning Session

#### Lesson Screen
```kotlin
@Composable
fun LessonScreen(
    lessonId: String,
    onComplete: () -> Unit,
    viewModel: LessonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = (uiState.currentQuestionIndex + 1f) / uiState.questions.size,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        currentQuestion?.let { question ->
            // Question Text
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Answer Options
            question.options.forEachIndexed { index, option ->
                AnswerOption(
                    text = option,
                    isSelected = uiState.selectedAnswer == index,
                    isCorrect = uiState.showResult && index == question.correctAnswer,
                    isWrong = uiState.showResult && uiState.selectedAnswer == index && index != question.correctAnswer,
                    onClick = { viewModel.selectAnswer(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action Button
            Button(
                onClick = {
                    if (uiState.showResult) {
                        viewModel.nextQuestion()
                    } else {
                        viewModel.checkAnswer()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedAnswer != null
            ) {
                Text(if (uiState.showResult) "Next" else "Check Answer")
            }
        }
    }
    
    // Handle completion
    if (uiState.isCompleted) {
        CompletionDialog(
            score = uiState.score,
            totalQuestions = uiState.questions.size,
            onDismiss = onComplete
        )
    }
}
```

### Day 11-12: Progress & Polish

#### Add Sample Content
```kotlin
// SampleDataGenerator.kt
object SampleDataGenerator {
    fun generateLessons(): List<LessonEntity> {
        return listOf(
            // Math Lessons
            LessonEntity(
                id = "math_1",
                title = "Addition Basics",
                subject = "mathematics",
                gradeLevel = 3,
                content = "Learn the fundamentals of addition",
                questions = generateMathQuestions(),
                duration = 15,
                points = 100
            ),
            // Science Lessons
            LessonEntity(
                id = "science_1",
                title = "The Solar System",
                subject = "science",
                gradeLevel = 4,
                content = "Explore our solar system",
                questions = generateScienceQuestions(),
                duration = 20,
                points = 150
            ),
            // English Lessons
            LessonEntity(
                id = "english_1",
                title = "Parts of Speech",
                subject = "english",
                gradeLevel = 3,
                content = "Understanding nouns, verbs, and adjectives",
                questions = generateEnglishQuestions(),
                duration = 15,
                points = 100
            )
        )
    }
}
```

#### Navigation Setup
```kotlin
@Composable
fun StudyWiseNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                onNavigateToLesson = { lessonId ->
                    navController.navigate("lesson/$lessonId")
                }
            )
        }
        
        composable("lesson/{lessonId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            LessonScreen(
                lessonId = lessonId,
                onComplete = {
                    navController.popBackStack()
                }
            )
        }
    }
}
```

## ✅ MVP Deliverables Checklist

### Core Features
- [ ] User can register with name, email, password, grade
- [ ] User can login and logout
- [ ] Dashboard shows 3 subjects (Math, Science, English)
- [ ] Each subject has 5 sample lessons
- [ ] User can complete a lesson with questions
- [ ] Progress is saved locally
- [ ] App works completely offline

### Technical Requirements
- [ ] Clean Architecture implementation
- [ ] Hilt dependency injection
- [ ] Room database with 3 tables
- [ ] Compose UI with Material 3
- [ ] Navigation between screens
- [ ] Error handling
- [ ] Loading states

### Testing
- [ ] App launches without crashes
- [ ] All navigation flows work
- [ ] Data persists between sessions
- [ ] Works in airplane mode

## 🚀 Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run tests
./gradlew test
```

## 📝 Git Commits for MVP

```bash
# Initial setup
git commit -m "feat: initial project setup with dependencies"

# Database
git commit -m "feat(data): implement Room database with entities"
git commit -m "feat(data): add DAOs for user, lesson, and progress"

# Auth
git commit -m "feat(auth): implement login and register screens"
git commit -m "feat(auth): add authentication repository and use cases"

# Home
git commit -m "feat(home): create student dashboard with subject grid"
git commit -m "feat(home): add sample lessons and content"

# Learning
git commit -m "feat(learning): implement lesson viewer with questions"
git commit -m "feat(learning): add progress tracking and scoring"

# Polish
git commit -m "fix: handle edge cases and loading states"
git commit -m "docs: add README and setup instructions"
```

This implementation guide provides concrete code examples and a day-by-day plan to build the MVP in 2 weeks. Each component is simplified but functional, creating a solid foundation for future versions.