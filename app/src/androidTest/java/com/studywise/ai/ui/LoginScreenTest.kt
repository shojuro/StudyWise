package com.studywise.ai.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.presentation.screens.auth.LoginScreen
import com.studywise.ai.presentation.screens.auth.LoginUiState
import com.studywise.ai.presentation.screens.auth.LoginViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var viewModel: LoginViewModel
    private lateinit var uiState: MutableStateFlow<LoginUiState>
    
    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        uiState = MutableStateFlow(LoginUiState())
        every { viewModel.uiState } returns uiState
    }
    
    @Test
    fun loginScreen_InitialState_DisplaysCorrectly() {
        // Given/When
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Welcome Back!")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Sign in to continue your learning journey")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Email")
            .assertIsDisplayed()
            .assertTextContains("")
        
        composeTestRule
            .onNodeWithContentDescription("Password")
            .assertIsDisplayed()
            .assertTextContains("")
        
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsDisplayed()
            .assertIsEnabled()
        
        composeTestRule
            .onNodeWithText("Don't have an account? Sign Up")
            .assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_EnterCredentials_UpdatesViewModel() {
        // Given
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithContentDescription("Email")
            .performTextInput("test@example.com")
        
        composeTestRule
            .onNodeWithContentDescription("Password")
            .performTextInput("password123")
        
        // Then
        verify {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("password123")
        }
    }
    
    @Test
    fun loginScreen_ClickSignIn_CallsLogin() {
        // Given
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithText("Sign In")
            .performClick()
        
        // Then
        verify { viewModel.login() }
    }
    
    @Test
    fun loginScreen_ShowsLoadingState() {
        // Given
        uiState.value = LoginUiState(isLoading = true)
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithTag("LoadingIndicator")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsNotEnabled()
    }
    
    @Test
    fun loginScreen_ShowsEmailError() {
        // Given
        uiState.value = LoginUiState(
            email = "invalid-email",
            emailError = "Invalid email format"
        )
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Invalid email format")
            .assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_ShowsPasswordError() {
        // Given
        uiState.value = LoginUiState(
            password = "123",
            passwordError = "Password must be at least 6 characters"
        )
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Password must be at least 6 characters")
            .assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_ShowsGeneralError() {
        // Given
        uiState.value = LoginUiState(
            generalError = "Invalid credentials. Please try again."
        )
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Invalid credentials. Please try again.")
            .assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_SuccessfulLogin_CallsNavigationCallback() {
        // Given
        var loginSuccessCalled = false
        var userRole: UserRole? = null
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = { role ->
                    loginSuccessCalled = true
                    userRole = role
                },
                viewModel = viewModel
            )
        }
        
        // When
        uiState.value = LoginUiState(
            loginSuccess = true,
            userRole = UserRole.STUDENT
        )
        
        // Then
        composeTestRule.waitUntil(5000) { loginSuccessCalled }
        assert(loginSuccessCalled)
        assert(userRole == UserRole.STUDENT)
    }
    
    @Test
    fun loginScreen_ClickSignUp_CallsNavigationCallback() {
        // Given
        var navigateToRegisterCalled = false
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = { navigateToRegisterCalled = true },
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithText("Don't have an account? Sign Up")
            .performClick()
        
        // Then
        assert(navigateToRegisterCalled)
    }
    
    @Test
    fun loginScreen_PasswordVisibilityToggle_Works() {
        // Given
        uiState.value = LoginUiState(password = "secret123")
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // Initially password should be hidden
        composeTestRule
            .onNodeWithContentDescription("Password")
            .assert(hasTextExactly("••••••••••", includeEditableText = false))
        
        // When - Click visibility toggle
        composeTestRule
            .onNodeWithContentDescription("Toggle password visibility")
            .performClick()
        
        // Then - Password should be visible
        composeTestRule
            .onNodeWithContentDescription("Password")
            .assert(hasTextExactly("secret123", includeEditableText = false))
        
        // When - Click again
        composeTestRule
            .onNodeWithContentDescription("Toggle password visibility")
            .performClick()
        
        // Then - Password should be hidden again
        composeTestRule
            .onNodeWithContentDescription("Password")
            .assert(hasTextExactly("••••••••••", includeEditableText = false))
    }
    
    @Test
    fun loginScreen_DemoLogin_ShowsDemoOptions() {
        // Given
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // Then - Demo section should be visible
        composeTestRule
            .onNodeWithText("Demo Accounts")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Try as Student")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Try as Parent")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Try as Teacher")
            .assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_DemoStudentLogin_FillsCredentials() {
        // Given
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToRegister = {},
                onLoginSuccess = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithText("Try as Student")
            .performClick()
        
        // Then
        verify {
            viewModel.loginAsDemo(UserRole.STUDENT)
        }
    }
}