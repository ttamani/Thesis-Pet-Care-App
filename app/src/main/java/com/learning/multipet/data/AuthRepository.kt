package com.learning.multipet.data

import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository class that encapsulates all Supabase authentication operations.
 * This class provides a clean API for sign-in, sign-up, and sign-out operations,
 * handling errors and returning results in a type-safe manner.
 *
 * @param auth The Supabase Auth module for performing authentication operations
 */
class AuthRepository(
    private val auth: Auth = SupabaseClient.auth
) {

    /**
     * Signs in an existing user with email and password.
     *
     * @param email The user's email address
     * @param password The user's password
     * @return Result indicating success or failure of the sign-in operation
     */
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs up a new user with email and password.
     *
     * @param email The user's email address
     * @param password The user's password
     * @return Result containing the UserInfo on success, or an exception on failure
     */
    suspend fun signUp(email: String, password: String): Result<UserInfo?> {
        return try {
            val result = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out the currently authenticated user.
     *
     * @return Result indicating success or failure of the sign-out operation
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves the current active session if one exists.
     *
     * @return The current UserSession, or null if no user is signed in
     */
    fun getCurrentSession(): UserSession? {
        return auth.currentSessionOrNull()
    }

    /**
     * Serializes the current session to a JSON string for storage.
     *
     * @return JSON string representation of the session, or null if no session exists
     */
    fun serializeCurrentSession(): String? {
        return getCurrentSession()?.let { session ->
            Json.encodeToString(session)
        }
    }

    /**
     * Deserializes a session JSON string into a UserSession object.
     *
     * @param json The JSON string containing the serialized session
     * @return The deserialized UserSession, or null if parsing fails
     */
    fun deserializeSession(json: String): UserSession? {
        return try {
            Json.decodeFromString<UserSession>(json)
        } catch (e: Exception) {
            null
        }
    }
}
