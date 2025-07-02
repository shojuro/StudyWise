package com.studywise.ai.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.studywise.ai.data.local.dao.SyncDao
import com.studywise.ai.data.local.database.StudyWiseDatabase
import com.studywise.ai.data.remote.api.StudyWiseApi
import com.studywise.ai.domain.repository.*
import com.studywise.ai.domain.service.ConflictResolver
import com.studywise.ai.domain.service.DefaultConflictResolver
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for SyncRepository
 */
@RunWith(RobolectricTestRunner::class)
class SyncRepositoryTest {

    @MockK
    private lateinit var api: StudyWiseApi

    private lateinit var database: StudyWiseDatabase
    private lateinit var syncDao: SyncDao
    private lateinit var conflictResolver: ConflictResolver
    private lateinit var syncRepository: SyncRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudyWiseDatabase::class.java
        ).allowMainThreadQueries().build()

        syncDao = database.syncDao()
        conflictResolver = DefaultConflictResolver()
        syncRepository = SyncRepositoryImpl(syncDao, api, conflictResolver)
    }

    @After
    fun tearDown() {
        database.close()
        unmockkAll()
    }

    @Test
    fun `test enqueueSyncOperation adds operation to queue`() = runTest {
        // Given
        val operation = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        // When
        syncRepository.enqueueSyncOperation(operation)

        // Then
        val pendingOperations = syncRepository.getPendingSyncOperations()
        assertEquals(1, pendingOperations.size)
        assertEquals(operation.id, pendingOperations[0].id)
        assertEquals(operation.entityType, pendingOperations[0].entityType)
        assertEquals(operation.entityId, pendingOperations[0].entityId)
    }

    @Test
    fun `test enqueueSyncOperation replaces existing pending operation for same entity`() = runTest {
        // Given
        val operation1 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        val operation2 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.UPDATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "Jane Doe"}""",
            timestamp = System.currentTimeMillis() + 1000
        )

        // When
        syncRepository.enqueueSyncOperation(operation1)
        syncRepository.enqueueSyncOperation(operation2)

        // Then
        val pendingOperations = syncRepository.getPendingSyncOperations()
        assertEquals(1, pendingOperations.size)
        assertEquals(operation2.id, pendingOperations[0].id)
        assertEquals(SyncOperationType.UPDATE, pendingOperations[0].type)
        assertEquals("""{"name": "Jane Doe"}""", pendingOperations[0].data)
    }

    @Test
    fun `test markOperationCompleted updates operation status`() = runTest {
        // Given
        val operation = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        syncRepository.enqueueSyncOperation(operation)

        // When
        syncRepository.markOperationCompleted(operation.id)

        // Then
        val pendingOperations = syncRepository.getPendingSyncOperations()
        assertTrue(pendingOperations.isEmpty())
    }

    @Test
    fun `test markOperationFailed updates operation with error`() = runTest {
        // Given
        val operation = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        syncRepository.enqueueSyncOperation(operation)

        // When
        syncRepository.markOperationFailed(operation.id, "Network error", true)

        // Then
        val retryableOperations = syncRepository.getRetryableOperations()
        assertEquals(1, retryableOperations.size)
        assertEquals(operation.id, retryableOperations[0].id)
        assertEquals("Network error", retryableOperations[0].error)
        assertEquals(1, retryableOperations[0].retryCount)
        assertTrue(retryableOperations[0].isRetryable)
    }

    @Test
    fun `test getRetryableOperations excludes max retry count operations`() = runTest {
        // Given
        val operation1 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis(),
            retryCount = 2 // Below max
        )

        val operation2 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user2",
            data = """{"name": "Jane Doe"}""",
            timestamp = System.currentTimeMillis(),
            retryCount = 3 // At max (MAX_RETRY_COUNT = 3)
        )

        syncRepository.enqueueSyncOperation(operation1)
        syncRepository.markOperationFailed(operation1.id, "Error", true)
        syncRepository.markOperationFailed(operation1.id, "Error", true) // Increment retry count

        syncRepository.enqueueSyncOperation(operation2)
        syncRepository.markOperationFailed(operation2.id, "Error", true)
        syncRepository.markOperationFailed(operation2.id, "Error", true)
        syncRepository.markOperationFailed(operation2.id, "Error", true) // Max retries

        // When
        val retryableOperations = syncRepository.getRetryableOperations()

        // Then
        assertEquals(1, retryableOperations.size)
        assertEquals(operation1.id, retryableOperations[0].id)
    }

    @Test
    fun `test resolveConflict with KeepLocal resolution`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = UUID.randomUUID().toString(),
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis() - 1000,
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // Add conflict to database
        syncDao.insertSyncConflict(conflict.toEntity())

        // When
        val result = syncRepository.resolveConflict(conflict, ConflictResolution.KeepLocal)

        // Then
        assertTrue(result.success)
        assertEquals(conflict.localData, result.resolvedData)

        // Should have a new sync operation queued
        val pendingOperations = syncRepository.getPendingSyncOperations()
        assertEquals(1, pendingOperations.size)
        assertEquals(SyncOperationType.UPDATE, pendingOperations[0].type)
        assertEquals("User", pendingOperations[0].entityType)
        assertEquals("user1", pendingOperations[0].entityId)
    }

    @Test
    fun `test getUnresolvedConflicts returns only unresolved conflicts`() = runTest {
        // Given
        val conflict1 = SyncConflict(
            id = UUID.randomUUID().toString(),
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name 1"}""",
            serverData = """{"name": "Server Name 1"}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.UPDATE_UPDATE
        )

        val conflict2 = SyncConflict(
            id = UUID.randomUUID().toString(),
            entityType = "User",
            entityId = "user2",
            localData = """{"name": "Local Name 2"}""",
            serverData = """{"name": "Server Name 2"}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // Add conflicts
        syncDao.insertSyncConflict(conflict1.toEntity())
        syncDao.insertSyncConflict(conflict2.toEntity())

        // Resolve one conflict
        syncDao.markConflictResolved(conflict1.id, "KeepLocal", conflict1.localData)

        // When
        val unresolvedConflicts = syncRepository.getUnresolvedConflicts()

        // Then
        assertEquals(1, unresolvedConflicts.size)
        assertEquals(conflict2.id, unresolvedConflicts[0].id)
    }

    @Test
    fun `test observeSyncQueueSize emits correct count`() = runTest {
        // Given
        val operation1 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        val operation2 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.UPDATE,
            entityType = "User",
            entityId = "user2",
            data = """{"name": "Jane Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        // When
        val initialSize = syncRepository.observeSyncQueueSize().first()
        assertEquals(0, initialSize)

        syncRepository.enqueueSyncOperation(operation1)
        val sizeAfterFirst = syncRepository.observeSyncQueueSize().first()
        assertEquals(1, sizeAfterFirst)

        syncRepository.enqueueSyncOperation(operation2)
        val sizeAfterSecond = syncRepository.observeSyncQueueSize().first()
        assertEquals(2, sizeAfterSecond)

        syncRepository.markOperationCompleted(operation1.id)
        val sizeAfterCompletion = syncRepository.observeSyncQueueSize().first()
        assertEquals(1, sizeAfterCompletion)
    }

    @Test
    fun `test getSyncStatistics returns correct counts`() = runTest {
        // Given
        val operation1 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        val operation2 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.UPDATE,
            entityType = "User",
            entityId = "user2",
            data = """{"name": "Jane Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        val conflict = SyncConflict(
            id = UUID.randomUUID().toString(),
            entityType = "User",
            entityId = "user3",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // Setup data
        syncRepository.enqueueSyncOperation(operation1)
        syncRepository.enqueueSyncOperation(operation2)
        syncRepository.markOperationCompleted(operation1.id)
        syncRepository.markOperationFailed(operation2.id, "Error", true)
        syncDao.insertSyncConflict(conflict.toEntity())

        // When
        val statistics = syncRepository.getSyncStatistics()

        // Then
        assertEquals(2, statistics.totalOperations)
        assertEquals(0, statistics.pendingOperations) // operation2 is failed, not pending
        assertEquals(1, statistics.completedOperations)
        assertEquals(1, statistics.failedOperations)
        assertEquals(1, statistics.conflicts)
    }

    @Test
    fun `test clearCompletedOperations removes only completed operations`() = runTest {
        // Given
        val operation1 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.CREATE,
            entityType = "User",
            entityId = "user1",
            data = """{"name": "John Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        val operation2 = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = SyncOperationType.UPDATE,
            entityType = "User",
            entityId = "user2",
            data = """{"name": "Jane Doe"}""",
            timestamp = System.currentTimeMillis()
        )

        syncRepository.enqueueSyncOperation(operation1)
        syncRepository.enqueueSyncOperation(operation2)
        syncRepository.markOperationCompleted(operation1.id)
        // operation2 remains pending

        // When
        syncRepository.clearCompletedOperations()

        // Then
        val statistics = syncRepository.getSyncStatistics()
        assertEquals(1, statistics.totalOperations) // Only pending operation remains
        assertEquals(1, statistics.pendingOperations)
        assertEquals(0, statistics.completedOperations)
    }
}

// Extension functions for test conversion
private fun SyncConflict.toEntity() = com.studywise.ai.data.local.entity.sync.SyncConflictEntity(
    id = id,
    entityType = entityType,
    entityId = entityId,
    localData = localData,
    serverData = serverData,
    localTimestamp = localTimestamp,
    serverTimestamp = serverTimestamp,
    conflictType = conflictType
)