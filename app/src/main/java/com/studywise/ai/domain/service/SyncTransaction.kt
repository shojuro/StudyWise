package com.studywise.ai.domain.service

import com.studywise.ai.domain.repository.SyncOperation
import com.studywise.ai.domain.repository.SyncOperationType
import java.util.UUID

/**
 * Represents a transactional sync operation that groups multiple operations
 */
class SyncTransaction {
    private val operations = mutableListOf<SyncOperation>()
    private val transactionId = UUID.randomUUID().toString()
    
    /**
     * Add a create operation to the transaction
     */
    fun create(entityType: String, entityId: String, data: String): SyncTransaction {
        operations.add(
            SyncOperation(
                id = UUID.randomUUID().toString(),
                type = SyncOperationType.CREATE,
                entityType = entityType,
                entityId = entityId,
                data = data,
                timestamp = System.currentTimeMillis()
            )
        )
        return this
    }
    
    /**
     * Add an update operation to the transaction
     */
    fun update(entityType: String, entityId: String, data: String): SyncTransaction {
        operations.add(
            SyncOperation(
                id = UUID.randomUUID().toString(),
                type = SyncOperationType.UPDATE,
                entityType = entityType,
                entityId = entityId,
                data = data,
                timestamp = System.currentTimeMillis()
            )
        )
        return this
    }
    
    /**
     * Add a delete operation to the transaction
     */
    fun delete(entityType: String, entityId: String): SyncTransaction {
        operations.add(
            SyncOperation(
                id = UUID.randomUUID().toString(),
                type = SyncOperationType.DELETE,
                entityType = entityType,
                entityId = entityId,
                data = "", // No data needed for delete
                timestamp = System.currentTimeMillis()
            )
        )
        return this
    }
    
    /**
     * Get all operations in this transaction
     */
    fun getOperations(): List<SyncOperation> = operations.toList()
    
    /**
     * Get the transaction ID
     */
    fun getTransactionId(): String = transactionId
    
    /**
     * Check if the transaction is empty
     */
    fun isEmpty(): Boolean = operations.isEmpty()
    
    /**
     * Get the number of operations in this transaction
     */
    fun size(): Int = operations.size
    
    /**
     * Clear all operations from this transaction
     */
    fun clear() {
        operations.clear()
    }
}

/**
 * Service for managing sync transactions
 */
interface SyncTransactionManager {
    /**
     * Begin a new sync transaction
     */
    fun beginTransaction(): SyncTransaction
    
    /**
     * Commit a transaction (enqueue all operations)
     */
    suspend fun commitTransaction(transaction: SyncTransaction): TransactionResult
    
    /**
     * Rollback a transaction (discard all operations)
     */
    fun rollbackTransaction(transaction: SyncTransaction)
    
    /**
     * Execute operations within a transaction
     */
    suspend fun <T> withTransaction(block: suspend (SyncTransaction) -> T): T
}

/**
 * Result of a transaction commit
 */
data class TransactionResult(
    val success: Boolean,
    val transactionId: String,
    val operationCount: Int,
    val errors: List<String> = emptyList()
)