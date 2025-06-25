package com.studywise.ai.presentation.screens.profile

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.presentation.components.AccessibleButton
import com.studywise.ai.presentation.components.AccessibleTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isEditMode) {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Profile Header
                ProfileHeader(
                    displayName = uiState.displayName,
                    email = uiState.email,
                    role = uiState.role,
                    memberSince = uiState.memberSince,
                    profileImageUrl = uiState.profileImageUrl,
                    isEditMode = uiState.isEditMode,
                    editDisplayName = uiState.editDisplayName,
                    onDisplayNameChange = viewModel::onDisplayNameChange
                )

                if (uiState.isEditMode) {
                    // Edit Mode Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.cancelEdit() },
                            enabled = !uiState.isSaving
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        AccessibleButton(
                            onClick = { viewModel.saveProfile() },
                            text = "Save",
                            enabled = !uiState.isSaving && uiState.editDisplayName.isNotBlank(),
                            isLoading = uiState.isSaving
                        )
                    }

                    uiState.saveError?.let { error ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    // Statistics Section
                    Text(
                        text = "Learning Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    StatisticsGrid(
                        totalSessions = uiState.totalSessions,
                        totalQuestions = uiState.totalQuestionsAnswered,
                        averageMastery = uiState.averageMastery,
                        currentStreak = uiState.currentStreak,
                        skillsMastered = uiState.skillsMastered,
                        totalPoints = uiState.totalPointsEarned
                    )

                    // Achievements Section
                    if (uiState.recentAchievements.isNotEmpty()) {
                        Text(
                            text = "Recent Achievements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )

                        uiState.recentAchievements.forEach { achievement ->
                            AchievementCard(achievement = achievement)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    displayName: String,
    email: String,
    role: String,
    memberSince: String,
    profileImageUrl: String?,
    isEditMode: Boolean,
    editDisplayName: String,
    onDisplayNameChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode) {
                AccessibleTextField(
                    value = editDisplayName,
                    onValueChange = onDisplayNameChange,
                    label = "Display Name",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (role) {
                    "STUDENT" -> MaterialTheme.colorScheme.primary
                    "TEACHER" -> MaterialTheme.colorScheme.tertiary
                    "PARENT" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = role.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = when (role) {
                        "STUDENT" -> MaterialTheme.colorScheme.onPrimary
                        "TEACHER" -> MaterialTheme.colorScheme.onTertiary
                        "PARENT" -> MaterialTheme.colorScheme.onSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Member since $memberSince",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatisticsGrid(
    totalSessions: Int,
    totalQuestions: Int,
    averageMastery: Float,
    currentStreak: Int,
    skillsMastered: Int,
    totalPoints: Int
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Sessions",
                value = totalSessions.toString(),
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Questions",
                value = totalQuestions.toString(),
                icon = Icons.Default.QuestionAnswer,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Avg Mastery",
                value = "${(averageMastery * 100).toInt()}%",
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Current Streak",
                value = "$currentStreak days",
                icon = Icons.Default.LocalFireDepartment,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Skills Mastered",
                value = skillsMastered.toString(),
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Points",
                value = totalPoints.toString(),
                icon = Icons.Default.Stars,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (achievement.iconName) {
                        "star" -> Icons.Default.Star
                        "calculate" -> Icons.Default.Calculate
                        "speed" -> Icons.Default.Speed
                        else -> Icons.Default.EmojiEvents
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = achievement.earnedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}