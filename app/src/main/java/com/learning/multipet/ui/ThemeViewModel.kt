package com.learning.multipet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ThemePrefsStore(application)

    val themePreference: StateFlow<ThemePreference> =
        store.themePreference.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = ThemePreference.LIGHT
        )

    fun setThemePreference(pref: ThemePreference) {
        viewModelScope.launch {
            store.saveThemePreference(pref)
        }
    }
}