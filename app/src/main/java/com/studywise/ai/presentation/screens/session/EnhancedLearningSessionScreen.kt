package com.studywise.ai.presentation.screens.session

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.*
import com.studywise.ai.presentation.components.AccessibleButton
import com.studywise.ai.presentation.components.session.InputMethodSelector
import com.studywise.ai.presentation.components.session.VoiceInputDisplay
import com.studywise.ai.presentation.theme.*
import com.studywise.ai.util.textextraction.InputMethod
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Enhanced learning session screen with comprehensive educational content features
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EnhancedLearningSessionScreen(
    subject: String,
    onNavigateBack: () -> Unit,
    viewModel: EnhancedLearningSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            EnhancedTopBar(
                subject = subject,
                uiState = uiState,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentError = uiState.error
            when {
                currentError != null -> {
                    ErrorContent(
                        error = currentError,
                        onRetry = { viewModel.retrySession() }
                    )
                }
                uiState.isSessionComplete -> {
                    EnhancedSessionCompleteContent(
                        uiState = uiState,
                        onNavigateBack = onNavigateBack
                    )
                }
                uiState.waitingForBookText -> {
                    BookTextInputContent(
                        uiState = uiState,
                        onBookTextChange = viewModel::onBookTextChange,
                        onSubmit = viewModel::submitBookText,
                        onInputMethodSelected = viewModel::onInputMethodSelected,
                        onPhotoSelected = viewModel::onPhotoSelected,
                        onDocumentSelected = viewModel::onDocumentSelected,
                        onStartVoiceCapture = viewModel::startVoiceCapture,
                        onStopVoiceCapture = viewModel::stopVoiceCapture
                    )
                }
                else -> {
                    EnhancedQuestionContent(
                        uiState = uiState,
                        onAnswerChange = viewModel::onAnswerChange,
                        onSubmitAnswer = viewModel::submitAnswer,
                        onRequestHint = viewModel::requestHint,
                        onContinue = viewModel::continueToNext,
                        onAnswerFollowUp = viewModel::answerFollowUp
                    )
                }
            }

            // Enhanced progress indicator
            if (!uiState.waitingForBookText && !uiState.isSessionComplete) {
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    // Session type indicator
                    if (uiState.sessionType.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ) {
                            Text(
                                text = "Session Type: ${uiState.sessionType}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    LinearProgressIndicator(
                        progress = { uiState.sessionProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTopBar(
    subject: String,
    uiState: EnhancedLearningSessionUiState,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { 
            Column {
                Text(
                    text = "$subject Session",
                    style = MaterialTheme.typography.titleMedium
                )
                if (!uiState.waitingForBookText && uiState.currentSkill != null) {
                    Text(
                        text = "Skill: ${uiState.currentSkill.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
            if (!uiState.waitingForBookText && !uiState.isSessionComplete) {
                // Points and streak display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    // Streak indicator
                    if (uiState.currentStreak > 0) {
                        Surface(
                            color = Color(0xFFFF6B35),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.currentStreak}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B35)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    // Points
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.pointsEarned}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

@Composable
private fun EnhancedQuestionContent(
    uiState: EnhancedLearningSessionUiState,
    onAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onRequestHint: () -> Unit,
    onContinue: () -> Unit,
    onAnswerFollowUp: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Skill information card
        uiState.currentSkill?.let { skill ->
            SkillInfoCard(skill = skill)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Progress indicators
        QuestionProgressIndicator(
            current = uiState.questionsCompleted + 1,
            total = uiState.totalQuestions
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Main question card with enhanced styling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Question prompt
                Text(
                    text = uiState.currentQuestion?.prompt ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
                )

                // Show hints with animation
                AnimatedVisibility(
                    visible = uiState.showHint,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        HintCard(
                            hint = uiState.currentHint,
                            hintNumber = uiState.currentHintIndex,
                            totalHints = uiState.currentQuestion?.hints?.size ?: 0
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Answer input section
        if (!uiState.showFollowUp) {
            AnswerInputSection(
                userAnswer = uiState.userAnswer,
                isAnswerSubmitted = uiState.isAnswerSubmitted,
                isLoading = uiState.isLoading || uiState.isEvaluating,
                onAnswerChange = onAnswerChange,
                onSubmit = onSubmitAnswer,
                focusManager = focusManager
            )
        } else {
            // Follow-up question (Socratic method)
            FollowUpQuestionSection(
                followUpQuestion = uiState.currentQuestion?.followUpQuestions?.getOrNull(
                    uiState.currentFollowUpIndex
                ) ?: "",
                response = uiState.followUpResponse,
                onResponseChange = onAnswerFollowUp,
                onSubmit = onContinue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        ActionButtonsSection(
            uiState = uiState,
            onRequestHint = onRequestHint,
            onSubmitAnswer = onSubmitAnswer,
            onContinue = onContinue
        )

        // Feedback section with enhanced animation
        AnimatedVisibility(
            visible = uiState.feedback.isNotEmpty(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            FeedbackCard(
                feedback = uiState.feedback,
                isCorrect = uiState.feedback.startsWith("Great") || 
                           uiState.feedback.startsWith("Excellent")
            )
        }
    }
}

@Composable
private fun SkillInfoCard(skill: SkillInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (skill.category) {
                    "reading_literature" -> Icons.Outlined.MenuBook
                    "reading_informational" -> Icons.Outlined.Article
                    "writing" -> Icons.Outlined.Edit
                    "language_grammar" -> Icons.Outlined.Abc
                    "vocabulary_speaking" -> Icons.Outlined.RecordVoiceOver
                    else -> Icons.Outlined.School
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = skill.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuestionProgressIndicator(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isCompleted = index < current - 1
            val isCurrent = index == current - 1
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
            
            if (index < total - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun HintCard(hint: String, hintNumber: Int, totalHints: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Hint $hintNumber of $totalHints",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun AnswerInputSection(
    userAnswer: String,
    isAnswerSubmitted: Boolean,
    isLoading: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = userAnswer,
        onValueChange = onAnswerChange,
        label = { Text("Your answer") },
        placeholder = { Text("Type your thoughtful response here...") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isAnswerSubmitted && !isLoading,
        minLines = 3,
        maxLines = 5,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                if (!isAnswerSubmitted && userAnswer.isNotBlank()) {
                    onSubmit()
                }
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
    
    if (isLoading) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Evaluating your response...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FollowUpQuestionSection(
    followUpQuestion: String,
    response: String,
    onResponseChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Follow-up Question",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = followUpQuestion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = response,
                onValueChange = onResponseChange,
                placeholder = { Text("Expand on your thinking...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    uiState: EnhancedLearningSessionUiState,
    onRequestHint: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hint button
        if (!uiState.isAnswerSubmitted && 
            uiState.currentHintIndex < (uiState.currentQuestion?.hints?.size ?: 0)) {
            OutlinedButton(
                onClick = onRequestHint,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (uiState.showHint) "Next Hint" else "Get Hint"
                )
            }
        }

        // Submit/Continue button
        Button(
            onClick = if (uiState.isAnswerSubmitted) onContinue else onSubmitAnswer,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isLoading && !uiState.isEvaluating && 
                      (uiState.userAnswer.isNotBlank() || uiState.isAnswerSubmitted)
        ) {
            if (uiState.isEvaluating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (uiState.isAnswerSubmitted) "Continue" else "Submit"
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(feedback: String, isCorrect: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) {
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCorrect) {
                Color(0xFF4CAF50).copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isCorrect) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Info
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isCorrect) {
                    Color(0xFF4CAF50)
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EnhancedSessionCompleteContent(
    uiState: EnhancedLearningSessionUiState,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated trophy
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Amazing Work!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Stars,
                label = "Points Earned",
                value = uiState.pointsEarned.toString(),
                color = MaterialTheme.colorScheme.primary
            )
            
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                label = "Questions",
                value = "${uiState.questionsCompleted}/${uiState.totalQuestions}",
                color = Color(0xFF4CAF50)
            )
        }
        
        if (uiState.currentStreak > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            StatCard(
                modifier = Modifier.fillMaxWidth(0.5f),
                icon = Icons.Default.LocalFireDepartment,
                label = "Best Streak",
                value = "${uiState.currentStreak}",
                color = Color(0xFFFF6B35)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Skill mastery progress
        uiState.currentSkill?.let { skill ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Skill Progress",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { skill.masteryLevel },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(skill.masteryLevel * 100).toInt()}% Mastery",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
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
            text = "Oops! Something went wrong",
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BookTextInputContent(
    uiState: EnhancedLearningSessionUiState,
    onBookTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onInputMethodSelected: (InputMethod) -> Unit,
    onPhotoSelected: (android.net.Uri) -> Unit,
    onDocumentSelected: (android.net.Uri) -> Unit,
    onStartVoiceCapture: () -> Unit,
    onStopVoiceCapture: () -> Unit
) {
    // Permission states
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val audioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    
    // Launchers for photo and document selection
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { onPhotoSelected(it) }
    }
    
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { onDocumentSelected(it) }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle camera capture
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Outlined.Book,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Let's Start Learning!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Choose how to input your study material:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Input method selector
        InputMethodSelector(
            selectedMethod = uiState.selectedInputMethod,
            onMethodSelected = { method ->
                onInputMethodSelected(method)
                when (method) {
                    InputMethod.CAMERA -> {
                        if (cameraPermissionState.status.isGranted) {
                            photoLauncher.launch("image/*")
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
                    InputMethod.DOCUMENT -> {
                        documentLauncher.launch("*/*")
                    }
                    InputMethod.VOICE -> {
                        if (audioPermissionState.status.isGranted) {
                            onStartVoiceCapture()
                        } else {
                            audioPermissionState.launchPermissionRequest()
                        }
                    }
                    else -> {}
                }
            },
            isProcessing = uiState.isProcessingInput
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        // Show appropriate input UI based on selected method
        when (uiState.selectedInputMethod) {
            InputMethod.TEXT -> {
                OutlinedTextField(
                    value = uiState.bookText,
                    onValueChange = onBookTextChange,
                    label = { Text("Your book text") },
                    placeholder = { Text("Type or paste at least 50 characters...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    enabled = !uiState.isLoading && !uiState.isProcessingInput,
                    isError = uiState.bookTextError != null,
                    supportingText = uiState.bookTextError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit() }
                    ),
                    maxLines = 10
                )
            }
            InputMethod.VOICE -> {
                VoiceInputDisplay(
                    voiceState = uiState.voiceState,
                    transcribedText = uiState.bookText,
                    onStop = onStopVoiceCapture
                )
            }
            else -> {
                // For CAMERA and DOCUMENT, show the extracted text preview
                if (uiState.bookText.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "Extracted Text",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { onBookTextChange("") },
                                    enabled = !uiState.isProcessingInput
                                ) {
                                    Text("Clear")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.bookText,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 10
                            )
                        }
                    }
                }
            }
        }
        
        // Processing indicator
        if (uiState.isProcessingInput) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Processing...",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AccessibleButton(
            onClick = onSubmit,
            text = "Start Session",
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && 
                     !uiState.isProcessingInput && 
                     uiState.bookText.isNotBlank(),
            isLoading = uiState.isLoading
        )
        
        // Error message
        uiState.bookTextError?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}