package com.studywise.ai.presentation.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.presentation.components.AccessibleButton
import com.studywise.ai.presentation.components.AccessibleTextField
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    userRole: UserRole,
    onProfileComplete: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile") },
                actions = {
                    TextButton(
                        onClick = onProfileComplete,
                        enabled = !uiState.isLoading
                    ) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.profileImageUri),
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Add profile photo",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Camera icon
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Change photo",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Display name
            AccessibleTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = "Display Name",
                placeholder = "How should we address you?",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.displayNameError != null,
                errorMessage = uiState.displayNameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bio/About
            OutlinedTextField(
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                label = { Text("About Me") },
                placeholder = { Text("Tell us a bit about yourself...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role-specific fields
            when (userRole) {
                UserRole.STUDENT -> {
                    StudentProfileFields(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                UserRole.TEACHER -> {
                    TeacherProfileFields(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                UserRole.PARENT -> {
                    ParentProfileFields(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Notification preferences
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notification Preferences",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily study reminders")
                        Switch(
                            checked = uiState.enableDailyReminders,
                            onCheckedChange = viewModel::onDailyRemindersChange
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progress updates")
                        Switch(
                            checked = uiState.enableProgressUpdates,
                            onCheckedChange = viewModel::onProgressUpdatesChange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Complete button
            AccessibleButton(
                onClick = {
                    viewModel.saveProfile()
                    onProfileComplete()
                },
                text = "Complete Profile",
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.displayName.isNotEmpty() && !uiState.isLoading,
                isLoading = uiState.isLoading
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentProfileFields(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel
) {
    Column {
        // Favorite subjects
        Text(
            text = "Favorite Subjects",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val subjects = listOf("Math", "Science", "English", "History", "Art", "Music", "PE")
            subjects.forEach { subject ->
                FilterChip(
                    selected = subject in uiState.favoriteSubjects,
                    onClick = { viewModel.toggleFavoriteSubject(subject) },
                    label = { Text(subject) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Learning goals
        OutlinedTextField(
            value = uiState.learningGoals,
            onValueChange = viewModel::onLearningGoalsChange,
            label = { Text("Learning Goals") },
            placeholder = { Text("What do you want to achieve?") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TeacherProfileFields(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel
) {
    Column {
        // Subjects taught
        AccessibleTextField(
            value = uiState.subjectsTaught,
            onValueChange = viewModel::onSubjectsTaughtChange,
            label = "Subjects You Teach",
            placeholder = "e.g., Math, Science",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Years of experience
        AccessibleTextField(
            value = uiState.yearsExperience,
            onValueChange = viewModel::onYearsExperienceChange,
            label = "Years of Experience",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Teaching philosophy
        OutlinedTextField(
            value = uiState.teachingPhilosophy,
            onValueChange = viewModel::onTeachingPhilosophyChange,
            label = { Text("Teaching Philosophy") },
            placeholder = { Text("Your approach to education...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 3
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentProfileFields(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel
) {
    Column {
        // Number of children
        AccessibleTextField(
            value = uiState.numberOfChildren,
            onValueChange = viewModel::onNumberOfChildrenChange,
            label = "Number of Children",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Parental goals
        OutlinedTextField(
            value = uiState.parentalGoals,
            onValueChange = viewModel::onParentalGoalsChange,
            label = { Text("Educational Goals for Your Children") },
            placeholder = { Text("What do you hope to achieve?") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}