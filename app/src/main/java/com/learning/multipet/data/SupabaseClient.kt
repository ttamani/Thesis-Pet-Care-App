package com.learning.multipet.data

import com.learning.multipet.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient

/**
 * Singleton object that provides a single, shared instance of the Supabase client.
 * This follows the singleton pattern to ensure all parts of the app use the same
 * authenticated session and connection pool.
 *
 * The client is initialized lazily with the Supabase URL and anon key from BuildConfig,
 * and includes the Auth module for user authentication operations.
 */
object SupabaseClient {

    /**
     * The lazily-initialized Supabase client instance.
     * Configured with the project's URL, anon key, and Auth module.
     * This client handles all communication with the Supabase backend.
     */
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
    }

    /**
     * The Auth module from the Supabase client.
     * Provides access to authentication operations like signIn, signUp, and signOut.
     */
    val auth: Auth
        get() = client.auth
}
