package com.studywise.ai.domain.service

import com.studywise.ai.data.local.entity.base.SyncStatus
import com.studywise.ai.data.local.entity.base.VersionedEntity
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for EntityVersionManager
 */
class EntityVersionManagerTest {

    private lateinit var versionManager: EntityVersionManager

    @Before
    fun setup() {
        versionManager = DefaultEntityVersionManager()
    }

    @Test
    fun `test isServerVersionNewer with newer server version`() {
        // When
        val isNewer = versionManager.isServerVersionNewer(localVersion = 1, serverVersion = 2)

        // Then
        assertTrue(isNewer)
    }

    @Test
    fun `test isServerVersionNewer with older server version`() {
        // When
        val isNewer = versionManager.isServerVersionNewer(localVersion = 2, serverVersion = 1)

        // Then
        assertFalse(isNewer)
    }

    @Test
    fun `test isServerVersionNewer with same version`() {
        // When
        val isNewer = versionManager.isServerVersionNewer(localVersion = 1, serverVersion = 1)

        // Then
        assertFalse(isNewer)
    }

    @Test
    fun `test hasVersionConflict with pending local and newer server within conflict window`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val localEntity = createMockVersionedEntity(
            version = 1,
            lastModified = currentTime,
            syncStatus = SyncStatus.PENDING
        )

        // When
        val hasConflict = versionManager.hasVersionConflict(
            localEntity = localEntity,
            serverVersion = 2,
            serverLastModified = currentTime - (2 * 60 * 1000) // 2 minutes ago
        )

        // Then
        assertTrue(hasConflict)
    }

    @Test
    fun `test hasVersionConflict with synced local entity`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val localEntity = createMockVersionedEntity(
            version = 1,
            lastModified = currentTime,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val hasConflict = versionManager.hasVersionConflict(
            localEntity = localEntity,
            serverVersion = 2,
            serverLastModified = currentTime - (2 * 60 * 1000)
        )

        // Then
        assertFalse(hasConflict)
    }

    @Test
    fun `test hasVersionConflict with local changes outside conflict window`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val localEntity = createMockVersionedEntity(
            version = 1,
            lastModified = currentTime,
            syncStatus = SyncStatus.PENDING
        )

        // When
        val hasConflict = versionManager.hasVersionConflict(
            localEntity = localEntity,
            serverVersion = 2,
            serverLastModified = currentTime - (10 * 60 * 1000) // 10 minutes ago
        )

        // Then
        assertFalse(hasConflict)
    }

    @Test
    fun `test determineSyncAction with null local entity`() {
        // When
        val action = versionManager.determineSyncAction(
            localEntity = null,
            serverVersion = 1,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.CREATE_LOCAL, action)
    }

    @Test
    fun `test determineSyncAction with pending local and newer server`() {
        // Given
        val localEntity = createMockVersionedEntity(
            version = 1,
            syncStatus = SyncStatus.PENDING
        )

        // When
        val action = versionManager.determineSyncAction(
            localEntity = localEntity,
            serverVersion = 2,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.CONFLICT, action)
    }

    @Test
    fun `test determineSyncAction with pending local and older server`() {
        // Given
        val localEntity = createMockVersionedEntity(
            version = 2,
            syncStatus = SyncStatus.PENDING
        )

        // When
        val action = versionManager.determineSyncAction(
            localEntity = localEntity,
            serverVersion = 1,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.UPLOAD, action)
    }

    @Test
    fun `test determineSyncAction with synced local and newer server`() {
        // Given
        val localEntity = createMockVersionedEntity(
            version = 1,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val action = versionManager.determineSyncAction(
            localEntity = localEntity,
            serverVersion = 2,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.DOWNLOAD, action)
    }

    @Test
    fun `test determineSyncAction with synced local and same server version`() {
        // Given
        val localEntity = createMockVersionedEntity(
            version = 1,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val action = versionManager.determineSyncAction(
            localEntity = localEntity,
            serverVersion = 1,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.NO_ACTION, action)
    }

    @Test
    fun `test determineSyncAction with synced local and older server`() {
        // Given
        val localEntity = createMockVersionedEntity(
            version = 2,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val action = versionManager.determineSyncAction(
            localEntity = localEntity,
            serverVersion = 1,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.UPLOAD, action)
    }

    @Test
    fun `test determineSyncAction with conflict status`() {
        // Given
        val localEntity = createMockVersionedEntity(
            version = 1,
            syncStatus = SyncStatus.CONFLICT
        )

        // When
        val action = versionManager.determineSyncAction(
            localEntity = localEntity,
            serverVersion = 2,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.CONFLICT, action)
    }

    @Test
    fun `test determineSyncAction with failed status`() {
        // Given
        val localEntity = createMockVersionedEntity(
            version = 1,
            syncStatus = SyncStatus.FAILED
        )

        // When
        val action = versionManager.determineSyncAction(
            localEntity = localEntity,
            serverVersion = 2,
            serverLastModified = System.currentTimeMillis()
        )

        // Then
        assertEquals(SyncAction.UPLOAD, action)
    }

    @Test
    fun `test createVersionStamp creates valid stamp`() {
        // When
        val versionStamp = versionManager.createVersionStamp()

        // Then
        assertEquals(1, versionStamp.version)
        assertTrue(versionStamp.lastModified > 0)
    }

    private fun createMockVersionedEntity(
        version: Int = 1,
        lastModified: Long = System.currentTimeMillis(),
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ): VersionedEntity {
        return object : VersionedEntity {
            override val version: Int = version
            override val lastModified: Long = lastModified
            override val syncStatus: SyncStatus = syncStatus
        }
    }
}