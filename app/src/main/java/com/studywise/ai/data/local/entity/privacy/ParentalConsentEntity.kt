package com.studywise.ai.data.local.entity.privacy

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.studywise.ai.data.local.converter.Converters
import com.studywise.ai.domain.model.privacy.ConsentStatus
import com.studywise.ai.domain.model.privacy.ConsentType
import com.studywise.ai.domain.model.privacy.ConsentMethod

/**
 * Room entity for parental consent records
 */
@Entity(tableName = "parental_consents")
@TypeConverters(Converters::class)
data class ParentalConsentEntity(
    @PrimaryKey
    val id: String,
    val childUserId: String,
    val parentEmail: String,
    val consentType: ConsentType,
    val status: ConsentStatus,
    val consentedAt: Long? = null,
    val expiresAt: Long? = null,
    val ipAddress: String? = null,
    val consentMethod: ConsentMethod? = null,
    val additionalData: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)