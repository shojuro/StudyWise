package com.studywise.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.presentation.StudyWiseApp
import com.studywise.ai.presentation.theme.StudyWiseTheme
import com.studywise.ai.presentation.theme.StudyWiseThemeSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var analyticsService: AnalyticsService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log app opened event
        analyticsService.logSessionStart()
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        
        setContent {
            val themeSettings by preferencesManager.userPreferences.collectAsState(
                initial = com.studywise.ai.data.local.preferences.UserPreferences()
            )
            
            StudyWiseTheme(
                themeSettings = StudyWiseThemeSettings(
                    themeMode = themeSettings.themeMode,
                    textScale = themeSettings.textSize,
                    highContrast = themeSettings.highContrast
                )
            ) {
                StudyWiseApp()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Log session end when app is closed
        analyticsService.logSessionEnd()
    }
}