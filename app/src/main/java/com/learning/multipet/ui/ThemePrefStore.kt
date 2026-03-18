package com.learning.multipet.ui

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

class ThemePrefsStore(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_preference")

    val themePreference: Flow<ThemePreference> =
        context.themeDataStore.data.map { prefs ->
            when (prefs[themeKey]) {
                ThemePreference.DARK.name -> ThemePreference.DARK
                ThemePreference.SYSTEM.name -> ThemePreference.SYSTEM
                else -> ThemePreference.LIGHT
            }
        }

    suspend fun saveThemePreference(pref: ThemePreference) {
        context.themeDataStore.edit { prefs ->
            prefs[themeKey] = pref.name
        }
    }
}