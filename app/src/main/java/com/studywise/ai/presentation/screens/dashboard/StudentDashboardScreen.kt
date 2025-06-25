package com.studywise.ai.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.presentation.components.SubjectCard
import com.studywise.ai.presentation.theme.SubjectColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    onNavigateToSession: (String) -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: StudentDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello, ${uiState.userName}!",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Choose a subject to practice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.semantics {
                            contentDescription = "Profile"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.semantics {
                            contentDescription = "Settings"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on dashboard */ },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null
                        )
                    },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProgress,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null
                        )
                    },
                    label = { Text("Progress") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO: Achievements */ },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null
                        )
                    },
                    label = { Text("Awards") }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.subjects) { subject ->
                    SubjectCard(
                        subject = subject.subject,
                        progress = subject.progress,
                        lastPracticed = subject.lastPracticed,
                        onClick = { onNavigateToSession(subject.subject) },
                        modifier = Modifier.fillMaxWidth(),
                        subjectColor = SubjectColors[subject.subject] ?: MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}