package com.studywise.ai.presentation.screens.session

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.presentation.components.AccessibleButton
import com.studywise.ai.presentation.components.AccessibleTextField
import com.studywise.ai.presentation.components.session.InputMethodSelector
import com.studywise.ai.presentation.components.session.VoiceInputDisplay
import com.studywise.ai.util.textextraction.InputMethod
import com.studywise.ai.util.textextraction.VoiceTextCapture
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LearningSessionScreen(
    subject: String,
    onNavigateBack: () -> Unit,
    viewModel: LearningSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("$subject Session")
                        if (!uiState.waitingForBookText) {
                            Text(
                                text = "Question ${uiState.questionsCompleted + 1} of ${uiState.totalQuestions}",
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
                    if (!uiState.waitingForBookText && !uiState.isSessionComplete) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isSessionComplete -> {
                    SessionCompleteContent(
                        pointsEarned = uiState.pointsEarned,
                        questionsCompleted = uiState.questionsCompleted,
                        totalQuestions = uiState.totalQuestions,
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
                    QuestionContent(
                        uiState = uiState,
                        onAnswerChange = viewModel::onAnswerChange,
                        onSubmitAnswer = viewModel::submitAnswer,
                        onRequestHint = viewModel::requestHint,
                        onContinue = viewModel::continueToNext
                    )
                }
            }

            // Progress bar at bottom
            if (!uiState.waitingForBookText && !uiState.isSessionComplete) {
                LinearProgressIndicator(
                    progress = { uiState.sessionProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BookTextInputContent(
    uiState: LearningSessionUiState,
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
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
    
    // Handle permission results
    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status.isGranted && 
            uiState.selectedInputMethod == InputMethod.CAMERA) {
            photoLauncher.launch("image/*")
        }
    }
    
    LaunchedEffect(audioPermissionState.status) {
        if (audioPermissionState.status.isGranted && 
            uiState.selectedInputMethod == InputMethod.VOICE &&
            uiState.voiceState is VoiceTextCapture.VoiceState.Idle) {
            onStartVoiceCapture()
        }
    }
}

@Composable
fun QuestionContent(
    uiState: LearningSessionUiState,
    onAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onRequestHint: () -> Unit,
    onContinue: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Streak indicator
        if (uiState.currentStreak > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${uiState.currentStreak} streak!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Question
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = uiState.currentQuestion?.text ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics {
                        contentDescription = "Question: ${uiState.currentQuestion?.text}"
                    }
                )

                // Show hint if requested
                AnimatedVisibility(
                    visible = uiState.showHint,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.currentHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Answer input
        OutlinedTextField(
            value = uiState.userAnswer,
            onValueChange = onAnswerChange,
            label = { Text("Your answer") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isAnswerSubmitted && !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (!uiState.isAnswerSubmitted) {
                        onSubmitAnswer()
                    }
                }
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!uiState.isAnswerSubmitted && !uiState.showHint) {
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
                    Text("Hint")
                }
            }

            AccessibleButton(
                onClick = if (uiState.isAnswerSubmitted) onContinue else onSubmitAnswer,
                text = if (uiState.isAnswerSubmitted) "Continue" else "Submit",
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && (uiState.userAnswer.isNotBlank() || uiState.isAnswerSubmitted),
                isLoading = uiState.isLoading
            )
        }

        // Feedback
        AnimatedVisibility(
            visible = uiState.feedback.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.feedback.startsWith("Excellent")) {
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (uiState.feedback.startsWith("Excellent")) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (uiState.feedback.startsWith("Excellent")) {
                            Color(0xFF4CAF50)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.feedback,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SessionCompleteContent(
    pointsEarned: Int,
    questionsCompleted: Int,
    totalQuestions: Int,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Points Earned",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = pointsEarned.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$questionsCompleted of $totalQuestions questions completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AccessibleButton(
            onClick = onNavigateBack,
            text = "Back to Dashboard",
            modifier = Modifier.fillMaxWidth()
        )
    }
}