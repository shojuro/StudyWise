package com.studywise.ai.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.studywise.ai.presentation.components.NetworkStateIndicator
import com.studywise.ai.presentation.navigation.StudyWiseNavigation

/**
 * Root composable for the StudyWise app that includes global UI elements
 */
@Composable
fun StudyWiseApp(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        topBar = {
            // Network state indicator at the top of the app
            NetworkStateIndicator()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StudyWiseNavigation(navController = navController)
        }
    }
}