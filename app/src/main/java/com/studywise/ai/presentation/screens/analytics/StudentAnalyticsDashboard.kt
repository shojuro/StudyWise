package com.studywise.ai.presentation.screens.analytics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.presentation.components.animations.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * Comprehensive analytics dashboard for student progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnalyticsDashboard(
    onNavigateBack: () -> Unit,
    viewModel: StudentAnalyticsViewModel = hiltViewModel()
) {
    val analyticsState by viewModel.analyticsState.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Analytics Dashboard",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Time range selector
                    TimeRangeSelector(
                        selectedRange = selectedTimeRange,
                        onRangeSelected = viewModel::updateTimeRange
                    )
                }
            )
        }
    ) { paddingValues ->
        when (analyticsState) {
            is AnalyticsState.Loading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }
            is AnalyticsState.Success -> {
                AnalyticsDashboardContent(
                    data = analyticsState.data,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is AnalyticsState.Error -> {
                ErrorState(
                    message = analyticsState.message,
                    onRetry = viewModel::refreshAnalytics,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsDashboardContent(
    data: AnalyticsData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Progress Card
        item {
            OverallProgressCard(data.overallProgress)
        }
        
        // Performance Metrics Grid
        item {
            PerformanceMetricsGrid(data.performanceMetrics)
        }
        
        // Skill Mastery Chart
        item {
            SkillMasteryChart(data.skillMastery)
        }
        
        // Learning Patterns
        item {
            LearningPatternsCard(data.learningPatterns)
        }
        
        // Subject Performance
        item {
            SubjectPerformanceCard(data.subjectPerformance)
        }
        
        // Achievements Showcase
        item {
            AchievementsShowcase(data.recentAchievements)
        }
        
        // Study Time Analysis
        item {
            StudyTimeAnalysis(data.studyTimeData)
        }
        
        // Recommendations
        item {
            PersonalizedRecommendations(data.recommendations)
        }
    }
}

@Composable
private fun OverallProgressCard(progress: OverallProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Overall Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated circular progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = progress.completionRate,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedCounter(
                        count = (progress.completionRate * 100).toInt(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Complete",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStat(
                    label = "Skills Mastered",
                    value = progress.skillsMastered.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
                ProgressStat(
                    label = "Current Streak",
                    value = "${progress.currentStreak} days",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFFF6B35)
                )
                ProgressStat(
                    label = "Total Points",
                    value = progress.totalPoints.toString(),
                    icon = Icons.Default.Star,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Composable
private fun ProgressStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
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
private fun PerformanceMetricsGrid(metrics: PerformanceMetrics) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MetricCard(
                title = "Accuracy Rate",
                value = "${(metrics.accuracyRate * 100).toInt()}%",
                trend = metrics.accuracyTrend,
                color = Color(0xFF4CAF50)
            )
        }
        item {
            MetricCard(
                title = "Avg Response Time",
                value = "${metrics.avgResponseTime}s",
                trend = -metrics.responseTimeTrend, // Negative because lower is better
                color = Color(0xFF2196F3)
            )
        }
        item {
            MetricCard(
                title = "Questions/Day",
                value = metrics.questionsPerDay.toString(),
                trend = metrics.questionsPerDayTrend,
                color = Color(0xFF9C27B0)
            )
        }
        item {
            MetricCard(
                title = "Improvement Rate",
                value = "+${(metrics.improvementRate * 100).toInt()}%",
                trend = metrics.improvementTrend,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    trend: Float,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TrendIndicator(trend = trend, color = color)
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TrendIndicator(trend: Float, color: Color) {
    val rotation by animateFloatAsState(
        targetValue = if (trend > 0) -45f else 45f,
        animationSpec = tween(500)
    )
    
    Icon(
        imageVector = Icons.Default.TrendingUp,
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .rotate(rotation),
        tint = if (trend > 0) color else Color.Red
    )
}

@Composable
private fun SkillMasteryChart(skillMastery: List<SkillMasteryData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Skill Mastery Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            skillMastery.take(5).forEach { skill ->
                SkillProgressBar(skill)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (skillMastery.size > 5) {
                TextButton(
                    onClick = { /* Navigate to full skills view */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("View All Skills")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillProgressBar(skill: SkillMasteryData) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = skill.skillName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(skill.mastery * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val animatedProgress by animateFloatAsState(
                targetValue = skill.mastery,
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }
        
        if (skill.recentImprovement > 0) {
            Text(
                text = "+${(skill.recentImprovement * 100).toInt()}% this week",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun LearningPatternsCard(patterns: LearningPatterns) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Learning Patterns",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Best learning time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Peak Performance Time",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = patterns.bestLearningTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Learning style distribution
            Text(
                text = "Learning Style Preferences",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            patterns.learningStyles.forEach { style ->
                LearningStyleBar(style)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LearningStyleBar(style: LearningStylePreference) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = style.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(100.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(style.preference)
                    .background(
                        when (style.name) {
                            "Visual" -> Color(0xFF4CAF50)
                            "Interactive" -> Color(0xFF2196F3)
                            "Reading" -> Color(0xFF9C27B0)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )
        }
        
        Text(
            text = "${(style.preference * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SubjectPerformanceCard(subjects: List<SubjectPerformance>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Subject Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjects) { subject ->
                    SubjectCard(subject)
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(subject: SubjectPerformance) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = subject.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = subject.icon,
                contentDescription = null,
                tint = subject.color,
                modifier = Modifier.size(40.dp)
            )
            
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            CircularProgressIndicator(
                progress = subject.progress,
                modifier = Modifier.size(60.dp),
                strokeWidth = 6.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = subject.color
            )
            
            Text(
                text = "${(subject.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = subject.color
            )
        }
    }
}

@Composable
private fun AchievementsShowcase(achievements: List<Achievement>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Achievements",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = { /* Navigate to all achievements */ }) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementBadge(achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    when (achievement.rarity) {
                        AchievementRarity.COMMON -> Color(0xFF9E9E9E)
                        AchievementRarity.RARE -> Color(0xFF2196F3)
                        AchievementRarity.EPIC -> Color(0xFF9C27B0)
                        AchievementRarity.LEGENDARY -> Color(0xFFFF9800)
                        AchievementRarity.MYTHIC -> Color(0xFFFF5722)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = achievement.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StudyTimeAnalysis(studyData: StudyTimeData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Study Time Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weekly heat map
            Text(
                text = "This Week's Activity",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            WeeklyHeatMap(studyData.weeklyData)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total study time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StudyTimeStat(
                    label = "Total This Week",
                    value = "${studyData.totalMinutesThisWeek / 60}h ${studyData.totalMinutesThisWeek % 60}m"
                )
                StudyTimeStat(
                    label = "Daily Average",
                    value = "${studyData.dailyAverage}m"
                )
                StudyTimeStat(
                    label = "Best Day",
                    value = studyData.mostProductiveDay
                )
            }
        }
    }
}

@Composable
private fun WeeklyHeatMap(weeklyData: List<DayStudyData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weeklyData.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = (day.minutes / 120f).coerceIn(0.1f, 1f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.minutes.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (day.minutes > 60) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = day.dayName.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StudyTimeStat(label: String, value: String) {
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
private fun PersonalizedRecommendations(recommendations: List<Recommendation>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Personalized Recommendations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            recommendations.forEach { recommendation ->
                RecommendationItem(recommendation)
                if (recommendation != recommendations.last()) {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun RecommendationItem(recommendation: Recommendation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = recommendation.icon,
            contentDescription = null,
            tint = recommendation.priority.color,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = recommendation.priority.color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = recommendation.priority.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = recommendation.priority.color
                    )
                }
            }
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (recommendation.actionable) {
                TextButton(
                    onClick = { /* Handle action */ },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Take Action")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        TextButton(
            onClick = { expanded = true }
        ) {
            Text(selectedRange.displayName)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimeRange.values().forEach { range ->
                DropdownMenuItem(
                    text = { Text(range.displayName) },
                    onClick = {
                        onRangeSelected(range)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading analytics...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Extension for GridCells
private object GridCells {
    fun Fixed(count: Int) = androidx.compose.foundation.lazy.grid.GridCells.Fixed(count)
}

@Composable
private fun LazyVerticalGrid(
    columns: androidx.compose.foundation.lazy.grid.GridCells,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: androidx.compose.foundation.lazy.grid.LazyGridScope.() -> Unit
) {
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}