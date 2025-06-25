package com.studywise.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.presentation.screens.auth.LoginScreen
import com.studywise.ai.presentation.screens.auth.RegisterScreen
import com.studywise.ai.presentation.screens.auth.OnboardingScreen
import com.studywise.ai.presentation.screens.auth.SchoolSelectionScreen
import com.studywise.ai.presentation.screens.auth.ProfileSetupScreen
import com.studywise.ai.presentation.screens.auth.TwoFactorAuthScreen
import com.studywise.ai.presentation.screens.dashboard.StudentDashboardScreen
import com.studywise.ai.presentation.screens.dashboard.ParentDashboardScreen
import com.studywise.ai.presentation.screens.dashboard.TeacherDashboardScreen
import com.studywise.ai.presentation.screens.session.LearningSessionScreen
import com.studywise.ai.presentation.screens.progress.ProgressScreen
import com.studywise.ai.presentation.screens.profile.ProfileScreen
import com.studywise.ai.presentation.screens.settings.SettingsScreen
import com.studywise.ai.presentation.screens.splash.SplashScreen
import com.studywise.ai.presentation.viewmodel.SharedAuthViewModel

@Composable
fun StudyWiseNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val sharedAuthViewModel: SharedAuthViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Onboarding.route) {
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
                    val authState = sharedAuthViewModel.authState.value
                    if (authState.isDemoMode) {
                        // Skip auth flow for demo mode
                        val dashboardRoute = when (userRole) {
                            UserRole.STUDENT -> Screen.Dashboard.route
                            UserRole.PARENT -> Screen.ParentDashboard.route
                            UserRole.TEACHER -> Screen.TeacherDashboard.route
                        }
                        navController.navigate(dashboardRoute) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        // For real users, go through 2FA
                        navController.navigate(Screen.TwoFactorAuth.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                sharedAuthViewModel = sharedAuthViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { userRole ->
                    sharedAuthViewModel.setUserRole(userRole)
                    navController.navigate(Screen.SchoolSelection.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SchoolSelection.route) {
            val authState by sharedAuthViewModel.authState.collectAsState()
            SchoolSelectionScreen(
                onSchoolSelected = { schoolId ->
                    sharedAuthViewModel.setSchoolId(schoolId)
                    val userRole = authState.userRole?.name ?: "STUDENT"
                    navController.navigate(Screen.ProfileSetup.createRoute(userRole))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ProfileSetup.route) { backStackEntry ->
            val userRoleString = backStackEntry.arguments?.getString("userRole") ?: "STUDENT"
            val userRole = try {
                UserRole.valueOf(userRoleString)
            } catch (e: Exception) {
                UserRole.STUDENT
            }
            
            ProfileSetupScreen(
                userRole = userRole,
                onProfileComplete = {
                    navController.navigate(Screen.TwoFactorAuth.route) {
                        popUpTo(Screen.SchoolSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TwoFactorAuth.route) {
            val authState by sharedAuthViewModel.authState.collectAsState()
            TwoFactorAuthScreen(
                onVerificationSuccess = {
                    val dashboardRoute = when (authState.userRole) {
                        UserRole.STUDENT -> Screen.Dashboard.route
                        UserRole.PARENT -> Screen.ParentDashboard.route
                        UserRole.TEACHER -> Screen.TeacherDashboard.route
                        null -> Screen.Dashboard.route
                    }
                    navController.navigate(dashboardRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            StudentDashboardScreen(
                onNavigateToSession = { subject ->
                    navController.navigate(Screen.LearningSession.createRoute(subject)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProgress = {
                    navController.navigate(Screen.Progress.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
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