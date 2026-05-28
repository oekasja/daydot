package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val DOB_KEY = stringPreferencesKey("date_of_birth") // YYYY-MM-DD
    private val LIFESPAN_KEY = intPreferencesKey("expected_lifespan")
    private val THEME_KEY = stringPreferencesKey("theme_preference") // SYSTEM, LIGHT, DARK
    private val LANGUAGE_KEY = stringPreferencesKey("language_preference") // EN, IN
    private val MORNING_REMINDER_KEY = stringPreferencesKey("morning_reminder_time") // HH:MM
    private val EVENING_REMINDER_KEY = stringPreferencesKey("evening_reminder_time") // HH:MM

    val userDob: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DOB_KEY]
    }

    val userLifespan: Flow<Int> = dataStore.data.map { preferences ->
        preferences[LIFESPAN_KEY] ?: 80 // default to 80 years
    }

    val themePreference: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "SYSTEM"
    }

    val languagePreference: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "EN"
    }

    val morningReminderTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[MORNING_REMINDER_KEY] ?: "08:00"
    }

    val eveningReminderTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[EVENING_REMINDER_KEY] ?: "20:00"
    }

    suspend fun saveUserDob(dob: String) {
        dataStore.edit { preferences ->
            preferences[DOB_KEY] = dob
        }
    }

    suspend fun saveUserLifespan(lifespan: Int) {
        dataStore.edit { preferences ->
            preferences[LIFESPAN_KEY] = lifespan
        }
    }

    suspend fun saveThemePreference(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun saveLanguagePreference(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun saveMorningReminderTime(time: String) {
        dataStore.edit { preferences ->
            preferences[MORNING_REMINDER_KEY] = time
        }
    }

    suspend fun saveEveningReminderTime(time: String) {
        dataStore.edit { preferences ->
            preferences[EVENING_REMINDER_KEY] = time
        }
    }
}
