package com.studywise.ai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.studywise.ai.data.local.DatabaseInitializerV2
import com.studywise.ai.data.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class StudyWiseApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var databaseInitializer: DatabaseInitializerV2
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize database with skills and questions
        applicationScope.launch {
            try {
                databaseInitializer.initializeDatabase()
                Timber.d("Database initialized successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize database")
            }
        }
        
        // Start periodic sync if user is logged in
        // This will be called from login/registration success
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    fun startDataSync() {
        syncManager.startPeriodicSync()
    }
    
    fun stopDataSync() {
        syncManager.stopPeriodicSync()
    }
}