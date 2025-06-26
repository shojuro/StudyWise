package com.studywise.ai.data.sync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    fun startPeriodicSync() {
        val workRequest = DataSyncWorker.buildWorkRequest()
        
        workManager.enqueueUniquePeriodicWork(
            DataSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        Timber.d("Periodic sync scheduled")
    }
    
    fun stopPeriodicSync() {
        workManager.cancelUniqueWork(DataSyncWorker.WORK_NAME)
        Timber.d("Periodic sync cancelled")
    }
    
    fun syncNow() {
        val workRequest = DataSyncWorker.buildOneTimeWorkRequest()
        
        workManager.enqueueUniqueWork(
            "${DataSyncWorker.WORK_NAME}_oneTime",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Timber.d("One-time sync started")
    }
    
    fun observeSyncStatus(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(DataSyncWorker.WORK_NAME)
    }
    
    fun isSyncInProgress(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(DataSyncWorker.WORK_NAME).get()
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }
}