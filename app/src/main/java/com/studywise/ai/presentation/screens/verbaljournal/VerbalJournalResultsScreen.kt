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
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.usecase.verbaljournal.*
import com.studywise.ai.presentation.components.animations.CelebrationAnimations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbalJournalResultsScreen(
    sessionId: String,
    onNavigateHome: () -> Unit,
    onNavigateToProgress: () -> Unit,
    viewModel: VerbalJournalResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Load session results
    LaunchedEffect(sessionId) {
        viewModel.loadSessionResults(sessionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Session Complete!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    TextButton(onClick = onNavigateHome) {
                        Text("Done")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ResultsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is ResultsUiState.Success -> {
                ResultsContent(
                    result = state.result,
                    onNavigateToProgress = onNavigateToProgress,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            is ResultsUiState.Error -> {
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
private fun ResultsContent(
    result: EndSessionResult.Success,
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
        // Celebration for achievements
        if (result.newAchievements.isNotEmpty()) {
            CelebrationAnimations.AchievementCelebration(
                achievements = result.newAchievements
            )
        }
        
        // Session Summary
        SessionSummaryCard(summary = result.sessionSummary)
        
        // Performance Overview
        PerformanceOverviewCard(analysis = result.sessionAnalysis)
        
        // Error Analysis
        if (result.sessionAnalysis.errorBreakdown.errorsByType.isNotEmpty()) {
            ErrorAnalysisCard(
                errorBreakdown = result.sessionAnalysis.errorBreakdown,
                improvementAreas = result.sessionAnalysis.improvementAreas
            )
        }
        
        // Next Steps
        NextStepsCard(
            nextSteps = result.nextSteps,
            recommendations = result.sessionAnalysis.recommendations
        )
        
        // Profile Progress
        ProfileProgressCard(
            updatedProfile = result.updatedProfile,
            progressMetrics = result.progressMetrics
        )
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToProgress,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Progress")
            }
            
            Button(
                onClick = { /* Share results */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        }
    }
}

@Composable
private fun SessionSummaryCard(
    summary: SessionSummary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Great job! Here's your summary:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryMetric(
                    icon = Icons.Default.Timer,
                    value = "${summary.duration}",
                    label = "minutes",
                    color = MaterialTheme.colorScheme.primary
                )
                
                SummaryMetric(
                    icon = Icons.Default.TextFields,
                    value = "${summary.wordsSpoken}",
                    label = "words",
                    color = MaterialTheme.colorScheme.secondary
                )
                
                SummaryMetric(
                    icon = Icons.Default.CheckCircle,
                    value = "${(summary.accuracy * 100).toInt()}%",
                    label = "accuracy",
                    color = getAccuracyColor(summary.accuracy)
                )
                
                SummaryMetric(
                    icon = Icons.Default.Speed,
                    value = "${(summary.fluencyScore * 100).toInt()}%",
                    label = "fluency",
                    color = getFluencyColor(summary.fluencyScore)
                )
            }
            
            if (summary.mainTopics.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Topics:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    summary.mainTopics.forEach { topic ->
                        AssistChip(
                            onClick = { },
                            label = { Text(topic, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PerformanceOverviewCard(
    analysis: SessionAnalysis
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Performance Breakdown",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Tier-specific accuracy
            analysis.overallStats.tierSpecificAccuracy.forEach { (tier, accuracy) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tier.name} Errors",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { accuracy },
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = getAccuracyColor(accuracy),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        
                        Text(
                            text = "${(accuracy * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            // Pause analysis
            val pauseAnalysis = analysis.overallStats.pausePattern
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PauseMetric(
                    label = "Filler Words",
                    value = pauseAnalysis.fillerWordCount,
                    isGood = pauseAnalysis.fillerWordCount < 5
                )
                
                PauseMetric(
                    label = "Hesitations",
                    value = pauseAnalysis.hesitationCount,
                    isGood = pauseAnalysis.hesitationCount < 3
                )
                
                PauseMetric(
                    label = "Natural Pauses",
                    value = "${(pauseAnalysis.naturalPauseRatio * 100).toInt()}%",
                    isGood = pauseAnalysis.naturalPauseRatio > 0.7f
                )
            }
        }
    }
}

@Composable
private fun PauseMetric(
    label: String,
    value: Any,
    isGood: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isGood) Color(0xFF4CAF50) else Color(0xFFFF6B35)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorAnalysisCard(
    errorBreakdown: ErrorBreakdown,
    improvementAreas: List<ImprovementArea>
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
                    text = "Error Analysis",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (errorBreakdown.improvementFromLastSession != 0f) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (errorBreakdown.improvementFromLastSession > 0) {
                                Icons.Default.TrendingUp
                            } else {
                                Icons.Default.TrendingDown
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (errorBreakdown.improvementFromLastSession > 0) {
                                Color(0xFF4CAF50)
                            } else {
                                Color(0xFFFF5252)
                            }
                        )
                        Text(
                            text = "${if (errorBreakdown.improvementFromLastSession > 0) "+" else ""}${(errorBreakdown.improvementFromLastSession * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (errorBreakdown.improvementFromLastSession > 0) {
                                Color(0xFF4CAF50)
                            } else {
                                Color(0xFFFF5252)
                            }
                        )
                    }
                }
            }
            
            // Top improvement areas
            improvementAreas.take(3).forEach { area ->
                ImprovementAreaItem(area = area)
            }
            
            // Persistent vs New errors
            if (errorBreakdown.persistentErrors.isNotEmpty() || errorBreakdown.newErrors.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    if (errorBreakdown.persistentErrors.isNotEmpty()) {
                        ErrorTypeChip(
                            label = "Persistent",
                            count = errorBreakdown.persistentErrors.size,
                            color = Color(0xFFFF6B35)
                        )
                    }
                    
                    if (errorBreakdown.newErrors.isNotEmpty()) {
                        ErrorTypeChip(
                            label = "New",
                            count = errorBreakdown.newErrors.size,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImprovementAreaItem(
    area: ImprovementArea
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = area.errorType.replace("_", " ").capitalize(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${area.frequency} occurrences",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        AssistChip(
            onClick = { },
            label = { 
                Text(
                    "${area.estimatedPracticeTime} min practice",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}

@Composable
private fun ErrorTypeChip(
    label: String,
    count: Int,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun NextStepsCard(
    nextSteps: List<String>,
    recommendations: List<Recommendation>
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Next Steps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            nextSteps.forEach { step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            if (recommendations.isNotEmpty()) {
                Divider(
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Text(
                    text = "Recommended Practice:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
                
                recommendations.take(2).forEach { rec ->
                    RecommendationChip(recommendation = rec)
                }
            }
        }
    }
}

@Composable
private fun RecommendationChip(
    recommendation: Recommendation
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                if (recommendation.estimatedTime > 0) {
                    Text(
                        text = "${recommendation.estimatedTime} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileProgressCard(
    updatedProfile: VerbalJournalProfile,
    progressMetrics: VerbalJournalMetrics
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProgressItem(
                    label = "Current Streak",
                    value = "${updatedProfile.streakDays} days",
                    icon = Icons.Default.LocalFireDepartment,
                    color = if (updatedProfile.streakDays > 0) Color(0xFFFF6B35) else Color.Gray
                )
                
                ProgressItem(
                    label = "Total Time",
                    value = "${updatedProfile.totalSpeakingMinutes} min",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.primary
                )
                
                ProgressItem(
                    label = "Weekly Goal",
                    value = "${(progressMetrics.sessionCompletionRate * 100).toInt()}%",
                    icon = Icons.Default.Flag,
                    color = if (progressMetrics.sessionCompletionRate >= 0.8f) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFFFC107)
                    }
                )
            }
        }
    }
}

@Composable
private fun ProgressItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getAccuracyColor(accuracy: Float): Color {
    return when {
        accuracy >= 0.9f -> Color(0xFF4CAF50)
        accuracy >= 0.7f -> Color(0xFFFFC107)
        else -> Color(0xFFFF5252)
    }
}

private fun getFluencyColor(fluency: Float): Color {
    return when {
        fluency >= 0.8f -> Color(0xFF4CAF50)
        fluency >= 0.6f -> Color(0xFFFFC107)
        else -> Color(0xFFFF5252)
    }
}