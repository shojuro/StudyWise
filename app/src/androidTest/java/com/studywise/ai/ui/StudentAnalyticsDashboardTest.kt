package com.studywise.ai.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studywise.ai.domain.gamification.Achievement
import com.studywise.ai.domain.gamification.AchievementRarity
import com.studywise.ai.presentation.screens.analytics.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * UI tests for StudentAnalyticsDashboard
 */
@RunWith(AndroidJUnit4::class)
class StudentAnalyticsDashboardTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testLoadingState() {
        // When
        composeTestRule.setContent {
            LoadingState()
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Loading analytics...").assertExists()
            // Progress indicator should be present
            waitForIdle()
        }
    }
    
    @Test
    fun testErrorState() {
        // When
        composeTestRule.setContent {
            ErrorState(
                message = "Failed to load analytics",
                onRetry = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Failed to load analytics").assertExists()
            onNodeWithText("Retry").assertExists()
        }
    }
    
    @Test
    fun testOverallProgressCard() {
        // Given
        val progress = OverallProgress(
            completionRate = 0.75f,
            skillsMastered = 12,
            currentStreak = 7,
            totalPoints = 1250
        )
        
        // When
        composeTestRule.setContent {
            OverallProgressCard(progress)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Overall Progress").assertExists()
            onNodeWithText("75").assertExists() // Completion percentage
            onNodeWithText("Complete").assertExists()
            
            // Progress stats
            onNodeWithText("Skills Mastered").assertExists()
            onNodeWithText("12").assertExists()
            
            onNodeWithText("Current Streak").assertExists()
            onNodeWithText("7 days").assertExists()
            
            onNodeWithText("Total Points").assertExists()
            onNodeWithText("1250").assertExists()
        }
    }
    
    @Test
    fun testPerformanceMetricsGrid() {
        // Given
        val metrics = PerformanceMetrics(
            accuracyRate = 0.82f,
            accuracyTrend = 0.05f,
            avgResponseTime = 32,
            responseTimeTrend = -0.1f,
            questionsPerDay = 15,
            questionsPerDayTrend = 0.2f,
            improvementRate = 0.15f,
            improvementTrend = 0.08f
        )
        
        // When
        composeTestRule.setContent {
            PerformanceMetricsGrid(metrics)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Accuracy Rate").assertExists()
            onNodeWithText("82%").assertExists()
            
            onNodeWithText("Avg Response Time").assertExists()
            onNodeWithText("32s").assertExists()
            
            onNodeWithText("Questions/Day").assertExists()
            onNodeWithText("15").assertExists()
            
            onNodeWithText("Improvement Rate").assertExists()
            onNodeWithText("+15%").assertExists()
        }
    }
    
    @Test
    fun testSkillMasteryChart() {
        // Given
        val skillMastery = listOf(
            SkillMasteryData(
                skillName = "Character Analysis",
                skillCode = "RL.4.2",
                mastery = 0.85f,
                questionsAnswered = 20,
                correctAnswers = 17,
                recentImprovement = 0.1f
            ),
            SkillMasteryData(
                skillName = "Theme Identification",
                skillCode = "RL.4.3",
                mastery = 0.65f,
                questionsAnswered = 15,
                correctAnswers = 10,
                recentImprovement = 0.05f
            )
        )
        
        // When
        composeTestRule.setContent {
            SkillMasteryChart(skillMastery)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Skill Mastery Progress").assertExists()
            
            // First skill
            onNodeWithText("Character Analysis").assertExists()
            onNodeWithText("85%").assertExists()
            onNodeWithText("+10% this week").assertExists()
            
            // Second skill
            onNodeWithText("Theme Identification").assertExists()
            onNodeWithText("65%").assertExists()
            onNodeWithText("+5% this week").assertExists()
        }
    }
    
    @Test
    fun testLearningPatternsCard() {
        // Given
        val patterns = LearningPatterns(
            bestLearningTime = "3:00 PM - 5:00 PM",
            averageSessionDuration = 25,
            preferredDifficulty = "Medium",
            learningStyles = listOf(
                LearningStylePreference("Visual", 0.7f),
                LearningStylePreference("Interactive", 0.6f),
                LearningStylePreference("Reading", 0.4f)
            ),
            focusAreas = listOf("Character Analysis", "Theme Identification")
        )
        
        // When
        composeTestRule.setContent {
            LearningPatternsCard(patterns)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Learning Patterns").assertExists()
            onNodeWithText("Peak Performance Time").assertExists()
            onNodeWithText("3:00 PM - 5:00 PM").assertExists()
            
            onNodeWithText("Learning Style Preferences").assertExists()
            onNodeWithText("Visual").assertExists()
            onNodeWithText("70%").assertExists()
            onNodeWithText("Interactive").assertExists()
            onNodeWithText("60%").assertExists()
            onNodeWithText("Reading").assertExists()
            onNodeWithText("40%").assertExists()
        }
    }
    
    @Test
    fun testSubjectPerformanceCard() {
        // Given
        val subjects = listOf(
            SubjectPerformance(
                name = "Reading",
                progress = 0.75f,
                questionsCompleted = 150,
                accuracy = 0.82f,
                icon = Icons.Default.Book,
                color = Color(0xFF4CAF50)
            ),
            SubjectPerformance(
                name = "Writing",
                progress = 0.60f,
                questionsCompleted = 120,
                accuracy = 0.78f,
                icon = Icons.Default.Create,
                color = Color(0xFF2196F3)
            )
        )
        
        // When
        composeTestRule.setContent {
            SubjectPerformanceCard(subjects)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Subject Performance").assertExists()
            onNodeWithText("Reading").assertExists()
            onNodeWithText("75%").assertExists()
            onNodeWithText("Writing").assertExists()
            onNodeWithText("60%").assertExists()
        }
    }
    
    @Test
    fun testAchievementsShowcase() {
        // Given
        val achievements = listOf(
            Achievement(
                id = "speed_reader",
                name = "Speed Reader",
                description = "Read 10 passages quickly",
                iconUrl = "icon_speed",
                rarity = AchievementRarity.RARE,
                unlockedAt = Date()
            ),
            Achievement(
                id = "streak_master",
                name = "Streak Master",
                description = "7-day streak",
                iconUrl = "icon_streak",
                rarity = AchievementRarity.EPIC,
                unlockedAt = Date()
            )
        )
        
        // When
        composeTestRule.setContent {
            AchievementsShowcase(achievements)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Recent Achievements").assertExists()
            onNodeWithText("View All").assertExists()
            onNodeWithText("Speed Reader").assertExists()
            onNodeWithText("Streak Master").assertExists()
        }
    }
    
    @Test
    fun testStudyTimeAnalysis() {
        // Given
        val studyData = StudyTimeData(
            totalMinutesThisWeek = 350,
            dailyAverage = 50,
            mostProductiveDay = "Saturday",
            weeklyData = listOf(
                DayStudyData("Monday", 45),
                DayStudyData("Tuesday", 60),
                DayStudyData("Wednesday", 30),
                DayStudyData("Thursday", 55),
                DayStudyData("Friday", 40),
                DayStudyData("Saturday", 75),
                DayStudyData("Sunday", 45)
            ),
            monthlyTrend = 0.15f
        )
        
        // When
        composeTestRule.setContent {
            StudyTimeAnalysis(studyData)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Study Time Analysis").assertExists()
            onNodeWithText("This Week's Activity").assertExists()
            
            // Weekly stats
            onNodeWithText("Total This Week").assertExists()
            onNodeWithText("5h 50m").assertExists()
            
            onNodeWithText("Daily Average").assertExists()
            onNodeWithText("50m").assertExists()
            
            onNodeWithText("Best Day").assertExists()
            onNodeWithText("Saturday").assertExists()
            
            // Heat map days
            onNodeWithText("Mon").assertExists()
            onNodeWithText("45").assertExists() // Monday minutes
        }
    }
    
    @Test
    fun testPersonalizedRecommendations() {
        // Given
        val recommendations = listOf(
            Recommendation(
                title = "Improve Accuracy",
                description = "Your accuracy is below 70%. Try using hints more strategically.",
                priority = Priority.HIGH,
                icon = Icons.Default.TipsAndUpdates,
                actionable = true
            ),
            Recommendation(
                title = "Build a Learning Streak",
                description = "Practice daily to build momentum.",
                priority = Priority.MEDIUM,
                icon = Icons.Default.LocalFireDepartment,
                actionable = true
            ),
            Recommendation(
                title = "Study at Peak Time",
                description = "Your best learning time is 3-5 PM.",
                priority = Priority.LOW,
                icon = Icons.Default.Schedule,
                actionable = false
            )
        )
        
        // When
        composeTestRule.setContent {
            PersonalizedRecommendations(recommendations)
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Personalized Recommendations").assertExists()
            
            // High priority recommendation
            onNodeWithText("Improve Accuracy").assertExists()
            onNodeWithText("Your accuracy is below 70%. Try using hints more strategically.").assertExists()
            onNodeWithText("HIGH").assertExists()
            
            // Action button for actionable items
            onAllNodesWithText("Take Action").assertCountEquals(2)
            
            // All recommendations present
            onNodeWithText("Build a Learning Streak").assertExists()
            onNodeWithText("Study at Peak Time").assertExists()
        }
    }
    
    @Test
    fun testTimeRangeSelector() {
        // When
        var selectedRange = TimeRange.WEEK
        
        composeTestRule.setContent {
            TimeRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it }
            )
        }
        
        // Then
        composeTestRule.apply {
            // Default selection
            onNodeWithText("This Week").assertExists()
            
            // Click to expand dropdown
            onNodeWithText("This Week").performClick()
            
            // Verify all options
            onNodeWithText("Today").assertExists()
            onNodeWithText("This Week").assertExists()
            onNodeWithText("This Month").assertExists()
            onNodeWithText("All Time").assertExists()
            
            // Select different range
            onNodeWithText("This Month").performClick()
        }
    }
    
    @Test
    fun testCompleteAnalyticsDashboard() {
        // Given
        val analyticsData = AnalyticsData(
            overallProgress = OverallProgress(
                completionRate = 0.75f,
                skillsMastered = 12,
                currentStreak = 7,
                totalPoints = 1250
            ),
            performanceMetrics = PerformanceMetrics(
                accuracyRate = 0.82f,
                accuracyTrend = 0.05f,
                avgResponseTime = 32,
                responseTimeTrend = -0.1f,
                questionsPerDay = 15,
                questionsPerDayTrend = 0.2f,
                improvementRate = 0.15f,
                improvementTrend = 0.08f
            ),
            skillMastery = listOf(
                SkillMasteryData(
                    skillName = "Character Analysis",
                    skillCode = "RL.4.2",
                    mastery = 0.85f,
                    questionsAnswered = 20,
                    correctAnswers = 17,
                    recentImprovement = 0.1f
                )
            ),
            learningPatterns = LearningPatterns(
                bestLearningTime = "3:00 PM - 5:00 PM",
                averageSessionDuration = 25,
                preferredDifficulty = "Medium",
                learningStyles = listOf(
                    LearningStylePreference("Visual", 0.7f)
                ),
                focusAreas = listOf("Character Analysis")
            ),
            subjectPerformance = listOf(
                SubjectPerformance(
                    name = "Reading",
                    progress = 0.75f,
                    questionsCompleted = 150,
                    accuracy = 0.82f,
                    icon = Icons.Default.Book,
                    color = Color(0xFF4CAF50)
                )
            ),
            recentAchievements = listOf(
                Achievement(
                    id = "speed_reader",
                    name = "Speed Reader",
                    description = "Read quickly",
                    iconUrl = "icon",
                    rarity = AchievementRarity.RARE,
                    unlockedAt = Date()
                )
            ),
            studyTimeData = StudyTimeData(
                totalMinutesThisWeek = 350,
                dailyAverage = 50,
                mostProductiveDay = "Saturday",
                weeklyData = listOf(
                    DayStudyData("Monday", 45),
                    DayStudyData("Tuesday", 60),
                    DayStudyData("Wednesday", 30),
                    DayStudyData("Thursday", 55),
                    DayStudyData("Friday", 40),
                    DayStudyData("Saturday", 75),
                    DayStudyData("Sunday", 45)
                ),
                monthlyTrend = 0.15f
            ),
            recommendations = listOf(
                Recommendation(
                    title = "Keep it up!",
                    description = "You're doing great",
                    priority = Priority.LOW,
                    icon = Icons.Default.ThumbUp,
                    actionable = false
                )
            )
        )
        
        // When
        composeTestRule.setContent {
            AnalyticsDashboardContent(
                data = analyticsData
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify all major sections are present
            onNodeWithText("Overall Progress").assertExists()
            onNodeWithText("75").assertExists() // Completion %
            
            onNodeWithText("Accuracy Rate").assertExists()
            onNodeWithText("82%").assertExists()
            
            onNodeWithText("Skill Mastery Progress").assertExists()
            onNodeWithText("Character Analysis").assertExists()
            
            onNodeWithText("Learning Patterns").assertExists()
            onNodeWithText("3:00 PM - 5:00 PM").assertExists()
            
            onNodeWithText("Subject Performance").assertExists()
            onNodeWithText("Reading").assertExists()
            
            onNodeWithText("Recent Achievements").assertExists()
            onNodeWithText("Speed Reader").assertExists()
            
            onNodeWithText("Study Time Analysis").assertExists()
            onNodeWithText("5h 50m").assertExists()
            
            onNodeWithText("Personalized Recommendations").assertExists()
            onNodeWithText("Keep it up!").assertExists()
        }
    }
}