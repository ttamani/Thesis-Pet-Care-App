package com.learning.multipet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learning.multipet.data.AuthRepository
import com.learning.multipet.data.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel that manages user authentication state and operations.
 * This ViewModel handles sign-in, sign-up, and sign-out operations using Supabase Auth,
 * and manages the session state using DataStore for persistence.
 *
 * @param app The Application instance for accessing context and creating the SessionStore
 */
class SessionViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SessionStore(app.applicationContext)
    private val authRepository = AuthRepository()

    /**
     * StateFlow indicating whether the user is currently logged in.
     * Derived from the stored session JSON - if non-null/non-blank, user is considered logged in.
     */
    val isLoggedIn: StateFlow<Boolean> =
        store.sessionJson
            .map { !it.isNullOrBlank() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * StateFlow indicating whether an authentication operation is in progress.
     * Used to show loading indicators in the UI during sign-in or sign-up.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * StateFlow containing the most recent authentication error message.
     * Cleared when a new authentication operation starts.
     */
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    /**
     * Saves the session JSON string to persistent storage.
     * This marks the user as logged in across app restarts.
     *
     * @param sessionJson The JSON string representation of the Supabase session
     */
    fun saveSession(sessionJson: String) {
        viewModelScope.launch { store.saveSessionJson(sessionJson) }
    }

    /**
     * Attempts to sign in a user with the provided email and password.
     * On success, saves the session to DataStore. On failure, sets the error state.
     *
     * @param email The user's email address
     * @param password The user's password
     * @param onSuccess Callback invoked when sign-in succeeds
     */
    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null

            val result = authRepository.signIn(email, password)

            result
                .onSuccess {
                    val sessionJson = authRepository.serializeCurrentSession()
                    sessionJson?.let { saveSession(it) }
                    onSuccess()
                }
                .onFailure { error ->
                    _authError.value = error.message ?: "Sign in failed"
                }

            _isLoading.value = false
        }
    }

    /**
     * Attempts to sign up a new user with the provided email and password.
     * On success, saves the session to DataStore. On failure, sets the error state.
     *
     * @param email The user's email address
     * @param password The user's password
     * @param onSuccess Callback invoked when sign-up succeeds
     */
    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null

            val result = authRepository.signUp(email, password)

            result
                .onSuccess {
                    val sessionJson = authRepository.serializeCurrentSession()
                    sessionJson?.let { saveSession(it) }
                    onSuccess()
                }
                .onFailure { error ->
                    _authError.value = error.message ?: "Sign up failed"
                }

            _isLoading.value = false
        }
    }

    /**
     * Signs out the current user and clears the stored session.
     * After calling this method, the user will be considered logged out.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            store.clearSession()
        }
    }

    /**
     * Clears the current authentication error state.
     * Call this when the user dismisses or acknowledges an error.
     */
    fun clearError() {
        _authError.value = null
    }
}