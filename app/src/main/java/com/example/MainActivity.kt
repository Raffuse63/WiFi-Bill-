package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.preferences.SettingsDataStore
import com.example.data.preferences.UserSettings
import com.example.data.repository.WiFiManagerRepository
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.WiFiManagerTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var backupManager: BackupManager
    private lateinit var repository: WiFiManagerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(this)
        settingsDataStore = SettingsDataStore(this)
        backupManager = BackupManager(database)
        repository = WiFiManagerRepository(database, settingsDataStore)

        setContent {
            val userSettings by settingsDataStore.userSettings.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )

            WiFiManagerTheme(darkTheme = userSettings.isDarkMode) {
                AppNavGraph(
                    repository = repository,
                    settingsDataStore = settingsDataStore,
                    backupManager = backupManager,
                    userSettings = userSettings
                )
            }
        }
    }
}
