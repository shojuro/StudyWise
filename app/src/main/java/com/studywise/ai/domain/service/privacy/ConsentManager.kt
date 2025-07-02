package com.studywise.ai.domain.service.privacy

import com.studywise.ai.domain.model.privacy.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Service for managing parental consent and COPPA compliance
 */
interface ConsentManager {
    /**
     * Verify user's age and determine consent requirements
     */
    suspend fun verifyAge(birthDate: LocalDate): AgeVerification
    
    /**
     * Request parental consent for a minor user
     */
    suspend fun requestParentalConsent(
        childUserId: String,
        parentEmail: String,
        consentTypes: List<ConsentType>,
        method: ConsentMethod = ConsentMethod.EMAIL_VERIFICATION
    ): Result<ParentalConsent>
    
    /**
     * Check if user has all required consents
     */
    suspend fun hasRequiredConsents(userId: String): Boolean
    
    /**
     * Get all consent records for a user
     */
    suspend fun getUserConsents(userId: String): List<ParentalConsent>
    
    /**
     * Update consent status
     */
    suspend fun updateConsentStatus(
        consentId: String,
        status: ConsentStatus,
        verificationData: Map<String, String> = emptyMap()
    ): Result<ParentalConsent>
    
    /**
     * Revoke consent
     */
    suspend fun revokeConsent(consentId: String, reason: String): Result<Unit>
    
    /**
     * Check COPPA compliance for a user
     */
    suspend fun checkCOPPACompliance(userId: String): COPPAVerification
    
    /**
     * Get restricted features for a user based on age and consent
     */
    suspend fun getRestrictedFeatures(userId: String): List<RestrictedFeature>
    
    /**
     * Observe consent status changes
     */
    fun observeConsentStatus(userId: String): Flow<List<ParentalConsent>>
    
    /**
     * Generate consent audit trail
     */
    suspend fun getConsentAuditTrail(userId: String): List<ConsentAuditEntry>
}

/**
 * Features that may be restricted based on age or consent
 */
data class RestrictedFeature(
    val featureId: String,
    val name: String,
    val description: String,
    val reason: RestrictionReason,
    val requiredConsents: List<ConsentType> = emptyList()
)

/**
 * Reasons for feature restrictions
 */
enum class RestrictionReason {
    AGE_RESTRICTION,          // User is too young
    MISSING_CONSENT,          // Required consent not provided
    CONSENT_DENIED,           // Parent denied consent
    CONSENT_REVOKED,          // Consent was revoked
    REGIONAL_RESTRICTION     // Feature not available in user's region
}

/**
 * Audit entry for consent changes
 */
data class ConsentAuditEntry(
    val id: String,
    val userId: String,
    val consentId: String,
    val action: ConsentAction,
    val previousStatus: ConsentStatus?,
    val newStatus: ConsentStatus,
    val timestamp: Long,
    val performedBy: String,
    val ipAddress: String?,
    val additionalData: Map<String, String> = emptyMap()
)

/**
 * Actions that can be performed on consent
 */
enum class ConsentAction {
    REQUESTED,
    GRANTED,
    DENIED,
    REVOKED,
    EXPIRED,
    RENEWED,
    UPDATED
}