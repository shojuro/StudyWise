package com.studywise.ai.domain.model.verbaljournal

/**
 * Break-in period schedules for new users
 * Gradually increases session length and correction complexity
 * Based on ESL pedagogy best practices
 */
object BreakInSchedules {
    
    /**
     * Standard 12-week break-in schedule
     * Designed to prevent overwhelm and build sustainable habits
     */
    val standardSchedule = listOf(
        BreakInSchedule(
            week = 1,
            sessionLengthMinutes = 2,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 60f,
            description = "Introduction week - Focus on getting comfortable speaking. " +
                    "Only critical errors that prevent understanding are highlighted."
        ),
        BreakInSchedule(
            week = 2,
            sessionLengthMinutes = 5,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 65f,
            description = "Building confidence - Slightly longer sessions. " +
                    "Continue focusing only on communication-breaking errors."
        ),
        BreakInSchedule(
            week = 3,
            sessionLengthMinutes = 8,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 70f,
            description = "Establishing routine - Sessions approach 10 minutes. " +
                    "Maintain focus on critical errors to build fluency."
        ),
        BreakInSchedule(
            week = 4,
            sessionLengthMinutes = 10,
            correctionFocus = CorrectionTier.IMPORTANT,
            accuracyTarget = 72f,
            description = "Expanding awareness - Now including errors that cause confusion. " +
                    "Modal verbs, key prepositions, and conditionals."
        ),
        BreakInSchedule(
            week = 5,
            sessionLengthMinutes = 12,
            correctionFocus = CorrectionTier.IMPORTANT,
            accuracyTarget = 75f,
            description = "Deepening practice - Solidifying important grammar patterns. " +
                    "Focus on errors that might require clarification."
        ),
        BreakInSchedule(
            week = 6,
            sessionLengthMinutes = 15,
            correctionFocus = CorrectionTier.IMPORTANT,
            accuracyTarget = 77f,
            description = "Mid-program milestone - Half the target duration. " +
                    "Mastering confusion-level error correction."
        ),
        BreakInSchedule(
            week = 7,
            sessionLengthMinutes = 18,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 80f,
            description = "Refining speech - Now including noticeable errors. " +
                    "Articles, plurals, and minor prepositions."
        ),
        BreakInSchedule(
            week = 8,
            sessionLengthMinutes = 20,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 82f,
            description = "Natural conversation length - 20-minute sessions. " +
                    "Working on errors that mark non-native speech."
        ),
        BreakInSchedule(
            week = 9,
            sessionLengthMinutes = 23,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 83f,
            description = "Advanced practice - Approaching full session length. " +
                    "Polishing common non-native patterns."
        ),
        BreakInSchedule(
            week = 10,
            sessionLengthMinutes = 25,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 84f,
            description = "Near-target duration - Almost at 30 minutes. " +
                    "Consolidating helpful-tier corrections."
        ),
        BreakInSchedule(
            week = 11,
            sessionLengthMinutes = 28,
            correctionFocus = CorrectionTier.COMPREHENSIVE,
            accuracyTarget = 85f,
            description = "Polish phase - Including style and idiom corrections. " +
                    "Subject-verb agreement, collocations, register."
        ),
        BreakInSchedule(
            week = 12,
            sessionLengthMinutes = 30,
            correctionFocus = CorrectionTier.COMPREHENSIVE,
            accuracyTarget = 85f,
            description = "Full program - 30-minute sessions with comprehensive feedback. " +
                    "Ready for long-term practice routine."
        )
    )
    
    /**
     * Accelerated 6-week schedule for motivated/advanced learners
     */
    val acceleratedSchedule = listOf(
        BreakInSchedule(
            week = 1,
            sessionLengthMinutes = 5,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 70f,
            description = "Fast start - 5-minute sessions focusing on critical errors only."
        ),
        BreakInSchedule(
            week = 2,
            sessionLengthMinutes = 10,
            correctionFocus = CorrectionTier.IMPORTANT,
            accuracyTarget = 75f,
            description = "Quick progression - Jump to important-tier corrections."
        ),
        BreakInSchedule(
            week = 3,
            sessionLengthMinutes = 15,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 80f,
            description = "Mid-point - Half target duration with helpful corrections."
        ),
        BreakInSchedule(
            week = 4,
            sessionLengthMinutes = 20,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 82f,
            description = "Building stamina - 20-minute sessions."
        ),
        BreakInSchedule(
            week = 5,
            sessionLengthMinutes = 25,
            correctionFocus = CorrectionTier.COMPREHENSIVE,
            accuracyTarget = 84f,
            description = "Near target - Almost full duration with all corrections."
        ),
        BreakInSchedule(
            week = 6,
            sessionLengthMinutes = 30,
            correctionFocus = CorrectionTier.COMPREHENSIVE,
            accuracyTarget = 85f,
            description = "Full program achieved - Ready for sustained practice."
        )
    )
    
    /**
     * Gentle 16-week schedule for beginners or those with limited time
     */
    val gentleSchedule = listOf(
        BreakInSchedule(
            week = 1,
            sessionLengthMinutes = 1,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 50f,
            description = "Micro-start - Just 1 minute to build the habit."
        ),
        BreakInSchedule(
            week = 2,
            sessionLengthMinutes = 2,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 55f,
            description = "Doubling up - Still very short, building confidence."
        ),
        BreakInSchedule(
            week = 4,
            sessionLengthMinutes = 5,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 60f,
            description = "First milestone - 5-minute sessions."
        ),
        BreakInSchedule(
            week = 6,
            sessionLengthMinutes = 8,
            correctionFocus = CorrectionTier.CRITICAL,
            accuracyTarget = 65f,
            description = "Steady progress - Approaching 10 minutes."
        ),
        BreakInSchedule(
            week = 8,
            sessionLengthMinutes = 10,
            correctionFocus = CorrectionTier.IMPORTANT,
            accuracyTarget = 70f,
            description = "Adding complexity - Important-tier corrections begin."
        ),
        BreakInSchedule(
            week = 10,
            sessionLengthMinutes = 15,
            correctionFocus = CorrectionTier.IMPORTANT,
            accuracyTarget = 75f,
            description = "Half-way point - 15-minute sessions."
        ),
        BreakInSchedule(
            week = 12,
            sessionLengthMinutes = 20,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 78f,
            description = "Expanding feedback - Helpful corrections added."
        ),
        BreakInSchedule(
            week = 14,
            sessionLengthMinutes = 25,
            correctionFocus = CorrectionTier.HELPFUL,
            accuracyTarget = 80f,
            description = "Nearly there - 25-minute sessions."
        ),
        BreakInSchedule(
            week = 16,
            sessionLengthMinutes = 30,
            correctionFocus = CorrectionTier.COMPREHENSIVE,
            accuracyTarget = 82f,
            description = "Complete - Full sessions with all correction tiers."
        )
    )
    
    /**
     * Maintenance schedule for advanced speakers
     */
    val maintenanceSchedule = BreakInSchedule(
        week = 0,
        sessionLengthMinutes = 30,
        correctionFocus = CorrectionTier.COMPREHENSIVE,
        accuracyTarget = 90f,
        description = "Maintenance mode - Full sessions focusing on polish and style. " +
                "Suitable for C1/C2 speakers maintaining fluency."
    )
    
    /**
     * Get appropriate schedule based on user profile
     */
    fun getScheduleForUser(
        proficiencyLevel: ProficiencyLevel,
        availableMinutesPerDay: Int,
        learningPace: String = "standard"
    ): List<BreakInSchedule> {
        return when {
            // Advanced users or those with more time can use accelerated
            proficiencyLevel == ProficiencyLevel.ADVANCED && 
            availableMinutesPerDay >= 30 && 
            learningPace == "fast" -> acceleratedSchedule
            
            // Beginners or time-constrained users get gentle schedule
            proficiencyLevel == ProficiencyLevel.BEGINNER || 
            availableMinutesPerDay < 15 || 
            learningPace == "slow" -> gentleSchedule
            
            // Native-level speakers skip break-in
            proficiencyLevel == ProficiencyLevel.NATIVE_LEVEL -> 
                listOf(maintenanceSchedule)
            
            // Everyone else gets standard
            else -> standardSchedule
        }
    }
    
    /**
     * Get current week's schedule based on start date
     */
    fun getCurrentWeekSchedule(
        weeksSinceStart: Int,
        schedule: List<BreakInSchedule> = standardSchedule
    ): BreakInSchedule {
        // Find the appropriate week's schedule
        return schedule
            .filter { it.week <= weeksSinceStart }
            .maxByOrNull { it.week }
            ?: schedule.first()
    }
    
    /**
     * Check if user has completed break-in period
     */
    fun isBreakInComplete(
        weeksSinceStart: Int,
        schedule: List<BreakInSchedule> = standardSchedule
    ): Boolean {
        val finalWeek = schedule.maxOf { it.week }
        return weeksSinceStart >= finalWeek
    }
    
    /**
     * Calculate progress through break-in period
     */
    fun getBreakInProgress(
        weeksSinceStart: Int,
        schedule: List<BreakInSchedule> = standardSchedule
    ): Float {
        val finalWeek = schedule.maxOf { it.week }
        return (weeksSinceStart.toFloat() / finalWeek).coerceIn(0f, 1f)
    }
    
    /**
     * Get milestone description for current week
     */
    fun getMilestoneDescription(weeksSinceStart: Int): String? {
        return when (weeksSinceStart) {
            1 -> "🎯 First week complete! You're building the speaking habit."
            4 -> "📈 One month in! Correction complexity increased."
            6 -> "⭐ Halfway there! 15-minute sessions achieved."
            8 -> "🚀 Two months strong! 20-minute conversations."
            12 -> "🏆 Break-in complete! Full 30-minute sessions unlocked."
            else -> null
        }
    }
}