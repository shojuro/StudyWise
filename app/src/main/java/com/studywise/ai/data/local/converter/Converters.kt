package com.studywise.ai.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studywise.ai.data.local.entity.base.SyncStatus
import com.studywise.ai.domain.model.privacy.ConsentStatus
import com.studywise.ai.domain.model.privacy.ConsentType
import com.studywise.ai.domain.model.privacy.ConsentMethod
import com.studywise.ai.domain.repository.ConflictType
import com.studywise.ai.domain.repository.SyncOperationType
import com.studywise.ai.domain.service.privacy.ConsentAction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

/**
 * Type converters for Room database
 */
class Converters {
    private val gson = Gson()

    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun toLocalDateTime(epochSecond: Long?): LocalDateTime? {
        return epochSecond?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    // List<String> converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Map<String, String> converters
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Enum converters
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toSyncStatus(status: String?): SyncStatus? {
        return status?.let { SyncStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromConsentStatus(status: ConsentStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toConsentStatus(status: String?): ConsentStatus? {
        return status?.let { ConsentStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromConsentType(type: ConsentType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toConsentType(type: String?): ConsentType? {
        return type?.let { ConsentType.valueOf(it) }
    }

    @TypeConverter
    fun fromConsentMethod(method: ConsentMethod?): String? {
        return method?.name
    }

    @TypeConverter
    fun toConsentMethod(method: String?): ConsentMethod? {
        return method?.let { ConsentMethod.valueOf(it) }
    }

    @TypeConverter
    fun fromConsentAction(action: ConsentAction?): String? {
        return action?.name
    }

    @TypeConverter
    fun toConsentAction(action: String?): ConsentAction? {
        return action?.let { ConsentAction.valueOf(it) }
    }

    @TypeConverter
    fun fromSyncOperationType(type: SyncOperationType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toSyncOperationType(type: String?): SyncOperationType? {
        return type?.let { SyncOperationType.valueOf(it) }
    }

    @TypeConverter
    fun fromConflictType(type: ConflictType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toConflictType(type: String?): ConflictType? {
        return type?.let { ConflictType.valueOf(it) }
    }
}