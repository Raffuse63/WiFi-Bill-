package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.preferences.SettingsDataStore
import com.example.data.preferences.UserSettings
import com.example.data.repository.WiFiManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: WiFiManagerRepository,
    private val settingsDataStore: SettingsDataStore,
    private val backupManager: BackupManager
) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    var messageState = MutableStateFlow<String?>(null)

    fun updateOwnerName(name: String) {
        viewModelScope.launch {
            settingsDataStore.updateOwnerName(name)
        }
    }

    fun updateBusinessName(name: String) {
        viewModelScope.launch {
            settingsDataStore.updateBusinessName(name)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            settingsDataStore.updateCurrency(currency)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.updateLanguage(language)
        }
    }

    fun updatePin(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updatePin(pin, enabled)
        }
    }

    fun updateDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateDarkMode(isDark)
        }
    }

    suspend fun exportJsonBackup(): String {
        return backupManager.createJsonBackup()
    }

    suspend fun restoreJsonBackup(json: String): Boolean {
        return backupManager.restoreFromJson(json)
    }

    class Factory(
        private val repository: WiFiManagerRepository,
        private val settingsDataStore: SettingsDataStore,
        private val backupManager: BackupManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, settingsDataStore, backupManager) as T
        }
    }
}
