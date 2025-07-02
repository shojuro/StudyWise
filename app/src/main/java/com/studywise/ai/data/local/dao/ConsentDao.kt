package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.privacy.ConsentAuditEntity
import com.studywise.ai.data.local.entity.privacy.ParentalConsentEntity
import com.studywise.ai.domain.model.privacy.ConsentStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for consent management
 */
@Dao
interface ConsentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsent(consent: ParentalConsentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsents(consents: List<ParentalConsentEntity>)
    
    @Update
    suspend fun updateConsent(consent: ParentalConsentEntity)
    
    @Query("SELECT * FROM parental_consents WHERE id = :consentId")
    suspend fun getConsentById(consentId: String): ParentalConsentEntity?
    
    @Query("SELECT * FROM parental_consents WHERE childUserId = :userId")
    suspend fun getUserConsents(userId: String): List<ParentalConsentEntity>
    
    @Query("SELECT * FROM parental_consents WHERE childUserId = :userId")
    fun observeUserConsents(userId: String): Flow<List<ParentalConsentEntity>>
    
    @Query("SELECT * FROM parental_consents WHERE childUserId = :userId AND status = :status")
    suspend fun getUserConsentsByStatus(userId: String, status: ConsentStatus): List<ParentalConsentEntity>
    
    @Query("SELECT * FROM parental_consents WHERE parentEmail = :parentEmail")
    suspend fun getConsentsByParentEmail(parentEmail: String): List<ParentalConsentEntity>
    
    @Query("DELETE FROM parental_consents WHERE id = :consentId")
    suspend fun deleteConsent(consentId: String)
    
    // Audit trail
    @Insert
    suspend fun insertAuditEntry(auditEntry: ConsentAuditEntity)
    
    @Query("SELECT * FROM consent_audit_trail WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getConsentAuditTrail(userId: String): List<ConsentAuditEntity>
    
    @Query("SELECT * FROM consent_audit_trail WHERE consentId = :consentId ORDER BY timestamp DESC")
    suspend fun getConsentHistory(consentId: String): List<ConsentAuditEntity>
    
    @Query("DELETE FROM consent_audit_trail WHERE timestamp < :timestamp")
    suspend fun deleteOldAuditEntries(timestamp: Long)
    
    // Utility queries
    @Query("""
        UPDATE parental_consents 
        SET status = 'EXPIRED' 
        WHERE expiresAt IS NOT NULL 
        AND expiresAt < :currentTimestamp 
        AND status = 'GRANTED'
    """)
    suspend fun expireOldConsents(currentTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM parental_consents WHERE childUserId = :userId AND status = 'PENDING'")
    suspend fun getPendingConsentCount(userId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM parental_consents 
        WHERE childUserId = :userId 
        AND status = 'GRANTED' 
        AND (expiresAt IS NULL OR expiresAt > :currentTimestamp)
    """)
    suspend fun getActiveConsentCount(userId: String, currentTimestamp: Long): Int
}