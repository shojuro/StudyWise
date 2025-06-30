# StudyWise V2.0 Implementation Guide

## 🎯 Overview
Version 2.0 focuses on content expansion, gamification, analytics, and polish to create a full student experience.

## 📅 Timeline: 4 Weeks (Jan 8 - Feb 4)

---

## Week 1: Content Expansion

### 1. Add 50+ Lessons Across All Subjects

#### Implementation Plan
```kotlin
// 1. Create comprehensive lesson data structure
data class ExpandedLesson(
    val id: String,
    val title: String,
    val subject: Subject,
    val gradeLevel: Int,
    val difficulty: DifficultyLevel,
    val estimatedDuration: Int, // minutes
    val tags: List<String>,
    val prerequisites: List<String>,
    val learningObjectives: List<String>,
    val contentBlocks: List<ContentBlock>,
    val assessmentQuestions: List<Question>,
    val metadata: LessonMetadata
)

enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    CHALLENGE
}

// 2. Content blocks for varied learning
sealed class ContentBlock {
    data class TextContent(val text: String, val style: TextStyle) : ContentBlock()
    data class InteractiveExample(val prompt: String, val hints: List<String>) : ContentBlock()
    data class PracticeExercise(val question: Question, val scaffolding: List<String>) : ContentBlock()
    data class Checkpoint(val questions: List<Question>, val passThreshold: Float) : ContentBlock()
}
```

#### Content Distribution (50 lessons)
- **Reading Literature**: 15 lessons
- **Reading Informational**: 10 lessons
- **Writing**: 10 lessons
- **Language/Grammar**: 10 lessons
- **Vocabulary/Speaking**: 5 lessons

### 2. Implement Lesson Search and Filtering

```kotlin
// SearchRepository implementation
class SearchRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val ftsDao: FullTextSearchDao
) : SearchRepository {
    
    override suspend fun searchLessons(
        query: String,
        filters: SearchFilters
    ): Flow<List<SearchResult>> = flow {
        val results = when {
            query.isBlank() -> lessonDao.getAllLessons()
            else -> ftsDao.searchLessons(query)
        }
        
        emit(results
            .applyFilters(filters)
            .map { it.toSearchResult() }
        )
    }
}

// Search UI Component
@Composable
fun LessonSearchScreen(
    onLessonSelected: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    
    Column {
        // Search bar with filters
        SearchBar(
            query = searchState.query,
            onQueryChange = viewModel::updateQuery,
            onSearch = viewModel::performSearch
        )
        
        // Filter chips
        LazyRow {
            items(searchState.availableFilters) { filter ->
                FilterChip(
                    selected = filter in searchState.activeFilters,
                    onClick = { viewModel.toggleFilter(filter) },
                    label = { Text(filter.label) }
                )
            }
        }
        
        // Results
        LazyColumn {
            items(searchState.results) { result ->
                LessonSearchResultCard(
                    result = result,
                    onClick = { onLessonSelected(result.lessonId) }
                )
            }
        }
    }
}
```

### 3. Add Difficulty Levels

```kotlin
// Difficulty adaptation system
class DifficultyAdapter @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend fun getRecommendedDifficulty(
        userId: String,
        skillId: String
    ): DifficultyLevel {
        val recentPerformance = progressRepository
            .getRecentPerformance(userId, skillId, limit = 10)
        
        return when (recentPerformance.averageAccuracy) {
            in 0.0..0.5 -> DifficultyLevel.BEGINNER
            in 0.5..0.7 -> DifficultyLevel.INTERMEDIATE
            in 0.7..0.9 -> DifficultyLevel.ADVANCED
            else -> DifficultyLevel.CHALLENGE
        }
    }
    
    fun adjustQuestionDifficulty(
        question: Question,
        targetDifficulty: DifficultyLevel
    ): Question {
        return when (targetDifficulty) {
            DifficultyLevel.BEGINNER -> question.copy(
                hints = question.hints + generateScaffoldingHints(question),
                complexity = 0.3f
            )
            DifficultyLevel.CHALLENGE -> question.copy(
                hints = emptyList(),
                complexity = 0.9f,
                requiresEvidence = true
            )
            else -> question
        }
    }
}
```

---

## Week 2: Gamification

### 1. Points and XP System

```kotlin
// XP calculation engine
class XPCalculator @Inject constructor() {
    
    fun calculateXP(
        baseXP: Int,
        accuracy: Float,
        timeSpent: Int,
        difficulty: DifficultyLevel,
        streakBonus: Int
    ): XPResult {
        val difficultyMultiplier = when (difficulty) {
            DifficultyLevel.BEGINNER -> 1.0f
            DifficultyLevel.INTERMEDIATE -> 1.5f
            DifficultyLevel.ADVANCED -> 2.0f
            DifficultyLevel.CHALLENGE -> 3.0f
        }
        
        val accuracyBonus = when {
            accuracy >= 1.0f -> 50 // Perfect score
            accuracy >= 0.9f -> 25
            accuracy >= 0.8f -> 10
            else -> 0
        }
        
        val speedBonus = calculateSpeedBonus(timeSpent)
        val streakMultiplier = 1.0f + (streakBonus * 0.1f)
        
        val totalXP = ((baseXP * difficultyMultiplier) + accuracyBonus + speedBonus) * streakMultiplier
        
        return XPResult(
            totalXP = totalXP.toInt(),
            breakdown = XPBreakdown(
                base = baseXP,
                difficulty = (baseXP * (difficultyMultiplier - 1)).toInt(),
                accuracy = accuracyBonus,
                speed = speedBonus,
                streak = ((totalXP / streakMultiplier) * (streakMultiplier - 1)).toInt()
            )
        )
    }
}
```

### 2. Achievement Badges (20+)

```kotlin
// Achievement definitions
enum class AchievementId {
    // Streak achievements
    FIRST_STREAK, WEEK_WARRIOR, MONTH_MASTER, STREAK_LEGEND,
    
    // Progress achievements
    FIRST_LESSON, TEN_LESSONS, HUNDRED_LESSONS, THOUSAND_LESSONS,
    
    // Mastery achievements
    FIRST_SKILL_MASTERED, SUBJECT_EXPERT, POLYMATH, GRAND_MASTER,
    
    // Speed achievements
    SPEED_READER, QUICK_THINKER, LIGHTNING_LEARNER,
    
    // Accuracy achievements
    PERFECTIONIST, CONSISTENT_PERFORMER, ACCURACY_ACE,
    
    // Special achievements
    NIGHT_OWL, EARLY_BIRD, WEEKEND_WARRIOR, COMEBACK_KID
}

// Achievement tracker
class AchievementTracker @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val eventBus: EventBus
) {
    suspend fun checkAchievements(
        userId: String,
        event: LearningEvent
    ): List<Achievement> {
        val unlockedAchievements = mutableListOf<Achievement>()
        
        // Check each achievement condition
        AchievementId.values().forEach { achievementId ->
            if (!achievementRepository.isUnlocked(userId, achievementId)) {
                val condition = getAchievementCondition(achievementId)
                if (condition.isMet(event)) {
                    val achievement = unlockAchievement(userId, achievementId)
                    unlockedAchievements.add(achievement)
                }
            }
        }
        
        // Notify UI about new achievements
        unlockedAchievements.forEach { achievement ->
            eventBus.emit(AchievementUnlockedEvent(achievement))
        }
        
        return unlockedAchievements
    }
}
```

### 3. Daily Streaks

```kotlin
// Streak tracking system
class StreakManager @Inject constructor(
    private val streakRepository: StreakRepository,
    private val notificationManager: NotificationManager
) {
    suspend fun recordActivity(userId: String): StreakUpdate {
        val today = LocalDate.now()
        val streak = streakRepository.getCurrentStreak(userId)
        
        return when {
            streak.lastActiveDate == today -> {
                // Already active today
                StreakUpdate(currentStreak = streak.count, changed = false)
            }
            streak.lastActiveDate == today.minusDays(1) -> {
                // Continuing streak
                val newStreak = streak.count + 1
                streakRepository.updateStreak(userId, newStreak, today)
                
                // Check for streak milestones
                checkStreakMilestones(userId, newStreak)
                
                StreakUpdate(
                    currentStreak = newStreak,
                    changed = true,
                    milestone = getStreakMilestone(newStreak)
                )
            }
            else -> {
                // Streak broken, start new
                streakRepository.updateStreak(userId, 1, today)
                
                // Send notification about broken streak if it was significant
                if (streak.count >= 7) {
                    notificationManager.sendStreakBrokenNotification(userId, streak.count)
                }
                
                StreakUpdate(currentStreak = 1, changed = true, streakBroken = true)
            }
        }
    }
}
```

### 4. Leaderboards

```kotlin
// Leaderboard implementation
class LeaderboardService @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    private val userRepository: UserRepository
) {
    suspend fun getLeaderboard(
        type: LeaderboardType,
        timeframe: Timeframe,
        userId: String
    ): LeaderboardData {
        val entries = when (type) {
            LeaderboardType.POINTS -> leaderboardRepository.getTopByPoints(timeframe)
            LeaderboardType.STREAK -> leaderboardRepository.getTopByStreak()
            LeaderboardType.ACCURACY -> leaderboardRepository.getTopByAccuracy(timeframe)
            LeaderboardType.LESSONS -> leaderboardRepository.getTopByLessonsCompleted(timeframe)
        }
        
        val userRank = leaderboardRepository.getUserRank(userId, type, timeframe)
        val nearbyUsers = if (userRank > 10) {
            leaderboardRepository.getUsersNearRank(userRank, type, timeframe)
        } else null
        
        return LeaderboardData(
            topEntries = entries,
            userRank = userRank,
            nearbyEntries = nearbyUsers,
            totalParticipants = leaderboardRepository.getTotalParticipants(timeframe)
        )
    }
}

// Leaderboard UI
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column {
        // Type selector
        ScrollableTabRow(selectedTabIndex = state.selectedTypeIndex) {
            LeaderboardType.values().forEach { type ->
                Tab(
                    selected = state.selectedType == type,
                    onClick = { viewModel.selectType(type) },
                    text = { Text(type.displayName) }
                )
            }
        }
        
        // Timeframe selector
        SegmentedButton(
            options = Timeframe.values().map { it.displayName },
            selectedIndex = state.selectedTimeframe.ordinal,
            onSelectionChange = { viewModel.selectTimeframe(Timeframe.values()[it]) }
        )
        
        // Leaderboard content
        LazyColumn {
            // Top 3 podium
            item {
                PodiumView(
                    first = state.leaderboard?.topEntries?.getOrNull(0),
                    second = state.leaderboard?.topEntries?.getOrNull(1),
                    third = state.leaderboard?.topEntries?.getOrNull(2)
                )
            }
            
            // Rest of top 10
            items(state.leaderboard?.topEntries?.drop(3) ?: emptyList()) { entry ->
                LeaderboardEntryCard(entry)
            }
            
            // User position if not in top 10
            state.leaderboard?.nearbyEntries?.let { nearby ->
                item {
                    Divider()
                    Text("Your Position", style = MaterialTheme.typography.titleMedium)
                }
                items(nearby) { entry ->
                    LeaderboardEntryCard(
                        entry = entry,
                        highlight = entry.userId == state.currentUserId
                    )
                }
            }
        }
    }
}
```

---

## Week 3: Analytics

### 1. Progress Charts

```kotlin
// Chart data preparation
class ChartDataProcessor @Inject constructor() {
    
    fun prepareProgressChart(
        progressData: List<DailyProgress>,
        metric: ProgressMetric
    ): LineChartData {
        val dataPoints = progressData.map { daily ->
            DataPoint(
                x = daily.date.toEpochDay().toFloat(),
                y = when (metric) {
                    ProgressMetric.ACCURACY -> daily.accuracy
                    ProgressMetric.QUESTIONS -> daily.questionsCompleted.toFloat()
                    ProgressMetric.TIME -> daily.minutesSpent.toFloat()
                    ProgressMetric.POINTS -> daily.pointsEarned.toFloat()
                }
            )
        }
        
        return LineChartData(
            datasets = listOf(
                LineDataset(
                    points = dataPoints,
                    label = metric.displayName,
                    color = metric.color,
                    smooth = true
                )
            ),
            xAxisFormatter = DateAxisFormatter(),
            yAxisFormatter = metric.formatter
        )
    }
}

// Chart component
@Composable
fun ProgressChart(
    data: LineChartData,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        drawChart(
            data = data,
            size = size,
            density = density
        )
    }
}
```

### 2. Weekly Reports

```kotlin
// Report generation
class WeeklyReportGenerator @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val analyticsService: AnalyticsService
) {
    suspend fun generateWeeklyReport(
        userId: String,
        weekStartDate: LocalDate
    ): WeeklyReport {
        val weekData = progressRepository.getWeekData(userId, weekStartDate)
        
        return WeeklyReport(
            period = WeekPeriod(weekStartDate, weekStartDate.plusDays(6)),
            summary = WeeklySummary(
                totalMinutes = weekData.sumOf { it.minutesSpent },
                totalQuestions = weekData.sumOf { it.questionsCompleted },
                averageAccuracy = weekData.map { it.accuracy }.average().toFloat(),
                daysActive = weekData.count { it.minutesSpent > 0 },
                pointsEarned = weekData.sumOf { it.pointsEarned }
            ),
            subjectBreakdown = calculateSubjectBreakdown(weekData),
            skillProgress = calculateSkillProgress(weekData),
            achievements = weekData.flatMap { it.achievementsUnlocked },
            recommendations = generateRecommendations(weekData),
            comparisonToPrevious = compareToLastWeek(userId, weekStartDate)
        )
    }
}
```

---

## Week 4: Polish

### 1. Animations and Transitions

```kotlin
// Shared element transitions
@Composable
fun AnimatedLessonCard(
    lesson: Lesson,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Card content
    }
}

// Page transitions
@Composable
fun StudyWiseNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it / 4 } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it / 4 } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
    ) {
        // Navigation graph
    }
}
```

### 2. Sound Effects

```kotlin
// Sound manager
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    
    private val sounds = mutableMapOf<SoundEffect, Int>()
    
    init {
        // Load sounds
        SoundEffect.values().forEach { effect ->
            sounds[effect] = soundPool.load(context, effect.resourceId, 1)
        }
    }
    
    fun play(effect: SoundEffect) {
        if (preferencesManager.soundEffectsEnabled) {
            sounds[effect]?.let { soundId ->
                soundPool.play(soundId, 0.7f, 0.7f, 1, 0, 1f)
            }
        }
    }
}

enum class SoundEffect(val resourceId: Int) {
    CORRECT_ANSWER(R.raw.correct),
    WRONG_ANSWER(R.raw.wrong),
    ACHIEVEMENT_UNLOCKED(R.raw.achievement),
    LEVEL_UP(R.raw.level_up),
    BUTTON_CLICK(R.raw.click)
}
```

---

## Testing Plan for V2.0

### Unit Tests
```kotlin
// Example: XP Calculator Test
class XPCalculatorTest {
    private val calculator = XPCalculator()
    
    @Test
    fun `perfect score on challenge difficulty gives maximum XP`() {
        val result = calculator.calculateXP(
            baseXP = 100,
            accuracy = 1.0f,
            timeSpent = 60,
            difficulty = DifficultyLevel.CHALLENGE,
            streakBonus = 5
        )
        
        // Base: 100 * 3 (challenge) = 300
        // Accuracy bonus: 50
        // Speed bonus: varies
        // Streak: * 1.5
        assertTrue(result.totalXP > 500)
    }
}
```

### Integration Tests
```kotlin
// Leaderboard integration test
class LeaderboardIntegrationTest {
    @Test
    fun `leaderboard updates when user completes lesson`() = runTest {
        // Given
        val userId = "test_user"
        val initialRank = leaderboardRepository.getUserRank(userId, LeaderboardType.POINTS)
        
        // When
        completeLesson(userId, points = 1000)
        
        // Then
        val newRank = leaderboardRepository.getUserRank(userId, LeaderboardType.POINTS)
        assertTrue(newRank < initialRank)
    }
}
```

## Deployment Checklist

- [ ] All features implemented and tested
- [ ] Performance metrics meet targets
- [ ] Accessibility compliance verified
- [ ] Analytics tracking in place
- [ ] Crash reporting configured
- [ ] Feature flags for gradual rollout
- [ ] A/B testing framework ready
- [ ] Documentation updated
- [ ] Release notes prepared