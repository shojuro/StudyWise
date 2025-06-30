package com.studywise.ai.presentation.screens.verbaljournal

import android.Manifest
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.presentation.components.animations.SmoothTransitions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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
    
    // Audio permission
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Initialize session
    LaunchedEffect(sessionId) {
        viewModel.initializeSession(sessionId)
    }
    
    // Handle permission
    LaunchedEffect(audioPermissionState.status) {
        if (!audioPermissionState.status.isGranted) {
            audioPermissionState.launchPermissionRequest()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Speaking Practice",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is SessionUiState.Active) {
                        TextButton(
                            onClick = { 
                                viewModel.endSession()
                                onSessionComplete(sessionId)
                            }
                        ) {
                            Text("End Session")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is SessionUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is SessionUiState.Active -> {
                if (audioPermissionState.status.isGranted) {
                    SessionContent(
                        state = state,
                        recordingState = recordingState,
                        transcriptionState = transcriptionState,
                        onToggleRecording = viewModel::toggleRecording,
                        onRetry = viewModel::retryRecording,
                        onSkipCorrections = viewModel::skipCorrections,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    PermissionRequiredContent(
                        onRequestPermission = { audioPermissionState.launchPermissionRequest() },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            
            is SessionUiState.Completed -> {
                // Navigate to results
                LaunchedEffect(Unit) {
                    onSessionComplete(sessionId)
                }
            }
            
            is SessionUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.initializeSession(sessionId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SessionContent(
    state: SessionUiState.Active,
    recordingState: RecordingState,
    transcriptionState: TranscriptionState,
    onToggleRecording: () -> Unit,
    onRetry: () -> Unit,
    onSkipCorrections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Session Timer
        SessionTimer(
            elapsedMinutes = state.elapsedMinutes,
            targetMinutes = state.targetMinutes
        )
        
        // Conversation Display
        ConversationDisplay(
            turns = state.conversationTurns,
            showCorrections = state.showCorrections
        )
        
        // Current AI Response
        if (state.currentAiResponse.isNotEmpty()) {
            AiResponseCard(
                response = state.currentAiResponse,
                isGenerating = state.isAiThinking
            )
        }
        
        // Transcription Display
        if (transcriptionState is TranscriptionState.Transcribing || 
            transcriptionState is TranscriptionState.Complete) {
            TranscriptionCard(
                state = transcriptionState,
                onRetry = onRetry
            )
        }
        
        // Error Corrections (if applicable)
        if (state.showCorrections && state.currentErrors.isNotEmpty()) {
            ErrorCorrectionsCard(
                errors = state.currentErrors,
                onSkip = onSkipCorrections
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Recording Controls
        RecordingControls(
            recordingState = recordingState,
            onToggleRecording = onToggleRecording,
            canRecord = !state.isAiThinking && 
                      transcriptionState !is TranscriptionState.Transcribing
        )
    }
}

@Composable
private fun SessionTimer(
    elapsedMinutes: Int,
    targetMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$elapsedMinutes / $targetMinutes min",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            
            LinearProgressIndicator(
                progress = { elapsedMinutes.toFloat() / targetMinutes },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun ConversationDisplay(
    turns: List<ConversationTurn>,
    showCorrections: Boolean
) {
    if (turns.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Conversation",
                style = MaterialTheme.typography.titleMedium
            )
            
            turns.takeLast(5).forEach { turn ->
                ConversationTurnItem(
                    turn = turn,
                    showErrors = showCorrections && turn.speaker == Speaker.USER
                )
            }
        }
    }
}

@Composable
private fun ConversationTurnItem(
    turn: ConversationTurn,
    showErrors: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (turn.speaker == Speaker.USER) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (turn.speaker == Speaker.USER) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = turn.transcript,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (turn.speaker == Speaker.USER) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                
                if (showErrors && turn.errors.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF6B35)
                        )
                        Text(
                            text = "${turn.errors.size} corrections",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF6B35)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiResponseCard(
    response: String,
    isGenerating: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (isGenerating) "AI is thinking..." else "AI Assistant",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Text(
                text = response,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun TranscriptionCard(
    state: TranscriptionState,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            width = 2.dp,
            color = when (state) {
                is TranscriptionState.Transcribing -> MaterialTheme.colorScheme.primary
                is TranscriptionState.Complete -> Color.Transparent
                else -> Color.Transparent
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state) {
                is TranscriptionState.Transcribing -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Transcribing your speech...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                is TranscriptionState.Complete -> {
                    Text(
                        text = "You said:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.transcript,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Confidence: ${(state.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (state.confidence < 0.8f) {
                            TextButton(onClick = onRetry) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                else -> {}
            }
        }
    }
}

@Composable
private fun ErrorCorrectionsCard(
    errors: List<ErrorInstance>,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Corrections",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                TextButton(onClick = onSkip) {
                    Text("Skip")
                }
            }
            
            errors.take(3).forEach { error ->
                ErrorCorrectionItem(error = error)
            }
        }
    }
}

@Composable
private fun ErrorCorrectionItem(
    error: ErrorInstance
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFF5252)
            )
            Text(
                text = error.originalText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFF5252)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF4CAF50)
            )
            Text(
                text = error.correctedText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = error.explanation,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}

@Composable
private fun RecordingControls(
    recordingState: RecordingState,
    onToggleRecording: () -> Unit,
    canRecord: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recording button
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (recordingState is RecordingState.Recording) {
                Box(
                    modifier = Modifier
                        .size(120.dp * pulseScale)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        )
                )
            }
            
            FilledIconButton(
                onClick = onToggleRecording,
                modifier = Modifier.size(80.dp),
                enabled = canRecord,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = when (recordingState) {
                        is RecordingState.Recording -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Icon(
                    imageVector = when (recordingState) {
                        is RecordingState.Recording -> Icons.Default.Stop
                        else -> Icons.Default.Mic
                    },
                    contentDescription = when (recordingState) {
                        is RecordingState.Recording -> "Stop recording"
                        else -> "Start recording"
                    },
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        // Recording status
        AnimatedContent(
            targetState = recordingState,
            label = "recording status"
        ) { state ->
            when (state) {
                is RecordingState.Idle -> {
                    Text(
                        text = "Tap to speak",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is RecordingState.Recording -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Recording...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${state.duration}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is RecordingState.Processing -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit,
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
            Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Microphone Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "To practice speaking, we need access to your microphone.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
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
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}