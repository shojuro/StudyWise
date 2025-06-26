package com.studywise.ai.presentation.screens.progress

import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studywise.ai.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectProgressDetailScreen(
    subject: String,
    onNavigateBack: () -> Unit,
    viewModel: SubjectProgressDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(subject) {
        viewModel.loadSubjectProgress(subject)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$subject Progress") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportProgress() }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                }
            )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overview Stats
                item {
                    OverviewStatsRow(uiState.subjectProgress)
                }
                
                // Accuracy Trend Chart
                item {
                    AccuracyTrendChart(uiState.sessionHistory)
                }
                
                // Skill Breakdown
                item {
                    SkillBreakdownCard(uiState.subjectProgress?.skillMastery)
                }
                
                // Recent Sessions
                item {
                    RecentSessionsCard(uiState.sessionHistory)
                }
                
                // Learning Recommendations
                item {
                    LearningRecommendationsCard(uiState.recommendations)
                }
            }
        }
        
        // Export Progress Dialog
        if (uiState.showExportDialog) {
            ExportProgressDialog(
                onDismiss = { viewModel.dismissExportDialog() },
                onExport = { format -> viewModel.confirmExport(format) }
            )
        }
    }
}

@Composable
private fun OverviewStatsRow(subjectProgress: SubjectProgress?) {
    if (subjectProgress == null) return
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Total Sessions",
            value = subjectProgress.totalSessions.toString(),
            icon = Icons.Default.PlayCircle,
            color = MaterialTheme.colorScheme.primary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Avg Accuracy",
            value = "${(subjectProgress.averageAccuracy * 100).toInt()}%",
            icon = Icons.Default.CheckCircle,
            color = MaterialTheme.colorScheme.secondary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Study Time",
            value = "${subjectProgress.totalTimeMinutes / 60}h",
            icon = Icons.Default.Timer,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AccuracyTrendChart(sessions: List<LearningSession>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Accuracy Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (sessions.isNotEmpty()) {
                // Simple line chart implementation using Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val points = sessions.takeLast(10).mapIndexed { index, session ->
                        val x = (index.toFloat() / (sessions.takeLast(10).size - 1).coerceAtLeast(1)) * width
                        val y = height - (session.accuracy * height)
                        Offset(x, y)
                    }
                    
                    // Draw grid lines
                    for (i in 0..4) {
                        val y = height * i / 4
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // Draw accuracy line
                    if (points.size > 1) {
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = Color(0xFF1976D2),
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                    
                    // Draw points
                    points.forEach { point ->
                        drawCircle(
                            color = Color(0xFF1976D2),
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = point
                        )
                    }
                }
                
                // Show accuracy labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "50%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Recent sessions info
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last ${sessions.takeLast(10).size} sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    text = "No session data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun SkillBreakdownCard(skillMastery: Map<String, Float>?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Skill Mastery",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            skillMastery?.forEach { (skill, mastery) ->
                SkillProgressItem(skill, mastery)
                if (skill != skillMastery.keys.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SkillProgressItem(skill: String, mastery: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = skill.replace("_", " ").split(" ").joinToString(" ") { 
                    it.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${(mastery * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    mastery >= 0.8f -> MaterialTheme.colorScheme.primary
                    mastery >= 0.6f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { mastery },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = when {
                mastery >= 0.8f -> MaterialTheme.colorScheme.primary
                mastery >= 0.6f -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    }
}

@Composable
private fun RecentSessionsCard(sessions: List<LearningSession>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Sessions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            sessions.take(5).forEach { session ->
                SessionItem(session)
                if (session != sessions.take(5).last()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionItem(session: LearningSession) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(session.date),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${session.questionsAnswered} questions • ${session.minutesSpent} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${(session.accuracy * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    session.accuracy >= 0.8f -> MaterialTheme.colorScheme.primary
                    session.accuracy >= 0.6f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
            Text(
                text = "+${session.pointsEarned} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LearningRecommendationsCard(recommendations: List<String>) {
    if (recommendations.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportProgressDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Progress Report") },
        text = {
            Column {
                Text("Select export format:")
                Spacer(modifier = Modifier.height(16.dp))
                ExportFormat.values().forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExport(format) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = false,
                            onClick = { onExport(format) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(format.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class ExportFormat(val displayName: String) {
    PDF("PDF Report"),
    CSV("CSV Data"),
    JSON("JSON Data")
}

// Data classes for the screen
data class SubjectProgressDetailUiState(
    val isLoading: Boolean = true,
    val subjectProgress: SubjectProgress? = null,
    val sessionHistory: List<LearningSession> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val showExportDialog: Boolean = false,
    val error: String? = null
)

data class LearningSession(
    val date: Date,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val minutesSpent: Int,
    val pointsEarned: Int,
    val skills: List<String>
)