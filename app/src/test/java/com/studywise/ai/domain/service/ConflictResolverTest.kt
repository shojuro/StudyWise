package com.studywise.ai.domain.service

import com.studywise.ai.domain.repository.ConflictResolution
import com.studywise.ai.domain.repository.ConflictType
import com.studywise.ai.domain.repository.SyncConflict
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for ConflictResolver
 */
class ConflictResolverTest {

    private lateinit var conflictResolver: ConflictResolver

    @Before
    fun setup() {
        conflictResolver = DefaultConflictResolver()
    }

    @Test
    fun `test determineResolutionStrategy for UPDATE_UPDATE conflict with newer local`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name", "version": 2}""",
            serverData = """{"name": "Server Name", "version": 1}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis() - 1000,
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // When
        val resolution = conflictResolver.determineResolutionStrategy(conflict)

        // Then
        assertEquals(ConflictResolution.KeepLocal, resolution)
    }

    @Test
    fun `test determineResolutionStrategy for UPDATE_UPDATE conflict with newer server`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name", "version": 1}""",
            serverData = """{"name": "Server Name", "version": 2}""",
            localTimestamp = System.currentTimeMillis() - 1000,
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // When
        val resolution = conflictResolver.determineResolutionStrategy(conflict)

        // Then
        assertEquals(ConflictResolution.KeepServer, resolution)
    }

    @Test
    fun `test determineResolutionStrategy for UPDATE_DELETE conflict`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name", "version": 2}""",
            serverData = "", // Server deleted
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.UPDATE_DELETE
        )

        // When
        val resolution = conflictResolver.determineResolutionStrategy(conflict)

        // Then
        assertEquals(ConflictResolution.KeepServer, resolution)
    }

    @Test
    fun `test determineResolutionStrategy for DELETE_UPDATE conflict`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = "", // Local deleted
            serverData = """{"name": "Server Name", "version": 2}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.DELETE_UPDATE
        )

        // When
        val resolution = conflictResolver.determineResolutionStrategy(conflict)

        // Then
        assertEquals(ConflictResolution.KeepLocal, resolution)
    }

    @Test
    fun `test determineResolutionStrategy for CREATE_CREATE conflict`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name", "id": "user1"}""",
            serverData = """{"name": "Server Name", "id": "user1"}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.CREATE_CREATE
        )

        // When
        val resolution = conflictResolver.determineResolutionStrategy(conflict)

        // Then
        assertEquals(ConflictResolution.KeepBoth, resolution)
    }

    @Test
    fun `test applyResolution for KeepLocal`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = 12345L,
            serverTimestamp = 67890L,
            conflictType = ConflictType.UPDATE_UPDATE
        )
        val resolution = ConflictResolution.KeepLocal

        // When
        val result = conflictResolver.applyResolution(conflict, resolution)

        // Then
        assertEquals(conflict.localData, result.data)
        assertEquals("local", result.metadata["resolution"])
        assertEquals(12345L, result.metadata["timestamp"])
    }

    @Test
    fun `test applyResolution for KeepServer`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = 12345L,
            serverTimestamp = 67890L,
            conflictType = ConflictType.UPDATE_UPDATE
        )
        val resolution = ConflictResolution.KeepServer

        // When
        val result = conflictResolver.applyResolution(conflict, resolution)

        // Then
        assertEquals(conflict.serverData, result.data)
        assertEquals("server", result.metadata["resolution"])
        assertEquals(67890L, result.metadata["timestamp"])
    }

    @Test
    fun `test applyResolution for Merge`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = 12345L,
            serverTimestamp = 67890L,
            conflictType = ConflictType.UPDATE_UPDATE
        )
        val mergedData = """{"name": "Merged Name"}"""
        val resolution = ConflictResolution.Merge(mergedData)

        // When
        val result = conflictResolver.applyResolution(conflict, resolution)

        // Then
        assertEquals(mergedData, result.data)
        assertEquals("merged", result.metadata["resolution"])
        assertTrue((result.metadata["timestamp"] as Long) > 0)
    }

    @Test
    fun `test applyResolution for KeepBoth`() = runTest {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = 12345L,
            serverTimestamp = 67890L,
            conflictType = ConflictType.CREATE_CREATE
        )
        val resolution = ConflictResolution.KeepBoth

        // When
        val result = conflictResolver.applyResolution(conflict, resolution)

        // Then
        assertEquals(conflict.localData, result.data)
        assertEquals("both", result.metadata["resolution"])
        assertEquals(conflict.serverData, result.metadata["duplicate_data"])
    }

    @Test
    fun `test canAutoResolve for UPDATE_UPDATE with significant time difference`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = currentTime,
            serverTimestamp = currentTime - (10 * 60 * 1000), // 10 minutes ago
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // When
        val canAutoResolve = conflictResolver.canAutoResolve(conflict)

        // Then
        assertTrue(canAutoResolve)
    }

    @Test
    fun `test canAutoResolve for UPDATE_UPDATE with minimal time difference`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = currentTime,
            serverTimestamp = currentTime - (1 * 60 * 1000), // 1 minute ago
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // When
        val canAutoResolve = conflictResolver.canAutoResolve(conflict)

        // Then
        assertFalse(canAutoResolve)
    }

    @Test
    fun `test canAutoResolve for UPDATE_DELETE always returns true`() {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = "",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.UPDATE_DELETE
        )

        // When
        val canAutoResolve = conflictResolver.canAutoResolve(conflict)

        // Then
        assertTrue(canAutoResolve)
    }

    @Test
    fun `test canAutoResolve for CREATE_CREATE always returns false`() {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.CREATE_CREATE
        )

        // When
        val canAutoResolve = conflictResolver.canAutoResolve(conflict)

        // Then
        assertFalse(canAutoResolve)
    }

    @Test
    fun `test getResolutionOptions for UPDATE_UPDATE conflict`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = currentTime,
            serverTimestamp = currentTime - 1000,
            conflictType = ConflictType.UPDATE_UPDATE
        )

        // When
        val options = conflictResolver.getResolutionOptions(conflict)

        // Then
        assertEquals(3, options.size)
        assertTrue(options.any { it.resolution is ConflictResolution.KeepLocal && it.isRecommended })
        assertTrue(options.any { it.resolution is ConflictResolution.KeepServer && !it.isRecommended })
        assertTrue(options.any { it.resolution is ConflictResolution.Merge })
    }

    @Test
    fun `test getResolutionOptions for CREATE_CREATE conflict`() {
        // Given
        val conflict = SyncConflict(
            id = "conflict1",
            entityType = "User",
            entityId = "user1",
            localData = """{"name": "Local Name"}""",
            serverData = """{"name": "Server Name"}""",
            localTimestamp = System.currentTimeMillis(),
            serverTimestamp = System.currentTimeMillis(),
            conflictType = ConflictType.CREATE_CREATE
        )

        // When
        val options = conflictResolver.getResolutionOptions(conflict)

        // Then
        assertEquals(3, options.size)
        assertTrue(options.any { it.resolution is ConflictResolution.KeepBoth && it.isRecommended })
        assertTrue(options.any { it.resolution is ConflictResolution.KeepLocal && !it.isRecommended })
        assertTrue(options.any { it.resolution is ConflictResolution.KeepServer && !it.isRecommended })
    }
}