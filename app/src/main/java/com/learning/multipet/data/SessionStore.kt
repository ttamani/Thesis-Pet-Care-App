package com.learning.multipet.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_store")

class SessionStore(private val context: Context) {

    private val KEY_SESSION_JSON = stringPreferencesKey("supabase_session_json")

    // If this is hindi null treat user as logged in
    val sessionJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_SESSION_JSON]
    }

    suspend fun saveSessionJson(sessionJson: String) {
        context.dataStore.edit { it[KEY_SESSION_JSON] = sessionJson }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.remove(KEY_SESSION_JSON) }
    }
}