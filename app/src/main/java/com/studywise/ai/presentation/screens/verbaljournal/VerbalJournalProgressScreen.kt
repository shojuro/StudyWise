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
import com.studywise.ai.presentation.components.animations.SmoothTransitions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbalJournalProgressScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: VerbalJournalProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Speaking Progress",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProgressUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is ProgressUiState.Success -> {
                ProgressContent(
                    progress = state.progress,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            is ProgressUiState.Error -> {
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
private fun ProgressContent(
    progress: VerbalJournalProgress,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Overview
        ProfileOverviewCard(profile = progress.profile)
        
        // Key Metrics
        KeyMetricsSection(progress = progress)
        
        // Weekly Goal Progress
        WeeklyGoalCard(weeklyProgress = progress.weeklyGoalProgress)
        
        // Skill Breakdown
        SkillBreakdownCard(skills = progress.skillBreakdown)
        
        // Recent Performance Chart
        if (progress.recentPerformance.isNotEmpty()) {
            RecentPerformanceCard(performances = progress.recentPerformance)
        }
        
        // Achievements
        if (progress.achievements.isNotEmpty()) {
            AchievementsCard(achievements = progress.achievements)
        }
        
        // Next Milestones
        if (progress.nextMilestones.isNotEmpty()) {
            NextMilestonesCard(milestones = progress.nextMilestones)
        }
        
        // Improvement Trends
        ImprovementTrendsCard(trends = progress.improvementTrends)
    }
}

@Composable
private fun ProfileOverviewCard(
    profile: VerbalJournalProfile
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Level",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = profile.currentLevel.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.currentLevel.cefrLevel,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStat(
                    label = "Native Language",
                    value = profile.nativeLanguage
                )
                ProfileStat(
                    label = "Correction Tier",
                    value = profile.currentTier.name
                )
                ProfileStat(
                    label = "Total Hours",
                    value = "${profile.totalSpeakingMinutes / 60}"
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
private fun KeyMetricsSection(
    progress: VerbalJournalProgress
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Streak",
            value = "${progress.currentStreak}",
            subtitle = "days",
            icon = Icons.Default.LocalFireDepartment,
            color = if (progress.currentStreak > 0) Color(0xFFFF6B35) else Color.Gray,
            trend = if (progress.currentStreak > progress.longestStreak * 0.8f) "↑" else null
        )
        
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Accuracy",
            value = "${(progress.averageAccuracy * 100).toInt()}%",
            subtitle = "average",
            icon = Icons.Default.CheckCircle,
            color = when {
                progress.averageAccuracy >= 0.9f -> Color(0xFF4CAF50)
                progress.averageAccuracy >= 0.7f -> Color(0xFFFFC107)
                else -> Color(0xFFFF5252)
            },
            trend = if (progress.improvementTrends.overallImprovement > 0) "↑" else "↓"
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Sessions",
            value = "${progress.completedSessions}",
            subtitle = "completed",
            icon = Icons.Default.PlayCircle,
            color = MaterialTheme.colorScheme.primary
        )
        
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Time",
            value = "${progress.totalMinutesSpoken}",
            subtitle = "minutes",
            icon = Icons.Default.Timer,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    trend: String? = null
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                trend?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (it == "↑") Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                }
            }
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyGoalCard(
    weeklyProgress: WeeklyGoalProgress
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Weekly Goal",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${weeklyProgress.completedMinutes} / ${weeklyProgress.targetMinutes}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "minutes this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${weeklyProgress.daysActive} / ${weeklyProgress.targetDays}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "days active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            LinearProgressIndicator(
                progress = { weeklyProgress.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    weeklyProgress.progress >= 1f -> Color(0xFF4CAF50)
                    weeklyProgress.progress >= 0.7f -> MaterialTheme.colorScheme.primary
                    else -> Color(0xFFFFC107)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            if (weeklyProgress.progress >= 1f) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Goal achieved!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillBreakdownCard(
    skills: Map<String, SkillLevel>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Skill Breakdown",
                style = MaterialTheme.typography.titleMedium
            )
            
            skills.forEach { (skillName, skillLevel) ->
                SkillItem(
                    name = skillName,
                    level = skillLevel
                )
            }
        }
    }
}

@Composable
private fun SkillItem(
    name: String,
    level: SkillLevel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = level.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = getSkillColor(level.score)
                )
                Text(
                    text = "${(level.score * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        LinearProgressIndicator(
            progress = { level.score },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = getSkillColor(level.score),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun RecentPerformanceCard(
    performances: List<DailyPerformance>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recent Performance",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Simple chart placeholder - in production, use a charting library
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                performances.takeLast(7).forEach { performance ->
                    PerformanceBar(performance = performance)
                }
            }
        }
    }
}

@Composable
private fun PerformanceBar(
    performance: DailyPerformance
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(80.dp * performance.accuracy)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    getSkillColor(performance.accuracy)
                )
        )
        
        Text(
            text = SimpleDateFormat("EEE", Locale.getDefault()).format(performance.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AchievementsCard(
    achievements: List<Achievement>
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
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "${achievements.size} earned",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                achievements.take(5).forEach { achievement ->
                    AchievementBadge(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    achievement: Achievement
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                when (achievement.category) {
                    AchievementCategory.STREAK -> Color(0xFFFF6B35)
                    AchievementCategory.ACCURACY -> Color(0xFF4CAF50)
                    AchievementCategory.FLUENCY -> Color(0xFF2196F3)
                    AchievementCategory.MILESTONE -> Color(0xFFFFD700)
                    AchievementCategory.CHALLENGE -> Color(0xFF9C27B0)
                }.copy(alpha = 0.2f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            when (achievement.category) {
                AchievementCategory.STREAK -> Icons.Default.LocalFireDepartment
                AchievementCategory.ACCURACY -> Icons.Default.CheckCircle
                AchievementCategory.FLUENCY -> Icons.Default.Speed
                AchievementCategory.MILESTONE -> Icons.Default.EmojiEvents
                AchievementCategory.CHALLENGE -> Icons.Default.Star
            },
            contentDescription = achievement.name,
            modifier = Modifier.size(30.dp),
            tint = when (achievement.category) {
                AchievementCategory.STREAK -> Color(0xFFFF6B35)
                AchievementCategory.ACCURACY -> Color(0xFF4CAF50)
                AchievementCategory.FLUENCY -> Color(0xFF2196F3)
                AchievementCategory.MILESTONE -> Color(0xFFFFD700)
                AchievementCategory.CHALLENGE -> Color(0xFF9C27B0)
            }
        )
    }
}

@Composable
private fun NextMilestonesCard(
    milestones: List<Milestone>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Next Milestones",
                style = MaterialTheme.typography.titleMedium
            )
            
            milestones.forEach { milestone ->
                MilestoneItem(milestone = milestone)
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    milestone: Milestone
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = milestone.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${(milestone.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        LinearProgressIndicator(
            progress = { milestone.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Text(
            text = "Reward: ${milestone.reward}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun ImprovementTrendsCard(
    trends: ImprovementTrends
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Improvement Trends",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TrendIndicator(
                    label = "Overall",
                    value = trends.overallImprovement,
                    isPercentage = true
                )
                
                TrendIndicator(
                    label = "Accuracy",
                    value = if (trends.accuracyTrend.size >= 2) {
                        trends.accuracyTrend.last().value - trends.accuracyTrend.first().value
                    } else 0f,
                    isPercentage = true
                )
                
                TrendIndicator(
                    label = "Fluency",
                    value = if (trends.fluencyTrend.size >= 2) {
                        trends.fluencyTrend.last().value - trends.fluencyTrend.first().value
                    } else 0f,
                    isPercentage = true
                )
            }
        }
    }
}

@Composable
private fun TrendIndicator(
    label: String,
    value: Float,
    isPercentage: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (value > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (value > 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
            )
            
            Text(
                text = if (isPercentage) {
                    "${if (value > 0) "+" else ""}${(value * 100).toInt()}%"
                } else {
                    "${if (value > 0) "+" else ""}${value.toInt()}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (value > 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getSkillColor(score: Float): Color {
    return when {
        score >= 0.9f -> Color(0xFF4CAF50)
        score >= 0.8f -> Color(0xFF8BC34A)
        score >= 0.7f -> Color(0xFFFFC107)
        score >= 0.6f -> Color(0xFFFF9800)
        else -> Color(0xFFFF5252)
    }
}