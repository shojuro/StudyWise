package com.studywise.ai.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for encrypted shared preferences using Android's security library
 */
@Singleton
class EncryptedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val ENCRYPTED_PREFS_NAME = "studywise_secure_prefs"
    }
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create encrypted preferences, falling back to regular preferences")
            // Fallback to regular preferences if encryption fails (shouldn't happen)
            context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    fun putString(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }
    
    fun getString(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs.getString(key, defaultValue)
    }
    
    fun putInt(key: String, value: Int) {
        encryptedPrefs.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return encryptedPrefs.getInt(key, defaultValue)
    }
    
    fun putLong(key: String, value: Long) {
        encryptedPrefs.edit().putLong(key, value).apply()
    }
    
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return encryptedPrefs.getLong(key, defaultValue)
    }
    
    fun putBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, defaultValue)
    }
    
    fun putFloat(key: String, value: Float) {
        encryptedPrefs.edit().putFloat(key, value).apply()
    }
    
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return encryptedPrefs.getFloat(key, defaultValue)
    }
    
    fun putStringSet(key: String, values: Set<String>) {
        encryptedPrefs.edit().putStringSet(key, values).apply()
    }
    
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return encryptedPrefs.getStringSet(key, defaultValue) ?: defaultValue
    }
    
    fun remove(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }
    
    fun clear() {
        encryptedPrefs.edit().clear().apply()
    }
    
    fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }
    
    fun getAll(): Map<String, *> {
        return encryptedPrefs.all
    }
    
    /**
     * Migrate data from regular preferences to encrypted preferences
     */
    fun migrateFromRegularPreferences(regularPrefsName: String) {
        try {
            val regularPrefs = context.getSharedPreferences(regularPrefsName, Context.MODE_PRIVATE)
            val editor = encryptedPrefs.edit()
            
            regularPrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        editor.putStringSet(key, value as Set<String>)
                    }
                }
            }
            
            editor.apply()
            
            // Clear regular preferences after migration
            regularPrefs.edit().clear().apply()
            
            Timber.i("Successfully migrated ${regularPrefs.all.size} preferences to encrypted storage")
        } catch (e: Exception) {
            Timber.e(e, "Failed to migrate preferences")
        }
    }
}