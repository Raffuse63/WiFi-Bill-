package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wifi_manager_settings")

data class UserSettings(
    val ownerName: String = "Manager",
    val businessName: String = "UltraNet WiFi",
    val currency: String = "৳",
    val language: String = "en", // "en" or "bn"
    val isPinEnabled: Boolean = false,
    val pinCode: String = "",
    val isDarkMode: Boolean = false
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val CURRENCY = stringPreferencesKey("currency")
        val LANGUAGE = stringPreferencesKey("language")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val PIN_CODE = stringPreferencesKey("pin_code")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            ownerName = preferences[Keys.OWNER_NAME] ?: "Owner",
            businessName = preferences[Keys.BUSINESS_NAME] ?: "WiFi Express Network",
            currency = preferences[Keys.CURRENCY] ?: "৳",
            language = preferences[Keys.LANGUAGE] ?: "en",
            isPinEnabled = preferences[Keys.IS_PIN_ENABLED] ?: false,
            pinCode = preferences[Keys.PIN_CODE] ?: "",
            isDarkMode = preferences[Keys.IS_DARK_MODE] ?: false
        )
    }

    suspend fun updateOwnerName(name: String) {
        context.dataStore.edit { prefs -> prefs[Keys.OWNER_NAME] = name }
    }

    suspend fun updateBusinessName(name: String) {
        context.dataStore.edit { prefs -> prefs[Keys.BUSINESS_NAME] = name }
    }

    suspend fun updateCurrency(currency: String) {
        context.dataStore.edit { prefs -> prefs[Keys.CURRENCY] = currency }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { prefs -> prefs[Keys.LANGUAGE] = lang }
    }

    suspend fun updatePin(pin: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_CODE] = pin
            prefs[Keys.IS_PIN_ENABLED] = enabled
        }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_DARK_MODE] = isDark }
    }
}
