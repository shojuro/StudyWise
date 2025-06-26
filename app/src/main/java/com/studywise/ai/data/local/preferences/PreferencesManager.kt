package com.studywise.ai.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_TEXT_SIZE = floatPreferencesKey("text_size")
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_STUDY_REMINDER_TIME = stringPreferencesKey("study_reminder_time")
        val KEY_SCHOOL_ID = stringPreferencesKey("school_id")
        val KEY_DAILY_REMINDERS = booleanPreferencesKey("daily_reminders")
        val KEY_PROGRESS_UPDATES = booleanPreferencesKey("progress_updates")
        val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val KEY_SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val KEY_SYNC_WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                userId = preferences[KEY_USER_ID],
                userEmail = preferences[KEY_USER_EMAIL],
                userName = preferences[KEY_USER_NAME],
                userRole = preferences[KEY_USER_ROLE],
                isLoggedIn = preferences[KEY_IS_LOGGED_IN] ?: false,
                themeMode = ThemeMode.fromString(preferences[KEY_THEME_MODE]),
                textSize = preferences[KEY_TEXT_SIZE] ?: 1.0f,
                highContrast = preferences[KEY_HIGH_CONTRAST] ?: false,
                onboardingCompleted = preferences[KEY_ONBOARDING_COMPLETED] ?: false,
                notificationsEnabled = preferences[KEY_NOTIFICATIONS_ENABLED] ?: true,
                studyReminderTime = preferences[KEY_STUDY_REMINDER_TIME],
                lastSyncTime = preferences[KEY_LAST_SYNC_TIME],
                syncEnabled = preferences[KEY_SYNC_ENABLED] ?: true,
                syncWifiOnly = preferences[KEY_SYNC_WIFI_ONLY] ?: true
            )
        }

    suspend fun updateUserSession(
        userId: String,
        userEmail: String,
        userName: String,
        userRole: String
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_EMAIL] = userEmail
            preferences[KEY_USER_NAME] = userName
            preferences[KEY_USER_ROLE] = userRole
            preferences[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_USER_ROLE)
            preferences[KEY_IS_LOGGED_IN] = false
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.value
        }
    }

    suspend fun updateTextSize(textSize: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_TEXT_SIZE] = textSize
        }
    }

    suspend fun updateHighContrast(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HIGH_CONTRAST] = enabled
        }
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateStudyReminderTime(time: String?) {
        dataStore.edit { preferences ->
            if (time != null) {
                preferences[KEY_STUDY_REMINDER_TIME] = time
            } else {
                preferences.remove(KEY_STUDY_REMINDER_TIME)
            }
        }
    }

    suspend fun updateSchoolId(schoolId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SCHOOL_ID] = schoolId
        }
    }

    suspend fun updateNotificationSettings(
        enableDailyReminders: Boolean,
        enableProgressUpdates: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDERS] = enableDailyReminders
            preferences[KEY_PROGRESS_UPDATES] = enableProgressUpdates
        }
    }
    
    suspend fun updateLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIME] = timestamp
        }
    }
    
    suspend fun updateSyncSettings(enabled: Boolean, wifiOnly: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SYNC_ENABLED] = enabled
            preferences[KEY_SYNC_WIFI_ONLY] = wifiOnly
        }
    }
}

data class UserPreferences(
    val userId: String? = null,
    val userEmail: String? = null,
    val userName: String? = null,
    val userRole: String? = null,
    val isLoggedIn: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textSize: Float = 1.0f,
    val highContrast: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val studyReminderTime: String? = null,
    val lastSyncTime: Long? = null,
    val syncEnabled: Boolean = true,
    val syncWifiOnly: Boolean = true
)

enum class ThemeMode(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromString(value: String?): ThemeMode {
            return values().find { it.value == value } ?: SYSTEM
        }
    }
}