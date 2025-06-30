package com.studywise.ai.presentation.screens.verbaljournal

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.usecase.verbaljournal.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbalJournalHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (String) -> Unit,
    viewModel: VerbalJournalHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Session History",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Badge(
                            containerColor = if (filterState.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is HistoryUiState.Success -> {
                if (state.sessions.isEmpty()) {
                    EmptyHistoryContent(
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    HistoryContent(
                        sessions = state.sessions,
                        onSessionClick = { sessionId ->
                            viewModel.selectSession(sessionId)
                            onNavigateToSession(sessionId)
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            
            is HistoryUiState.Error -> {
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
    
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = filterState,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { filter ->
                viewModel.applyFilter(filter)
                showFilterDialog = false
            }
        )
    }
}

@Composable
private fun HistoryContent(
    sessions: List<SessionHistoryItem>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Group sessions by date
        val groupedSessions = sessions.groupBy { session ->
            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(session.date)
        }
        
        groupedSessions.forEach { (date, sessionsForDate) ->
            item {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(sessionsForDate) { session ->
                SessionHistoryCard(
                    session = session,
                    onClick = { onSessionClick(session.id) }
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(
    session: SessionHistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row
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
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(session.date),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                StatusChip(status = session.status)
            }
            
            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricItem(
                    icon = Icons.Default.Timer,
                    value = "${session.duration} min",
                    label = "Duration"
                )
                
                session.accuracy?.let { accuracy ->
                    MetricItem(
                        icon = Icons.Default.CheckCircle,
                        value = "${(accuracy * 100).toInt()}%",
                        label = "Accuracy",
                        valueColor = getAccuracyColor(accuracy)
                    )
                }
                
                MetricItem(
                    icon = Icons.Default.TextFields,
                    value = "${session.wordsSpoken}",
                    label = "Words"
                )
                
                if (session.errorCount > 0) {
                    MetricItem(
                        icon = Icons.Default.Edit,
                        value = "${session.errorCount}",
                        label = "Errors",
                        valueColor = Color(0xFFFF6B35)
                    )
                }
            }
            
            // Topics
            if (session.topics.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    session.topics.take(3).forEach { topic ->
                        AssistChip(
                            onClick = { },
                            label = { Text(topic, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
            
            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EngagementIndicator(level = session.engagementLevel)
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (session.hasAudio) {
                        Icon(
                            Icons.Default.AudioFile,
                            contentDescription = "Has audio",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (session.achievements.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Achievements",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Text(
                                text = "${session.achievements.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: SessionStatus
) {
    val (containerColor, contentColor, label) = when (status) {
        SessionStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Active"
        )
        SessionStatus.COMPLETED -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF4CAF50),
            "Completed"
        )
        SessionStatus.ANALYZED -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Analyzed"
        )
        SessionStatus.ABANDONED -> Triple(
            Color(0xFFFF5252).copy(alpha = 0.2f),
            Color(0xFFFF5252),
            "Abandoned"
        )
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EngagementIndicator(
    level: EngagementLevel
) {
    val (color, label) = when (level) {
        EngagementLevel.HIGH -> Color(0xFF4CAF50) to "High engagement"
        EngagementLevel.MEDIUM -> Color(0xFFFFC107) to "Medium engagement"
        EngagementLevel.LOW -> Color(0xFFFF9800) to "Low engagement"
        EngagementLevel.STRUGGLING -> Color(0xFFFF5252) to "Struggling"
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyHistoryContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Sessions Yet",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Complete your first speaking session to see your history here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    currentFilter: FilterState,
    onDismiss: () -> Unit,
    onApplyFilter: (SessionHistoryFilter) -> Unit
) {
    var minDuration by remember { mutableStateOf(currentFilter.filter.minDuration?.toString() ?: "") }
    var minAccuracy by remember { mutableStateOf(currentFilter.filter.minAccuracy?.let { (it * 100).toInt().toString() } ?: "") }
    var selectedStatus by remember { mutableStateOf(currentFilter.filter.status) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Sessions") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status filter
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedStatus == SessionStatus.ANALYZED,
                        onClick = { selectedStatus = SessionStatus.ANALYZED },
                        label = { Text("Analyzed") }
                    )
                    FilterChip(
                        selected = selectedStatus == SessionStatus.COMPLETED,
                        onClick = { selectedStatus = SessionStatus.COMPLETED },
                        label = { Text("Completed") }
                    )
                }
                
                // Duration filter
                OutlinedTextField(
                    value = minDuration,
                    onValueChange = { minDuration = it },
                    label = { Text("Min Duration (minutes)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Accuracy filter
                OutlinedTextField(
                    value = minAccuracy,
                    onValueChange = { minAccuracy = it },
                    label = { Text("Min Accuracy (%)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApplyFilter(
                        SessionHistoryFilter(
                            status = selectedStatus,
                            minDuration = minDuration.toIntOrNull(),
                            minAccuracy = minAccuracy.toFloatOrNull()?.div(100f)
                        )
                    )
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getAccuracyColor(accuracy: Float): Color {
    return when {
        accuracy >= 0.9f -> Color(0xFF4CAF50)
        accuracy >= 0.7f -> Color(0xFFFFC107)
        else -> Color(0xFFFF5252)
    }
}