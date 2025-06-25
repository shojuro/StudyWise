package com.studywise.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.presentation.screens.auth.LoginScreen
import com.studywise.ai.presentation.screens.auth.RegisterScreen
import com.studywise.ai.presentation.screens.dashboard.StudentDashboardScreen
import com.studywise.ai.presentation.screens.dashboard.ParentDashboardScreen
import com.studywise.ai.presentation.screens.dashboard.TeacherDashboardScreen
import com.studywise.ai.presentation.screens.session.LearningSessionScreen
import com.studywise.ai.presentation.screens.progress.ProgressScreen
import com.studywise.ai.presentation.screens.profile.ProfileScreen
import com.studywise.ai.presentation.screens.settings.SettingsScreen
import com.studywise.ai.presentation.screens.splash.SplashScreen

@Composable
fun StudyWiseNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = { userRole ->
                    val dashboardRoute = when (userRole) {
                        UserRole.STUDENT -> Screen.Dashboard.route
                        UserRole.PARENT -> Screen.ParentDashboard.route
                        UserRole.TEACHER -> Screen.TeacherDashboard.route
                    }
                    navController.navigate(dashboardRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { userRole ->
                    val dashboardRoute = when (userRole) {
                        UserRole.STUDENT -> Screen.Dashboard.route
                        UserRole.PARENT -> Screen.ParentDashboard.route
                        UserRole.TEACHER -> Screen.TeacherDashboard.route
                    }
                    navController.navigate(dashboardRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { userRole ->
                    val dashboardRoute = when (userRole) {
                        UserRole.STUDENT -> Screen.Dashboard.route
                        UserRole.PARENT -> Screen.ParentDashboard.route
                        UserRole.TEACHER -> Screen.TeacherDashboard.route
                    }
                    navController.navigate(dashboardRoute) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            StudentDashboardScreen(
                onNavigateToSession = { subject ->
                    navController.navigate(Screen.LearningSession.createRoute(subject))
                },
                onNavigateToProgress = {
                    navController.navigate(Screen.Progress.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.ParentDashboard.route) {
            ParentDashboardScreen(
                onNavigateToChildProgress = { childId ->
                    navController.navigate(Screen.Progress.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.TeacherDashboard.route) {
            TeacherDashboardScreen(
                onNavigateToStudentProgress = { studentId ->
                    navController.navigate(Screen.Progress.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.LearningSession.route) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: ""
            LearningSessionScreen(
                subject = subject,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}