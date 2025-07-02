package com.studywise.ai.data.service.privacy

import com.studywise.ai.data.local.dao.ConsentDao
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.privacy.ConsentAuditEntity
import com.studywise.ai.data.local.entity.privacy.ParentalConsentEntity
import com.studywise.ai.data.remote.api.StudyWiseApi
import com.studywise.ai.domain.model.privacy.*
import com.studywise.ai.domain.service.privacy.ConsentManager
import com.studywise.ai.domain.service.privacy.RestrictedFeature
import com.studywise.ai.domain.service.privacy.RestrictionReason
import com.studywise.ai.domain.service.privacy.ConsentAuditEntry
import com.studywise.ai.domain.service.privacy.ConsentAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Period
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ConsentManager for COPPA compliance
 */
@Singleton
class ConsentManagerImpl @Inject constructor(
    private val consentDao: ConsentDao,
    private val userDao: UserDao,
    private val api: StudyWiseApi
) : ConsentManager {

    override suspend fun verifyAge(birthDate: LocalDate): AgeVerification {
        val age = Period.between(birthDate, LocalDate.now()).years
        val requiresConsent = age < AgeVerification.COPPA_AGE_LIMIT
        
        return AgeVerification(
            birthDate = birthDate,
            parentalConsentRequired = requiresConsent,
            parentalConsentStatus = if (requiresConsent) ConsentStatus.PENDING else ConsentStatus.NOT_REQUIRED
        )
    }

    override suspend fun requestParentalConsent(
        childUserId: String,
        parentEmail: String,
        consentTypes: List<ConsentType>,
        method: ConsentMethod
    ): Result<ParentalConsent> {
        return try {
            // Create consent records for each type
            val consents = consentTypes.map { type ->
                val consent = ParentalConsent(
                    id = UUID.randomUUID().toString(),
                    childUserId = childUserId,
                    parentEmail = parentEmail,
                    consentType = type,
                    status = ConsentStatus.PENDING,
                    consentMethod = method
                )
                
                // Save to local database
                consentDao.insertConsent(consent.toEntity())
                
                // Log audit entry
                logConsentAction(
                    userId = childUserId,
                    consentId = consent.id,
                    action = ConsentAction.REQUESTED,
                    previousStatus = null,
                    newStatus = ConsentStatus.PENDING,
                    performedBy = "system"
                )
                
                consent
            }
            
            // Send consent request to API
            when (method) {
                ConsentMethod.EMAIL_VERIFICATION -> {
                    api.sendParentalConsentEmail(
                        childUserId = childUserId,
                        parentEmail = parentEmail,
                        consentIds = consents.map { it.id }
                    )
                }
                else -> {
                    // Handle other consent methods
                }
            }
            
            Result.success(consents.first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasRequiredConsents(userId: String): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        
        // Check if user requires consent
        if (user.birthDate == null) return false
        
        val age = Period.between(user.birthDate, LocalDate.now()).years
        if (age >= AgeVerification.COPPA_AGE_LIMIT) return true
        
        // Check all required consent types
        val requiredTypes = listOf(
            ConsentType.REGISTRATION,
            ConsentType.DATA_COLLECTION
        )
        
        val consents = consentDao.getUserConsents(userId)
        return requiredTypes.all { type ->
            consents.any { it.consentType == type && it.status == ConsentStatus.GRANTED }
        }
    }

    override suspend fun getUserConsents(userId: String): List<ParentalConsent> {
        return consentDao.getUserConsents(userId).map { it.toDomain() }
    }

    override suspend fun updateConsentStatus(
        consentId: String,
        status: ConsentStatus,
        verificationData: Map<String, String>
    ): Result<ParentalConsent> {
        return try {
            val consent = consentDao.getConsentById(consentId)
                ?: return Result.failure(IllegalArgumentException("Consent not found"))
            
            val previousStatus = consent.status
            val updatedConsent = consent.copy(
                status = status,
                consentedAt = if (status == ConsentStatus.GRANTED) System.currentTimeMillis() else consent.consentedAt,
                expiresAt = if (status == ConsentStatus.GRANTED) {
                    System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) // 1 year
                } else consent.expiresAt
            )
            
            consentDao.updateConsent(updatedConsent)
            
            // Log audit entry
            logConsentAction(
                userId = consent.childUserId,
                consentId = consentId,
                action = when (status) {
                    ConsentStatus.GRANTED -> ConsentAction.GRANTED
                    ConsentStatus.DENIED -> ConsentAction.DENIED
                    ConsentStatus.REVOKED -> ConsentAction.REVOKED
                    ConsentStatus.EXPIRED -> ConsentAction.EXPIRED
                    else -> ConsentAction.UPDATED
                },
                previousStatus = previousStatus,
                newStatus = status,
                performedBy = verificationData["performedBy"] ?: "system",
                additionalData = verificationData
            )
            
            Result.success(updatedConsent.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeConsent(consentId: String, reason: String): Result<Unit> {
        return try {
            updateConsentStatus(
                consentId = consentId,
                status = ConsentStatus.REVOKED,
                verificationData = mapOf("reason" to reason)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkCOPPACompliance(userId: String): COPPAVerification {
        val user = userDao.getUserById(userId)
            ?: return COPPAVerification(
                isCompliant = false,
                age = 0,
                requiresParentalConsent = true,
                restrictedFeatures = getAllRestrictedFeatures()
            )
        
        val age = if (user.birthDate != null) {
            Period.between(user.birthDate, LocalDate.now()).years
        } else {
            0
        }
        
        val requiresConsent = age < AgeVerification.COPPA_AGE_LIMIT
        val hasConsent = if (requiresConsent) hasRequiredConsents(userId) else true
        
        val restrictedFeatures = if (!hasConsent) {
            getRestrictedFeatures(userId).map { it.featureId }
        } else {
            emptyList()
        }
        
        return COPPAVerification(
            isCompliant = !requiresConsent || hasConsent,
            age = age,
            requiresParentalConsent = requiresConsent,
            consentStatus = if (requiresConsent) {
                if (hasConsent) ConsentStatus.GRANTED else ConsentStatus.PENDING
            } else null,
            restrictedFeatures = restrictedFeatures,
            verificationMethod = "birthdate"
        )
    }

    override suspend fun getRestrictedFeatures(userId: String): List<RestrictedFeature> {
        val user = userDao.getUserById(userId) ?: return getAllRestrictedFeatures()
        
        val age = if (user.birthDate != null) {
            Period.between(user.birthDate, LocalDate.now()).years
        } else {
            0
        }
        
        val features = mutableListOf<RestrictedFeature>()
        
        // Age-based restrictions
        if (age < 13) {
            features.add(
                RestrictedFeature(
                    featureId = "voice_recording",
                    name = "Voice Recording",
                    description = "Voice journal and verbal input features",
                    reason = if (hasRequiredConsents(userId)) RestrictionReason.AGE_RESTRICTION else RestrictionReason.MISSING_CONSENT,
                    requiredConsents = listOf(ConsentType.VOICE_RECORDING)
                )
            )
            features.add(
                RestrictedFeature(
                    featureId = "social_features",
                    name = "Social Features",
                    description = "Sharing and collaboration features",
                    reason = RestrictionReason.AGE_RESTRICTION
                )
            )
            features.add(
                RestrictedFeature(
                    featureId = "third_party_content",
                    name = "Third-Party Content",
                    description = "Access to external educational content",
                    reason = RestrictionReason.MISSING_CONSENT,
                    requiredConsents = listOf(ConsentType.THIRD_PARTY_SHARING)
                )
            )
        }
        
        // Check specific consents
        val consents = consentDao.getUserConsents(userId)
        
        if (!consents.any { it.consentType == ConsentType.ANALYTICS && it.status == ConsentStatus.GRANTED }) {
            features.add(
                RestrictedFeature(
                    featureId = "analytics",
                    name = "Usage Analytics",
                    description = "Detailed learning analytics and insights",
                    reason = RestrictionReason.MISSING_CONSENT,
                    requiredConsents = listOf(ConsentType.ANALYTICS)
                )
            )
        }
        
        return features
    }

    override fun observeConsentStatus(userId: String): Flow<List<ParentalConsent>> {
        return consentDao.observeUserConsents(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getConsentAuditTrail(userId: String): List<ConsentAuditEntry> {
        return consentDao.getConsentAuditTrail(userId).map { it.toDomain() }
    }

    private suspend fun logConsentAction(
        userId: String,
        consentId: String,
        action: ConsentAction,
        previousStatus: ConsentStatus?,
        newStatus: ConsentStatus,
        performedBy: String,
        ipAddress: String? = null,
        additionalData: Map<String, String> = emptyMap()
    ) {
        val auditEntry = ConsentAuditEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            consentId = consentId,
            action = action,
            previousStatus = previousStatus,
            newStatus = newStatus,
            timestamp = System.currentTimeMillis(),
            performedBy = performedBy,
            ipAddress = ipAddress,
            additionalData = additionalData
        )
        
        consentDao.insertAuditEntry(auditEntry)
    }

    private fun getAllRestrictedFeatures(): List<String> {
        return listOf(
            "voice_recording",
            "social_features",
            "third_party_content",
            "analytics",
            "advanced_ai_features"
        )
    }
}

// Extension functions for entity conversion
private fun ParentalConsent.toEntity() = ParentalConsentEntity(
    id = id,
    childUserId = childUserId,
    parentEmail = parentEmail,
    consentType = consentType,
    status = status,
    consentedAt = consentedAt,
    expiresAt = expiresAt,
    ipAddress = ipAddress,
    consentMethod = consentMethod,
    additionalData = additionalData
)

private fun ParentalConsentEntity.toDomain() = ParentalConsent(
    id = id,
    childUserId = childUserId,
    parentEmail = parentEmail,
    consentType = consentType,
    status = status,
    consentedAt = consentedAt,
    expiresAt = expiresAt,
    ipAddress = ipAddress,
    consentMethod = consentMethod,
    additionalData = additionalData
)

private fun ConsentAuditEntity.toDomain() = ConsentAuditEntry(
    id = id,
    userId = userId,
    consentId = consentId,
    action = action,
    previousStatus = previousStatus,
    newStatus = newStatus,
    timestamp = timestamp,
    performedBy = performedBy,
    ipAddress = ipAddress,
    additionalData = additionalData
)