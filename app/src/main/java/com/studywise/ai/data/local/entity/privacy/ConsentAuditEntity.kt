package com.studywise.ai.data.local.entity.privacy

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.studywise.ai.data.local.converter.Converters
import com.studywise.ai.domain.model.privacy.ConsentStatus
import com.studywise.ai.domain.service.privacy.ConsentAction

/**
 * Room entity for consent audit trail
 */
@Entity(tableName = "consent_audit_trail")
@TypeConverters(Converters::class)
data class ConsentAuditEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val consentId: String,
    val action: ConsentAction,
    val previousStatus: ConsentStatus?,
    val newStatus: ConsentStatus,
    val timestamp: Long,
    val performedBy: String,
    val ipAddress: String? = null,
    val additionalData: Map<String, String> = emptyMap()
)