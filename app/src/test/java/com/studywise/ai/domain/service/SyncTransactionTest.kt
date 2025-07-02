package com.studywise.ai.domain.service

import com.studywise.ai.data.service.SyncTransactionManagerImpl
import com.studywise.ai.domain.repository.SyncOperation
import com.studywise.ai.domain.repository.SyncOperationType
import com.studywise.ai.domain.repository.SyncRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for SyncTransaction and SyncTransactionManager
 */
class SyncTransactionTest {

    @MockK
    private lateinit var syncRepository: SyncRepository

    private lateinit var transactionManager: SyncTransactionManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        transactionManager = SyncTransactionManagerImpl(syncRepository)
    }

    @Test
    fun `test SyncTransaction create operation`() {
        // Given
        val transaction = SyncTransaction()

        // When
        transaction.create("User", "user1", """{"name": "John"}""")

        // Then
        assertEquals(1, transaction.size())
        assertFalse(transaction.isEmpty())
        
        val operations = transaction.getOperations()
        assertEquals(1, operations.size)
        assertEquals(SyncOperationType.CREATE, operations[0].type)
        assertEquals("User", operations[0].entityType)
        assertEquals("user1", operations[0].entityId)
        assertEquals("""{"name": "John"}""", operations[0].data)
    }

    @Test
    fun `test SyncTransaction update operation`() {
        // Given
        val transaction = SyncTransaction()

        // When
        transaction.update("User", "user1", """{"name": "Jane"}""")

        // Then
        assertEquals(1, transaction.size())
        
        val operations = transaction.getOperations()
        assertEquals(SyncOperationType.UPDATE, operations[0].type)
        assertEquals("User", operations[0].entityType)
        assertEquals("user1", operations[0].entityId)
        assertEquals("""{"name": "Jane"}""", operations[0].data)
    }

    @Test
    fun `test SyncTransaction delete operation`() {
        // Given
        val transaction = SyncTransaction()

        // When
        transaction.delete("User", "user1")

        // Then
        assertEquals(1, transaction.size())
        
        val operations = transaction.getOperations()
        assertEquals(SyncOperationType.DELETE, operations[0].type)
        assertEquals("User", operations[0].entityType)
        assertEquals("user1", operations[0].entityId)
        assertEquals("", operations[0].data) // No data for delete
    }

    @Test
    fun `test SyncTransaction multiple operations`() {
        // Given
        val transaction = SyncTransaction()

        // When
        transaction
            .create("User", "user1", """{"name": "John"}""")
            .update("User", "user2", """{"name": "Jane"}""")
            .delete("User", "user3")

        // Then
        assertEquals(3, transaction.size())
        assertFalse(transaction.isEmpty())
        
        val operations = transaction.getOperations()
        assertEquals(SyncOperationType.CREATE, operations[0].type)
        assertEquals(SyncOperationType.UPDATE, operations[1].type)
        assertEquals(SyncOperationType.DELETE, operations[2].type)
    }

    @Test
    fun `test SyncTransaction clear`() {
        // Given
        val transaction = SyncTransaction()
        transaction.create("User", "user1", """{"name": "John"}""")

        // When
        transaction.clear()

        // Then
        assertEquals(0, transaction.size())
        assertTrue(transaction.isEmpty())
    }

    @Test
    fun `test SyncTransaction has unique ID`() {
        // Given
        val transaction1 = SyncTransaction()
        val transaction2 = SyncTransaction()

        // When
        val id1 = transaction1.getTransactionId()
        val id2 = transaction2.getTransactionId()

        // Then
        assertTrue(id1.isNotEmpty())
        assertTrue(id2.isNotEmpty())
        assertTrue(id1 != id2)
    }

    @Test
    fun `test SyncTransactionManager beginTransaction creates new transaction`() {
        // When
        val transaction = transactionManager.beginTransaction()

        // Then
        assertTrue(transaction.isEmpty())
        assertTrue(transaction.getTransactionId().isNotEmpty())
    }

    @Test
    fun `test SyncTransactionManager commitTransaction with empty transaction`() = runTest {
        // Given
        val transaction = transactionManager.beginTransaction()

        // When
        val result = transactionManager.commitTransaction(transaction)

        // Then
        assertTrue(result.success)
        assertEquals(0, result.operationCount)
        assertTrue(result.errors.isEmpty())
        verify { syncRepository wasNot Called }
    }

    @Test
    fun `test SyncTransactionManager commitTransaction with operations`() = runTest {
        // Given
        val transaction = transactionManager.beginTransaction()
        transaction
            .create("User", "user1", """{"name": "John"}""")
            .update("User", "user2", """{"name": "Jane"}""")

        coEvery { syncRepository.enqueueSyncOperation(any()) } just Runs

        // When
        val result = transactionManager.commitTransaction(transaction)

        // Then
        assertTrue(result.success)
        assertEquals(2, result.operationCount)
        assertTrue(result.errors.isEmpty())
        coVerify(exactly = 2) { syncRepository.enqueueSyncOperation(any()) }
    }

    @Test
    fun `test SyncTransactionManager commitTransaction with operation failure`() = runTest {
        // Given
        val transaction = transactionManager.beginTransaction()
        transaction.create("User", "user1", """{"name": "John"}""")

        coEvery { syncRepository.enqueueSyncOperation(any()) } throws Exception("Database error")

        // When
        val result = transactionManager.commitTransaction(transaction)

        // Then
        assertFalse(result.success)
        assertEquals(1, result.operationCount)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].contains("Database error"))
    }

    @Test
    fun `test SyncTransactionManager rollbackTransaction clears operations`() {
        // Given
        val transaction = transactionManager.beginTransaction()
        transaction.create("User", "user1", """{"name": "John"}""")
        assertEquals(1, transaction.size())

        // When
        transactionManager.rollbackTransaction(transaction)

        // Then
        assertTrue(transaction.isEmpty())
    }

    @Test
    fun `test SyncTransactionManager withTransaction success`() = runTest {
        // Given
        coEvery { syncRepository.enqueueSyncOperation(any()) } just Runs

        // When
        val result = transactionManager.withTransaction { transaction ->
            transaction.create("User", "user1", """{"name": "John"}""")
            "operation completed"
        }

        // Then
        assertEquals("operation completed", result)
        coVerify { syncRepository.enqueueSyncOperation(any()) }
    }

    @Test
    fun `test SyncTransactionManager withTransaction failure`() = runTest {
        // Given
        val exception = RuntimeException("Business logic error")

        // When & Then
        try {
            transactionManager.withTransaction { transaction ->
                transaction.create("User", "user1", """{"name": "John"}""")
                throw exception
            }
            assertTrue(false, "Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Business logic error", e.message)
        }

        // Transaction should not have been committed
        coVerify(exactly = 0) { syncRepository.enqueueSyncOperation(any()) }
    }

    @Test
    fun `test SyncTransactionManager withTransaction commit failure`() = runTest {
        // Given
        coEvery { syncRepository.enqueueSyncOperation(any()) } throws Exception("Commit failed")

        // When & Then
        try {
            transactionManager.withTransaction { transaction ->
                transaction.create("User", "user1", """{"name": "John"}""")
                "operation completed"
            }
            assertTrue(false, "Expected exception to be thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Failed to commit transaction") == true)
        }
    }
}