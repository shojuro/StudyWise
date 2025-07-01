package com.studywise.ai.presentation.screens.verbaljournal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.presentation.components.animations.SmoothTransitions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbalJournalSessionScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onSessionComplete: (String) -> Unit,
    viewModel: VerbalJournalSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val transcriptionState by viewModel.transcriptionState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            VerbalJournalTopBar(
                onNavigateBack = onNavigateBack,
                phase = uiState.currentPhase,
                isBreakInPeriod = uiState.isBreakInPeriod,
                weekInProgram = uiState.weekInProgram
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error,
                        onRetry = { viewModel.initializeSession(sessionId) }
                    )
                }
                uiState.showTopicSelection -> {
                    TopicSelectionContent(
                        sessionType = uiState.selectedSessionType,
                        suggestedTopics = uiState.suggestedTopics,
                        dailyPrompt = uiState.dailyPrompt,
                        onSelectSessionType = viewModel::selectSessionType,
                        onSelectTopic = viewModel::selectTopic
                    )
                }
                uiState.showResults -> {
                    SessionResultsContent(
                        analysis = uiState.sessionAnalysis,
                        recommendations = uiState.recommendations,
                        unlockedAchievements = uiState.unlockedAchievements,
                        onNavigateBack = onNavigateBack
                    )
                }
                else -> {
                    ConversationContent(
                        uiState = uiState,
                        recordingState = recordingState,
                        transcriptionState = transcriptionState,
                        onToggleRecording = viewModel::toggleRecording,
                        onRequestHint = viewModel::requestHint,
                        onSkipPhase = viewModel::skipToNextPhase,
                        onEndSession = viewModel::endSession
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerbalJournalTopBar(
    onNavigateBack: () -> Unit,
    phase: ConversationPhase,
    isBreakInPeriod: Boolean,
    weekInProgram: Int?
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Verbal Journal",
                    style = MaterialTheme.typography.titleMedium
                )
                if (isBreakInPeriod && weekInProgram != null) {
                    Text(
                        text = "Week $weekInProgram of Break-in Period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // Phase indicator
            Chip(
                onClick = { },
                modifier = Modifier.padding(end = 8.dp),
                colors = ChipDefaults.chipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = phase.name.replace("_", " ").lowercase().capitalize(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Setting up your session...")
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
private fun TopicSelectionContent(
    sessionType: SessionType?,
    suggestedTopics: List<String>,
    dailyPrompt: DailyPrompt?,
    onSelectSessionType: (SessionType) -> Unit,
    onSelectTopic: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Session",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Session type selection
        if (sessionType == null) {
            SessionTypeCard(
                type = SessionType.DAILY_PROMPT,
                title = "Daily Prompt",
                description = "Practice with today's guided topic",
                icon = Icons.Default.Today,
                onClick = { onSelectSessionType(SessionType.DAILY_PROMPT) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SessionTypeCard(
                type = SessionType.TOPIC_BASED,
                title = "Choose a Topic",
                description = "Select from suggested topics",
                icon = Icons.Default.Topic,
                onClick = { onSelectSessionType(SessionType.TOPIC_BASED) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SessionTypeCard(
                type = SessionType.FREE_CONVERSATION,
                title = "Free Conversation",
                description = "Talk about anything you like",
                icon = Icons.Default.Forum,
                onClick = { onSelectSessionType(SessionType.FREE_CONVERSATION) }
            )
        } else {
            // Show topics based on selected type
            when (sessionType) {
                SessionType.DAILY_PROMPT -> {
                    dailyPrompt?.let {
                        DailyPromptCard(
                            prompt = it,
                            onClick = { onSelectTopic(it.promptText) }
                        )
                    }
                }
                SessionType.TOPIC_BASED -> {
                    suggestedTopics.forEach { topic ->
                        TopicCard(
                            topic = topic,
                            onClick = { onSelectTopic(topic) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                SessionType.FREE_CONVERSATION -> {
                    // Auto-start free conversation
                    LaunchedEffect(Unit) {
                        onSelectTopic("Let's have a conversation!")
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionTypeCard(
    type: SessionType,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun DailyPromptCard(
    prompt: DailyPrompt,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Today's Prompt",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = prompt.promptText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Chip(
                    onClick = { },
                    colors = ChipDefaults.chipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = prompt.category.name.replace("_", " ").lowercase().capitalize(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Chip(
                    onClick = { },
                    colors = ChipDefaults.chipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "${prompt.estimatedDurationMinutes} min",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicCard(
    topic: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = topic,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ConversationContent(
    uiState: VerbalJournalSessionUiState,
    recordingState: RecordingState,
    transcriptionState: TranscriptionState,
    onToggleRecording: () -> Unit,
    onRequestHint: () -> Unit,
    onSkipPhase: () -> Unit,
    onEndSession: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Conversation history
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            uiState.conversationTurns.forEach { turn ->
                ConversationTurnItem(
                    turn = turn,
                    showErrors = turn.role == ConversationRole.USER
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // AI thinking indicator
            if (uiState.isAIThinking) {
                AIThinkingIndicator()
            }
            
            // Scroll to bottom when new message
            LaunchedEffect(uiState.conversationTurns.size) {
                coroutineScope.launch {
                    delay(100)
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }
        }
        
        // Current hint
        AnimatedVisibility(
            visible = uiState.currentHint != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            HintCard(
                hint = uiState.currentHint ?: "",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Recording controls
        RecordingControlsSection(
            recordingState = recordingState,
            transcriptionState = transcriptionState,
            isBreakInPeriod = uiState.isBreakInPeriod,
            onToggleRecording = onToggleRecording,
            onRequestHint = onRequestHint,
            onSkipPhase = onSkipPhase,
            onEndSession = onEndSession,
            suggestEndSession = uiState.suggestEndSession
        )
    }
}

@Composable
private fun ConversationTurnItem(
    turn: ConversationTurn,
    showErrors: Boolean
) {
    val isUser = turn.role == ConversationRole.USER
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = turn.transcription,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Show metrics for user turns
                if (isUser && turn.metrics != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricChip(
                            label = "WPM",
                            value = "${turn.metrics.wordsPerMinute.toInt()}"
                        )
                        MetricChip(
                            label = "Fluency",
                            value = "${(turn.metrics.fluencyScore * 100).toInt()}%"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AIThinkingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                            )
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HintCard(
    hint: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun RecordingControlsSection(
    recordingState: RecordingState,
    transcriptionState: TranscriptionState,
    isBreakInPeriod: Boolean,
    onToggleRecording: () -> Unit,
    onRequestHint: () -> Unit,
    onSkipPhase: () -> Unit,
    onEndSession: () -> Unit,
    suggestEndSession: Boolean
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Transcription preview
            AnimatedVisibility(
                visible = transcriptionState.isTranscribing || transcriptionState.transcribedText.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (transcriptionState.isTranscribing) {
                            Text(
                                text = "Transcribing...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (transcriptionState.transcribedText.isNotEmpty()) {
                            Text(
                                text = transcriptionState.transcribedText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Main controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hint button
                IconButton(
                    onClick = onRequestHint,
                    enabled = recordingState == RecordingState.IDLE
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Get Hint",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Record button
                RecordButton(
                    recordingState = recordingState,
                    onClick = onToggleRecording
                )
                
                // Menu
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Skip to Next Phase") },
                            onClick = {
                                showMenu = false
                                onSkipPhase()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("End Session") },
                            onClick = {
                                showMenu = false
                                onEndSession()
                            }
                        )
                    }
                }
            }
            
            // Session end suggestion
            AnimatedVisibility(
                visible = suggestEndSession,
                enter = slideInVertically() + fadeIn()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Great job! You've reached your daily goal.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onEndSession) {
                            Text("End Session")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordButton(
    recordingState: RecordingState,
    onClick: () -> Unit
) {
    val isRecording = recordingState == RecordingState.RECORDING
    val isProcessing = recordingState == RecordingState.PROCESSING
    
    val animatedSize by animateDpAsState(
        targetValue = if (isRecording) 80.dp else 64.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        // Pulsing animation when recording
        if (isRecording) {
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(animatedSize * scale)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
            )
        }
        
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(animatedSize),
            enabled = !isProcessing,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isRecording) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun SessionResultsContent(
    analysis: SessionAnalysis?,
    recommendations: List<SessionRecommendation>,
    unlockedAchievements: List<Achievement>,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Performance summary
        analysis?.let {
            PerformanceSummaryCard(analysis = it)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Unlocked achievements
        if (unlockedAchievements.isNotEmpty()) {
            AchievementsCard(achievements = unlockedAchievements)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Recommendations
        if (recommendations.isNotEmpty()) {
            RecommendationsCard(recommendations = recommendations)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun PerformanceSummaryCard(analysis: SessionAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Metrics
            val metrics = listOf(
                "Fluency" to analysis.metrics.averageFluencyScore,
                "Pronunciation" to analysis.metrics.averagePronunciationScore,
                "Grammar" to analysis.metrics.averageGrammarScore,
                "Vocabulary" to analysis.metrics.averageVocabularyScore
            )
            
            metrics.forEach { (label, score) ->
                MetricRow(label = label, score = score)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Insights
            if (analysis.performanceInsights.strengths.isNotEmpty()) {
                Text(
                    text = "Strengths",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                analysis.performanceInsights.strengths.forEach { strength ->
                    Row {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strength,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    score: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        LinearProgressIndicator(
            progress = { score },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                score >= 0.8f -> Color(0xFF4CAF50)
                score >= 0.6f -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.error
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(score * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AchievementsCard(achievements: List<Achievement>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Achievements!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            achievements.forEach { achievement ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = when (achievement.tier) {
                            AchievementTier.PLATINUM -> Color(0xFFE5E4E2)
                            AchievementTier.GOLD -> Color(0xFFFFD700)
                            AchievementTier.SILVER -> Color(0xFFC0C0C0)
                            AchievementTier.BRONZE -> Color(0xFFCD7F32)
                            null -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = achievement.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = achievement.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<SessionRecommendation>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Recommended for You",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recommendations.take(3).forEach { recommendation ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = when (recommendation.type) {
                            RecommendationType.EXERCISE -> Icons.Default.FitnessCenter
                            RecommendationType.TOPIC -> Icons.Default.Topic
                            RecommendationType.TECHNIQUE -> Icons.Default.Psychology
                            RecommendationType.RESOURCE -> Icons.Default.MenuBook
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = recommendation.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = recommendation.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (recommendation != recommendations.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}