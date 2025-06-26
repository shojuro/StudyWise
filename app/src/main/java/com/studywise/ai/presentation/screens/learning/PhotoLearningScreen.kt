package com.studywise.ai.presentation.screens.learning

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PhotoLearningScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotoLearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Permission for audio recording
    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Permission for camera
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.analyzePhoto(it) }
    }
    
    // Camera launcher
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { viewModel.analyzePhoto(it) }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Learn from Photos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.identifiedObject != null) {
                        IconButton(onClick = { viewModel.resetLesson() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Start Over")
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
                uiState.identifiedObject == null -> {
                    // Initial photo selection state
                    PhotoSelectionView(
                        onPickPhoto = { photoPickerLauncher.launch("image/*") },
                        onTakePhoto = {
                            if (cameraPermission.status.isGranted) {
                                tempPhotoUri = createTempPhotoUri(context)
                                tempPhotoUri?.let { cameraLauncher.launch(it) }
                            } else {
                                cameraPermission.launchPermissionRequest()
                            }
                        }
                    )
                }
                
                uiState.showGradeSelector -> {
                    // Grade selection for sentences
                    GradeSelectorView(
                        identifiedObject = uiState.identifiedObject!!,
                        gradedSentences = uiState.gradedSentences,
                        currentGrade = uiState.currentGrade,
                        onGradeSelected = viewModel::selectGrade,
                        onStartLesson = viewModel::startLesson
                    )
                }
                
                uiState.lessonStarted -> {
                    // Interactive Socratic lesson
                    SocraticLessonView(
                        lesson = uiState.socraticLesson,
                        conversationHistory = uiState.conversationHistory,
                        isRecording = uiState.isRecording,
                        isSpeaking = uiState.isSpeaking,
                        lessonComplete = uiState.lessonComplete,
                        hasRecordPermission = recordAudioPermission.status.isGranted,
                        onStartRecording = {
                            if (recordAudioPermission.status.isGranted) {
                                viewModel.startRecording()
                            } else {
                                recordAudioPermission.launchPermissionRequest()
                            }
                        },
                        onStopRecording = viewModel::stopRecording,
                        onTextInput = viewModel::onStudentResponse
                    )
                }
                
                else -> {
                    // Object identified, showing info
                    ObjectInfoView(
                        identifiedObject = uiState.identifiedObject!!,
                        gradedSentences = uiState.gradedSentences,
                        currentGrade = uiState.currentGrade,
                        onStartLesson = viewModel::startLesson
                    )
                }
            }
            
            // Loading overlay
            if (uiState.isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Analyzing photo...")
                        }
                    }
                }
            }
            
            // Manual entry dialog
            if (uiState.showManualEntry) {
                ManualObjectEntryDialog(
                    currentValue = uiState.manualObjectName,
                    onValueChange = viewModel::onManualObjectNameChange,
                    onSubmit = { viewModel.submitManualObject() },
                    onDismiss = { viewModel.dismissManualEntry() },
                    error = uiState.error
                )
            }
            
            // Error snackbar (only show if not showing manual entry)
            if (!uiState.showManualEntry) {
                uiState.error?.let { errorText ->
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { viewModel.resetLesson() }) {
                                Text("Try Again")
                            }
                        }
                    ) {
                        Text(errorText)
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoSelectionView(
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Take or choose a photo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "I'll identify what's in the photo and help you learn about it!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Take Photo")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onPickPhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose from Gallery")
        }
    }
}

@Composable
fun ObjectInfoView(
    identifiedObject: com.studywise.ai.domain.model.IdentifiedObject,
    gradedSentences: Map<Int, List<String>>,
    currentGrade: Int,
    onStartLesson: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = identifiedObject.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = identifiedObject.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = identifiedObject.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = identifiedObject.educationalValue,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                if (identifiedObject.confidence > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = identifiedObject.confidence,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Confidence: ${(identifiedObject.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStartLesson,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.School, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start 5-Minute Lesson")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSelectorView(
    identifiedObject: com.studywise.ai.domain.model.IdentifiedObject,
    gradedSentences: Map<Int, List<String>>,
    currentGrade: Int,
    onGradeSelected: (Int) -> Unit,
    onStartLesson: () -> Unit
) {
    var selectedGrade by remember { mutableStateOf(currentGrade) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Learning about: ${identifiedObject.name}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Select your grade level to see example sentences:",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Grade selector tabs
        ScrollableTabRow(
            selectedTabIndex = selectedGrade - 2,
            modifier = Modifier.fillMaxWidth()
        ) {
            (2..12).forEach { grade ->
                Tab(
                    selected = grade == selectedGrade,
                    onClick = { 
                        selectedGrade = grade
                        onGradeSelected(grade)
                    }
                ) {
                    Text(
                        text = "Grade $grade",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Display sentences for selected grade
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gradedSentences[selectedGrade]?.let { sentences ->
                    items(sentences) { sentence ->
                        Row {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(top = 6.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = sentence,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onStartLesson,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Interactive Lesson")
        }
    }
}

@Composable
fun SocraticLessonView(
    lesson: com.studywise.ai.domain.model.SocraticLesson?,
    conversationHistory: List<ConversationEntry>,
    isRecording: Boolean,
    isSpeaking: Boolean,
    lessonComplete: Boolean,
    hasRecordPermission: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTextInput: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Conversation history
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversationHistory.reversed()) { entry ->
                ChatBubble(
                    entry = entry,
                    isSpeaking = isSpeaking && entry == conversationHistory.lastOrNull()
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Input area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (lessonComplete) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lesson Complete! Great job!",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Type your answer...") },
                            modifier = Modifier.weight(1f),
                            enabled = !isRecording && !isSpeaking,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (textInput.isNotBlank()) {
                                            onTextInput(textInput)
                                            textInput = ""
                                        }
                                    },
                                    enabled = textInput.isNotBlank() && !isSpeaking
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send")
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Voice input button
                        FilledIconButton(
                            onClick = {
                                if (isRecording) {
                                    onStopRecording()
                                } else {
                                    onStartRecording()
                                }
                            },
                            enabled = hasRecordPermission && !isSpeaking,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isRecording) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording" else "Start Recording"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    entry: ConversationEntry,
    isSpeaking: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (entry.speaker == Speaker.STUDENT) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (entry.speaker == Speaker.STUDENT) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (entry.speaker == Speaker.AI) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Teacher",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (isSpeaking) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (entry.speaker == Speaker.STUDENT) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}

// Helper function to create temp photo URI
private fun createTempPhotoUri(context: android.content.Context): Uri? {
    return try {
        val tempFile = File.createTempFile(
            "photo_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualObjectEntryDialog(
    currentValue: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    error: String?
) {
    val commonObjects = listOf(
        "pen", "pencil", "paper", "notebook", "book", "eraser",
        "ruler", "scissors", "glue", "tape", "marker", "crayon",
        "backpack", "calculator", "clock", "keys", "water bottle"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Object Name") },
        text = {
            Column {
                error?.let { errorText ->
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Text(
                    text = "The AI couldn't identify the object. What is it?",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                TextField(
                    value = currentValue,
                    onValueChange = onValueChange,
                    label = { Text("Object name") },
                    placeholder = { Text("e.g., pen, book, clock") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Common objects:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonObjects.forEach { obj ->
                        SuggestionChip(
                            onClick = { onValueChange(obj) },
                            label = { Text(obj) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = currentValue.isNotBlank()
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}