package com.studywise.ai.data.service

import com.studywise.ai.domain.repository.SyncRepository
import com.studywise.ai.domain.service.SyncTransaction
import com.studywise.ai.domain.service.SyncTransactionManager
import com.studywise.ai.domain.service.TransactionResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SyncTransactionManager
 */
@Singleton
class SyncTransactionManagerImpl @Inject constructor(
    private val syncRepository: SyncRepository
) : SyncTransactionManager {
    
    // Mutex to ensure thread-safe transaction operations
    private val transactionMutex = Mutex()
    
    override fun beginTransaction(): SyncTransaction {
        return SyncTransaction()
    }
    
    override suspend fun commitTransaction(transaction: SyncTransaction): TransactionResult {
        if (transaction.isEmpty()) {
            return TransactionResult(
                success = true,
                transactionId = transaction.getTransactionId(),
                operationCount = 0
            )
        }
        
        return transactionMutex.withLock {
            try {
                // Enqueue all operations in the transaction
                val operations = transaction.getOperations()
                val errors = mutableListOf<String>()
                
                operations.forEach { operation ->
                    try {
                        syncRepository.enqueueSyncOperation(operation)
                    } catch (e: Exception) {
                        errors.add("Failed to enqueue ${operation.type} for ${operation.entityType}:${operation.entityId} - ${e.message}")
                    }
                }
                
                TransactionResult(
                    success = errors.isEmpty(),
                    transactionId = transaction.getTransactionId(),
                    operationCount = operations.size,
                    errors = errors
                )
            } catch (e: Exception) {
                TransactionResult(
                    success = false,
                    transactionId = transaction.getTransactionId(),
                    operationCount = 0,
                    errors = listOf("Transaction failed: ${e.message}")
                )
            }
        }
    }
    
    override fun rollbackTransaction(transaction: SyncTransaction) {
        // Simply clear the transaction operations
        // Since we haven't committed them to the queue yet, nothing to undo
        transaction.clear()
    }
    
    override suspend fun <T> withTransaction(block: suspend (SyncTransaction) -> T): T {
        val transaction = beginTransaction()
        
        return try {
            val result = block(transaction)
            
            // If block executed successfully, commit the transaction
            val commitResult = commitTransaction(transaction)
            if (!commitResult.success) {
                throw IllegalStateException("Failed to commit transaction: ${commitResult.errors.joinToString()}")
            }
            
            result
        } catch (e: Exception) {
            // If any error occurred, rollback the transaction
            rollbackTransaction(transaction)
            throw e
        }
    }
}