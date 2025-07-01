package com.studywise.ai.domain.model.verbaljournal

import java.time.LocalDate

/**
 * 12-week break-in period system for gradual skill building
 */
data class BreakInSchedule(
    val week: Int,
    val phase: BreakInPhase,
    val dailyGoalMinutes: Int,
    val focusAreas: List<FocusArea>,
    val milestones: List<String>,
    val supportFeatures: List<SupportFeature>
)

data class FocusArea(
    val skill: String,
    val description: String,
    val exercises: List<String>
)

data class SupportFeature(
    val name: String,
    val description: String,
    val isEnabled: Boolean = true
)

enum class BreakInPhase {
    FOUNDATION,      // Weeks 1-4
    BUILDING,        // Weeks 5-8  
    ADVANCING,       // Weeks 9-12
    GRADUATED        // Post 12 weeks
}

object BreakInPeriodSchedules {
    val COMPLETE_SCHEDULE = listOf(
        // Week 1-2: Foundation Phase
        BreakInSchedule(
            week = 1,
            phase = BreakInPhase.FOUNDATION,
            dailyGoalMinutes = 3,
            focusAreas = listOf(
                FocusArea(
                    skill = "Basic Greetings",
                    description = "Simple hello, goodbye, how are you exchanges",
                    exercises = listOf(
                        "Repeat after AI greeting variations",
                        "Practice different times of day greetings",
                        "Record and compare pronunciation"
                    )
                ),
                FocusArea(
                    skill = "Self Introduction",
                    description = "Name, where from, basic personal info",
                    exercises = listOf(
                        "30-second self introduction",
                        "Answer 'Tell me about yourself'",
                        "Practice spelling name clearly"
                    )
                )
            ),
            milestones = listOf(
                "Complete 5 sessions",
                "Record clear self-introduction",
                "Use 3 different greetings naturally"
            ),
            supportFeatures = listOf(
                SupportFeature("AI speaks slowly", "50% reduced speech rate"),
                SupportFeature("Visual word hints", "Key words displayed on screen"),
                SupportFeature("Unlimited retries", "No pressure to be perfect"),
                SupportFeature("Celebration animations", "Positive reinforcement for attempts")
            )
        ),
        
        BreakInSchedule(
            week = 2,
            phase = BreakInPhase.FOUNDATION,
            dailyGoalMinutes = 5,
            focusAreas = listOf(
                FocusArea(
                    skill = "Daily Activities",
                    description = "Talking about routine activities",
                    exercises = listOf(
                        "Describe your morning routine",
                        "What did you do yesterday?",
                        "Weekend plans discussion"
                    )
                ),
                FocusArea(
                    skill = "Simple Questions",
                    description = "Asking and answering basic questions",
                    exercises = listOf(
                        "Practice WH-questions",
                        "Yes/No question patterns",
                        "Follow-up questions"
                    )
                )
            ),
            milestones = listOf(
                "10 total sessions completed",
                "5-turn conversation achieved",
                "Ask 3 questions spontaneously"
            ),
            supportFeatures = listOf(
                SupportFeature("AI speaks slowly", "50% reduced speech rate"),
                SupportFeature("Sentence starters provided", "Templates for common phrases"),
                SupportFeature("Grammar hints", "Gentle corrections shown"),
                SupportFeature("Progress tracker visible", "See your improvement")
            )
        ),
        
        // Week 3-4: Foundation Phase Continued
        BreakInSchedule(
            week = 3,
            phase = BreakInPhase.FOUNDATION,
            dailyGoalMinutes = 5,
            focusAreas = listOf(
                FocusArea(
                    skill = "Describing Things",
                    description = "Using adjectives and simple descriptions",
                    exercises = listOf(
                        "Describe your favorite food",
                        "Talk about the weather",
                        "Describe a family member"
                    )
                ),
                FocusArea(
                    skill = "Expressing Preferences",
                    description = "Likes, dislikes, and preferences",
                    exercises = listOf(
                        "I like/don't like patterns",
                        "Prefer X to Y structures",
                        "Explaining why you like something"
                    )
                )
            ),
            milestones = listOf(
                "15 total sessions",
                "Use 10 new adjectives",
                "2-minute continuous speaking"
            ),
            supportFeatures = listOf(
                SupportFeature("AI speaks normally", "Gradual speed increase", isEnabled = false),
                SupportFeature("Vocabulary helper", "Suggested words when stuck"),
                SupportFeature("Pronunciation feedback", "Focus on problem sounds"),
                SupportFeature("Weekly progress report", "Detailed improvement analysis")
            )
        ),
        
        BreakInSchedule(
            week = 4,
            phase = BreakInPhase.FOUNDATION,
            dailyGoalMinutes = 7,
            focusAreas = listOf(
                FocusArea(
                    skill = "Past Events",
                    description = "Talking about what happened",
                    exercises = listOf(
                        "What did you do last weekend?",
                        "Describe a memorable day",
                        "Tell a simple story"
                    )
                ),
                FocusArea(
                    skill = "Future Plans",
                    description = "Discussing plans and intentions",
                    exercises = listOf(
                        "Tomorrow's schedule",
                        "Weekend plans",
                        "Future goals discussion"
                    )
                )
            ),
            milestones = listOf(
                "20 sessions milestone",
                "Tell a 1-minute story",
                "Use past and future tenses correctly 70% of time"
            ),
            supportFeatures = listOf(
                SupportFeature("Reduced AI support", "Less hand-holding", isEnabled = false),
                SupportFeature("Error summary", "Focus on repeated mistakes"),
                SupportFeature("Peer comparisons", "See how others at your level speak"),
                SupportFeature("First achievement badges", "Unlock foundation graduate badge")
            )
        ),
        
        // Week 5-8: Building Phase
        BreakInSchedule(
            week = 5,
            phase = BreakInPhase.BUILDING,
            dailyGoalMinutes = 10,
            focusAreas = listOf(
                FocusArea(
                    skill = "Opinion Expression",
                    description = "Sharing and justifying opinions",
                    exercises = listOf(
                        "I think... because...",
                        "Agree/disagree politely",
                        "Pros and cons discussion"
                    )
                ),
                FocusArea(
                    skill = "Comparisons",
                    description = "Comparing things, people, experiences",
                    exercises = listOf(
                        "X is better than Y",
                        "The most/least patterns",
                        "Similarities and differences"
                    )
                )
            ),
            milestones = listOf(
                "Express opinion with 3 supporting points",
                "5-minute conversation sustained",
                "Use 5 comparison structures"
            ),
            supportFeatures = listOf(
                SupportFeature("Topic suggestions only", "AI provides less structure"),
                SupportFeature("Advanced vocabulary intro", "Learn sophisticated alternatives"),
                SupportFeature("Fluency metrics", "Track pauses and filler words"),
                SupportFeature("Conversation replay", "Review and self-assess")
            )
        ),
        
        // Continue for weeks 6-12...
        // (Abbreviated for space - would include all 12 weeks)
        
        BreakInSchedule(
            week = 12,
            phase = BreakInPhase.ADVANCING,
            dailyGoalMinutes = 15,
            focusAreas = listOf(
                FocusArea(
                    skill = "Complex Discussions",
                    description = "Abstract topics and nuanced arguments",
                    exercises = listOf(
                        "Debate controversial topics",
                        "Explain complex processes",
                        "Hypothetical scenarios"
                    )
                ),
                FocusArea(
                    skill = "Cultural Fluency",
                    description = "Idioms, humor, and cultural references",
                    exercises = listOf(
                        "Use idioms naturally",
                        "Understand and make jokes",
                        "Cultural comparison discussions"
                    )
                )
            ),
            milestones = listOf(
                "60 total sessions",
                "15-minute fluent conversation",
                "Graduate from break-in period!"
            ),
            supportFeatures = listOf(
                SupportFeature("Full autonomy mode", "Minimal AI guidance"),
                SupportFeature("Native-speed conversations", "Real-world pace"),
                SupportFeature("Peer mentoring unlocked", "Help newer users"),
                SupportFeature("Advanced analytics", "Detailed progress insights")
            )
        )
    )
    
    fun getScheduleForWeek(week: Int): BreakInSchedule? {
        return COMPLETE_SCHEDULE.find { it.week == week }
    }
    
    fun getScheduleForUser(startDate: LocalDate, currentDate: LocalDate): BreakInSchedule? {
        val weekNumber = ((currentDate.toEpochDay() - startDate.toEpochDay()) / 7).toInt() + 1
        return when {
            weekNumber < 1 -> COMPLETE_SCHEDULE.first()
            weekNumber > 12 -> null // Graduated
            else -> getScheduleForWeek(weekNumber) ?: COMPLETE_SCHEDULE.lastOrNull { it.week <= weekNumber }
        }
    }
    
    fun isInBreakInPeriod(startDate: LocalDate, currentDate: LocalDate): Boolean {
        val daysSinceStart = currentDate.toEpochDay() - startDate.toEpochDay()
        return daysSinceStart < 84 // 12 weeks = 84 days
    }
}