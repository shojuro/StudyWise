package com.studywise.ai.domain.model

import java.util.Date

data class ProgressData(
    val userId: String,
    val subject: String,
    val date: Date,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val timeSpentMinutes: Int,
    val skillsProgress: Map<String, Float>,
    val streakDays: Int,
    val totalPoints: Int
)

data class WeeklyProgress(
    val weekStartDate: Date,
    val dailyProgress: List<DailyProgress>,
    val totalQuestions: Int,
    val totalCorrect: Int,
    val averageAccuracy: Float,
    val totalTimeMinutes: Int,
    val subjectsStudied: Set<String>,
    val dailyStats: List<DailyProgress> = emptyList(),
    val totalMinutes: Int = 0
)

data class DailyProgress(
    val date: Date,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val timeSpentMinutes: Int,
    val subjects: List<String>,
    val accuracy: Float,
    val sessionsCompleted: Int = 0,
    val minutesStudied: Int = 0,
    val pointsEarned: Int = 0
)

data class SubjectProgress(
    val subject: String,
    val totalSessions: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val averageAccuracy: Float,
    val totalTimeMinutes: Int,
    val lastPracticed: Date?,
    val skillMastery: Map<String, Float>,
    val trend: ProgressTrend,
    val totalMinutes: Int = totalTimeMinutes,
    val averageMastery: Double = skillMastery.values.average()
)

enum class ProgressTrend {
    IMPROVING,
    STABLE,
    DECLINING
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val progress: Float,
    val isUnlocked: Boolean,
    val unlockedDate: Date?,
    val category: AchievementCategory,
    val points: Int,
    val name: String = title,
    val icon: String = "star"
)

enum class AchievementCategory {
    STREAK,
    ACCURACY,
    COMPLETION,
    MASTERY,
    EXPLORATION,
    SPEED
}

data class SkillProgress(
    val skillId: String,
    val skillName: String,
    val currentLevel: Int,
    val progress: Float,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val lastPracticed: Date?,
    val milestones: List<SkillMilestone>
)

data class SkillMilestone(
    val level: Int,
    val description: String,
    val isAchieved: Boolean,
    val achievedDate: Date?
)