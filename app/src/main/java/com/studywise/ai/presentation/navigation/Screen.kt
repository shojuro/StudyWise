package com.studywise.ai.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object SchoolSelection : Screen("school_selection")
    object ProfileSetup : Screen("profile_setup/{userRole}") {
        fun createRoute(userRole: String) = "profile_setup/$userRole"
    }
    object TwoFactorAuth : Screen("two_factor_auth")
    object Dashboard : Screen("dashboard")
    object LearningSession : Screen("learning_session/{subject}") {
        fun createRoute(subject: String) = "learning_session/$subject"
    }
    object Progress : Screen("progress")
    object SubjectProgressDetail : Screen("subject_progress/{subject}") {
        fun createRoute(subject: String) = "subject_progress/$subject"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object ParentDashboard : Screen("parent_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object PhotoLearning : Screen("photo_learning")
    object Analytics : Screen("analytics")
    object VerbalJournal : Screen("verbal_journal/{sessionId}") {
        fun createRoute(sessionId: String = "") = "verbal_journal/$sessionId"
    }
}