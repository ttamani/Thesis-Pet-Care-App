package com.learning.multipet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learning.multipet.data.SessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SessionStore(app.applicationContext)

    val isLoggedIn: StateFlow<Boolean> =
        store.sessionJson
            .map { !it.isNullOrBlank() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun saveSession(sessionJson: String) {
        viewModelScope.launch { store.saveSessionJson(sessionJson) }
    }

    fun logout() {
        viewModelScope.launch { store.clearSession() }
    }
}