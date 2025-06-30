package com.studywise.ai.presentation.screens.verbaljournal

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studywise.ai.domain.model.verbaljournal.ProficiencyLevel
import com.studywise.ai.presentation.components.animations.SmoothTransitions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbalJournalScreen(
    onNavigateToSession: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProgress: () -> Unit,
    viewModel: VerbalJournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Verbal Journal",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToProgress) {
                        Icon(Icons.Default.Analytics, contentDescription = "Progress")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is VerbalJournalUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is VerbalJournalUiState.Success -> {
                VerbalJournalContent(
                    state = state,
                    onStartSession = {
                        viewModel.startNewSession()
                        onNavigateToSession()
                    },
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToProgress = onNavigateToProgress,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            is VerbalJournalUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun VerbalJournalContent(
    state: VerbalJournalUiState.Success,
    onStartSession: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Prompt Card
        TodayPromptCard(
            prompt = state.todayPrompt,
            onStartSession = onStartSession
        )
        
        // Progress Summary Card
        ProgressSummaryCard(
            progress = state.progress,
            onViewDetails = onNavigateToProgress
        )
        
        // Break-in Period Status
        if (!state.breakInStatus.isComplete) {
            BreakInPeriodCard(
                breakInStatus = state.breakInStatus
            )
        }
        
        // Recent Sessions
        if (state.recentSessions.isNotEmpty()) {
            RecentSessionsCard(
                sessions = state.recentSessions,
                onViewAll = onNavigateToHistory
            )
        }
        
        // Weekly Tips
        WeeklyTipsCard(
            tips = state.breakInStatus.weeklyTips
        )
    }
}

@Composable
private fun TodayPromptCard(
    prompt: com.studywise.ai.domain.model.verbaljournal.JournalPrompt,
    onStartSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Topic",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Chip(
                        onClick = { },
                        label = { Text(prompt.category.name.replace("_", " ")) },
                        colors = ChipDefaults.chipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
                
                Text(
                    text = prompt.promptText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${prompt.estimatedMinutes} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    Button(
                        onClick = onStartSession,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Speaking")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressSummaryCard(
    progress: com.studywise.ai.domain.usecase.verbaljournal.VerbalJournalProgress,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewDetails
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProgressMetric(
                    label = "Streak",
                    value = "${progress.currentStreak}",
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = if (progress.currentStreak > 0) Color(0xFFFF6B35) else Color.Gray
                )
                
                ProgressMetric(
                    label = "Minutes",
                    value = "${progress.totalMinutesSpoken}",
                    icon = Icons.Default.Timer
                )
                
                ProgressMetric(
                    label = "Accuracy",
                    value = "${(progress.averageAccuracy * 100).toInt()}%",
                    icon = Icons.Default.CheckCircle,
                    iconTint = when {
                        progress.averageAccuracy >= 0.9f -> Color(0xFF4CAF50)
                        progress.averageAccuracy >= 0.7f -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5252)
                    }
                )
                
                ProgressMetric(
                    label = "Level",
                    value = when (progress.profile.currentLevel) {
                        ProficiencyLevel.BEGINNER -> "B"
                        ProficiencyLevel.INTERMEDIATE -> "I"
                        ProficiencyLevel.ADVANCED -> "A"
                        ProficiencyLevel.NATIVE_LEVEL -> "N"
                    },
                    icon = Icons.Default.School
                )
            }
            
            // Weekly Goal Progress
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Weekly Goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${progress.weeklyGoalProgress.completedMinutes}/${progress.weeklyGoalProgress.targetMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                LinearProgressIndicator(
                    progress = { progress.weeklyGoalProgress.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconTint
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreakInPeriodCard(
    breakInStatus: com.studywise.ai.domain.usecase.verbaljournal.BreakInPeriodStatus.Success
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Break-in Period",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = "Week ${breakInStatus.currentWeek}/12",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            
            LinearProgressIndicator(
                progress = { breakInStatus.currentWeek / 12f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    label = "Session Length",
                    value = "${breakInStatus.currentSchedule.sessionLengthMinutes} min"
                )
                
                InfoChip(
                    label = "Focus",
                    value = breakInStatus.currentSchedule.correctionFocus.name
                )
                
                if (breakInStatus.progressionReadiness.readyToProgress) {
                    Chip(
                        onClick = { },
                        label = { Text("Ready for next week!") },
                        colors = ChipDefaults.chipColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RecentSessionsCard(
    sessions: List<com.studywise.ai.domain.usecase.verbaljournal.SessionHistoryItem>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium
                )
                
                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }
            
            sessions.take(3).forEach { session ->
                RecentSessionItem(session = session)
            }
        }
    }
}

@Composable
private fun RecentSessionItem(
    session: com.studywise.ai.domain.usecase.verbaljournal.SessionHistoryItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(session.date),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${session.duration} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                session.accuracy?.let { accuracy ->
                    Text(
                        text = "${(accuracy * 100).toInt()}% accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            accuracy >= 0.9f -> Color(0xFF4CAF50)
                            accuracy >= 0.7f -> Color(0xFFFFC107)
                            else -> Color(0xFFFF5252)
                        }
                    )
                }
            }
        }
        
        if (session.achievements.isNotEmpty()) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = "Achievement earned",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFFFD700)
            )
        }
    }
}

@Composable
private fun WeeklyTipsCard(
    tips: List<String>
) {
    if (tips.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Tips for This Week",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            tips.forEach { tip ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}