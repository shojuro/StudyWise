package com.studywise.ai.domain.model.privacy

import java.time.LocalDate
import java.time.Period

/**
 * Age verification data for COPPA compliance
 */
data class AgeVerification(
    val birthDate: LocalDate,
    val verifiedAt: Long = System.currentTimeMillis(),
    val parentalConsentRequired: Boolean = false,
    val parentalConsentStatus: ConsentStatus = ConsentStatus.NOT_REQUIRED
) {
    val age: Int
        get() = Period.between(birthDate, LocalDate.now()).years
    
    val isMinor: Boolean
        get() = age < 18
    
    val requiresParentalConsent: Boolean
        get() = age < 13
    
    companion object {
        const val COPPA_AGE_LIMIT = 13
        const val ADULT_AGE_LIMIT = 18
    }
}

/**
 * Parental consent status for COPPA compliance
 */
enum class ConsentStatus {
    NOT_REQUIRED,      // User is 13 or older
    PENDING,           // Consent requested but not yet given
    GRANTED,           // Parent has provided consent
    DENIED,            // Parent has denied consent
    EXPIRED,           // Consent has expired and needs renewal
    REVOKED           // Consent was granted but later revoked
}

/**
 * Parental consent record
 */
data class ParentalConsent(
    val id: String,
    val childUserId: String,
    val parentEmail: String,
    val consentType: ConsentType,
    val status: ConsentStatus,
    val consentedAt: Long? = null,
    val expiresAt: Long? = null,
    val ipAddress: String? = null,
    val consentMethod: ConsentMethod? = null,
    val additionalData: Map<String, String> = emptyMap()
)

/**
 * Types of consent required
 */
enum class ConsentType {
    REGISTRATION,          // Initial registration consent
    DATA_COLLECTION,       // Consent for collecting personal data
    MARKETING,            // Consent for marketing communications
    THIRD_PARTY_SHARING,  // Consent for sharing data with third parties
    VOICE_RECORDING,      // Consent for voice recording features
    ANALYTICS            // Consent for analytics data collection
}

/**
 * Methods for obtaining parental consent
 */
enum class ConsentMethod {
    EMAIL_VERIFICATION,   // Parent verifies via email link
    CREDIT_CARD,         // Parent provides credit card for verification
    PHONE_CALL,          // Verbal consent via phone
    SIGNED_FORM,         // Signed consent form upload
    IN_APP              // Direct consent through parent's app account
}

/**
 * COPPA compliance verification result
 */
data class COPPAVerification(
    val isCompliant: Boolean,
    val age: Int,
    val requiresParentalConsent: Boolean,
    val consentStatus: ConsentStatus? = null,
    val restrictedFeatures: List<String> = emptyList(),
    val verificationMethod: String? = null
)