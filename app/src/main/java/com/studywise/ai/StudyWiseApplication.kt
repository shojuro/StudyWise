package com.studywise.ai

import android.app.Application
import com.studywise.ai.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class StudyWiseApplication : Application() {
    
    @Inject
    lateinit var databaseInitializer: DatabaseInitializer
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // For now, always plant debug tree. In production, use a crash reporting tree
        Timber.plant(DebugTree())
        
        // Initialize database with skills and questions
        applicationScope.launch {
            try {
                databaseInitializer.initializeDatabase()
                Timber.d("Database initialized successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize database")
            }
        }
    }
}