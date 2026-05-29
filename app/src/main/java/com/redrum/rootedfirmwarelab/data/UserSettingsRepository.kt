package com.redrum.rootedfirmwarelab.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferenceKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
    val PRIMARY_ARGB = intPreferencesKey("primary_argb")
    val SECONDARY_ARGB = intPreferencesKey("secondary_argb")
    val TERTIARY_ARGB = intPreferencesKey("tertiary_argb")
    val FIRST_RUN_COMPLETE = booleanPreferencesKey("first_run_complete")
}

class UserSettingsRepository(private val context: Context) {
    
    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DARK_MODE] ?: false
    }
    val highContrastFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HIGH_CONTRAST] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DARK_MODE] = enabled
        }
    }
    
    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.HIGH_CONTRAST] = enabled
        }
    }

    suspend fun setThemeColors(primary: Int, secondary: Int, tertiary: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.PRIMARY_ARGB] = primary
            prefs[PreferenceKeys.SECONDARY_ARGB] = secondary
            prefs[PreferenceKeys.TERTIARY_ARGB] = tertiary
        }
    }
}
