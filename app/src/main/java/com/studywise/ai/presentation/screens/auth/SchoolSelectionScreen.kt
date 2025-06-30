package com.studywise.ai.presentation.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.presentation.components.AccessibleButton
import com.studywise.ai.presentation.components.AccessibleTextField
import com.studywise.ai.domain.model.School
import com.studywise.ai.domain.model.SchoolType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolSelectionScreen(
    onSchoolSelected: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SchoolSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Your School") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Help us customize your experience by selecting your school",
                style = MaterialTheme.typography.bodyLarge
            )

            // Search field
            AccessibleTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = "Search schools",
                leadingIcon = Icons.Default.Search,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.searchSchools()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // School type filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedType == SchoolType.ALL,
                    onClick = { viewModel.onSchoolTypeSelected(SchoolType.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.selectedType == SchoolType.PUBLIC,
                    onClick = { viewModel.onSchoolTypeSelected(SchoolType.PUBLIC) },
                    label = { Text("Public") }
                )
                FilterChip(
                    selected = uiState.selectedType == SchoolType.PRIVATE,
                    onClick = { viewModel.onSchoolTypeSelected(SchoolType.PRIVATE) },
                    label = { Text("Private") }
                )
                FilterChip(
                    selected = uiState.selectedType == SchoolType.HOMESCHOOL,
                    onClick = { viewModel.onSchoolTypeSelected(SchoolType.HOMESCHOOL) },
                    label = { Text("Homeschool") }
                )
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.schools.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                // No results
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No schools found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.onManualEntryClick() }
                        ) {
                            Text("Enter school manually")
                        }
                    }
                }
            } else {
                // School list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.schools) { school ->
                        SchoolListItem(
                            school = school,
                            isSelected = school.id == uiState.selectedSchool?.id,
                            onClick = { viewModel.onSchoolSelected(school) }
                        )
                    }
                }
            }

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onManualEntryClick() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enter Manually")
                    }

                    AccessibleButton(
                        onClick = {
                            viewModel.confirmSelection()
                            val schoolId = uiState.selectedSchool?.id ?: ""
                            onSchoolSelected(schoolId)
                        },
                        text = "Continue",
                        modifier = Modifier.weight(1f),
                        enabled = uiState.selectedSchool != null || uiState.manualSchoolName.isNotEmpty()
                    )
                }
                
                TextButton(
                    onClick = { onSchoolSelected("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for now")
                }
            }
        }
    }

    // Manual entry dialog
    if (uiState.showManualEntry) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissManualEntry() },
            title = { Text("Enter School Information") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.manualSchoolName,
                        onValueChange = viewModel::onManualSchoolNameChange,
                        label = { Text("School Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.manualSchoolCity,
                        onValueChange = viewModel::onManualSchoolCityChange,
                        label = { Text("City (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmManualEntry()
                        val schoolId = uiState.selectedSchool?.id ?: ""
                        onSchoolSelected(schoolId)
                    },
                    enabled = uiState.manualSchoolName.isNotEmpty()
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissManualEntry() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SchoolListItem(
    school: School,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (school.type) {
                    SchoolType.PUBLIC -> Icons.Default.School
                    SchoolType.PRIVATE -> Icons.Default.Business
                    SchoolType.HOMESCHOOL -> Icons.Default.Home
                    else -> Icons.Default.School
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = school.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (school.city.isNotEmpty() || school.state.isNotEmpty()) {
                    Text(
                        text = "${school.city}${if (school.city.isNotEmpty() && school.state.isNotEmpty()) ", " else ""}${school.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}