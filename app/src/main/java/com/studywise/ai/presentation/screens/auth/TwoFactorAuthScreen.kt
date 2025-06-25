package com.studywise.ai.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.presentation.components.AccessibleButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorAuthScreen(
    onVerificationSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: TwoFactorAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(6) { FocusRequester() } }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequesters[0].requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Two-Factor Authentication") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Enter Verification Code",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = when (uiState.verificationMethod) {
                    VerificationMethod.SMS -> "We've sent a 6-digit code to ${uiState.maskedPhoneNumber}"
                    VerificationMethod.EMAIL -> "We've sent a 6-digit code to ${uiState.maskedEmail}"
                    VerificationMethod.AUTHENTICATOR -> "Enter the 6-digit code from your authenticator app"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.otpCode.forEachIndexed { index, digit ->
                    OtpDigitField(
                        value = digit,
                        onValueChange = { newDigit ->
                            viewModel.onOtpDigitChange(index, newDigit)
                            if (newDigit.isNotEmpty() && index < 5) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        },
                        focusRequester = focusRequesters[index],
                        isError = uiState.otpError != null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Error message
            uiState.otpError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify button
            AccessibleButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.verifyCode(onSuccess = onVerificationSuccess)
                },
                text = "Verify",
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.otpCode.all { it.isNotEmpty() } && !uiState.isLoading,
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resend code
            if (uiState.canResend) {
                TextButton(
                    onClick = { viewModel.resendCode() }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resend Code")
                }
            } else {
                Text(
                    text = "Resend code in ${uiState.resendCountdown}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alternative methods
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
                        text = "Try another method",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (uiState.verificationMethod != VerificationMethod.SMS && uiState.hasSmsOption) {
                        TextButton(
                            onClick = { viewModel.switchToSms() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send code via SMS")
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    
                    if (uiState.verificationMethod != VerificationMethod.EMAIL) {
                        TextButton(
                            onClick = { viewModel.switchToEmail() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send code via Email")
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    
                    if (uiState.verificationMethod != VerificationMethod.AUTHENTICATOR && uiState.hasAuthenticatorOption) {
                        TextButton(
                            onClick = { viewModel.switchToAuthenticator() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Authenticator App")
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trust device option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.trustDevice,
                    onCheckedChange = viewModel::onTrustDeviceChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trust this device for 30 days",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun OtpDigitField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .size(48.dp)
            .border(
                width = 2.dp,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else if (value.isNotEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ),
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                innerTextField()
            }
        }
    )
}