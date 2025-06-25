package com.studywise.ai.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.presentation.components.AccessibleButton
import com.studywise.ai.presentation.components.AccessiblePasswordTextField
import com.studywise.ai.presentation.components.AccessibleTextField
import com.studywise.ai.presentation.components.AccessibleTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.loginSuccess, uiState.userRole) {
        if (uiState.loginSuccess && uiState.userRole != null) {
            onLoginSuccess(uiState.userRole)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Welcome to StudyWise")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Login screen"
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your credentials to continue learning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AccessibleTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                enabled = !uiState.isLoading,
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AccessiblePasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                enabled = !uiState.isLoading,
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login()
                    }
                )
            )

            if (uiState.generalError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.generalError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AccessibleButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login()
                },
                text = "Sign In",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                AccessibleTextButton(
                    onClick = onNavigateToRegister,
                    text = "Sign Up",
                    enabled = !uiState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}