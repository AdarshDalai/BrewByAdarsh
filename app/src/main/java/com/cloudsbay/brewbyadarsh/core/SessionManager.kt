package com.cloudsbay.brewbyadarsh.core

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthSession
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthUser
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : io.github.jan.supabase.auth.SessionManager {
    companion object {
        private const val TAG = "SessionManager"
        private val AUTH_SESSION_KEY = stringPreferencesKey("auth_session")
        private val SUPABASE_SESSION_KEY = stringPreferencesKey("supabase_session")
        private val PENDING_SIGN_OUT_KEY = booleanPreferencesKey("pending_sign_out")
    }

    override suspend fun saveSession(session: UserSession) {
        Log.d(TAG, "Saving session for user: ${session.user?.id}")
        dataStore.edit { preferences ->
            preferences[SUPABASE_SESSION_KEY] = json.encodeToString(session)
            
            // Map UserSession to our domain AuthSession
            val authUser = AuthUser(
                id = session.user?.id ?: "",
                email = session.user?.email,
                phone = session.user?.phone,
                emailConfirmedAt = session.user?.emailConfirmedAt?.toString(),
                phoneConfirmedAt = session.user?.phoneConfirmedAt?.toString(),
                lastSignInAt = session.user?.lastSignInAt?.toString(),
                userMetadata = session.user?.userMetadata?.mapValues { it.value.toString() },
                appMetadata = session.user?.appMetadata?.mapValues { it.value.toString() },
            )
            val authSession = AuthSession(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                user = authUser,
                expiresAt = session.expiresAt.toEpochMilliseconds()
            )
            preferences[AUTH_SESSION_KEY] = json.encodeToString(authSession)
        }
    }

    /**
     * Loads the session from DataStore.
     * Throws [IllegalStateException] when no session exists — this is the contract
     * required by the supabase-kt SDK. The SDK catches this internally and falls
     * back to an unauthenticated state.
     *
     * For nullable access, use [loadSessionOrNull] (default implementation in the
     * interface wraps this in try/catch).
     */
    override suspend fun loadSession(): UserSession {
        val preferences = dataStore.data.first()
        val sessionJson = preferences[SUPABASE_SESSION_KEY]
            ?: error("No session found")
        return json.decodeFromString<UserSession>(sessionJson)
    }

    override suspend fun deleteSession() {
        Log.d(TAG, "Deleting session")
        dataStore.edit { preferences ->
            preferences.remove(SUPABASE_SESSION_KEY)
            preferences.remove(AUTH_SESSION_KEY)
        }
    }

    suspend fun markPendingSignOut() {
        dataStore.edit { preferences ->
            preferences[PENDING_SIGN_OUT_KEY] = true
        }
    }

    suspend fun clearPendingSignOut() {
        dataStore.edit { preferences ->
            preferences.remove(PENDING_SIGN_OUT_KEY)
        }
    }

    suspend fun isPendingSignOut(): Boolean {
        return dataStore.data.first()[PENDING_SIGN_OUT_KEY] == true
    }

    val authSession: Flow<AuthSession?> = dataStore.data.map { preferences ->
        val sessionJson = preferences[AUTH_SESSION_KEY]
        if (sessionJson != null) {
            try {
                json.decodeFromString<AuthSession>(sessionJson)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val isLoggedIn: Flow<Boolean> = authSession.map { it != null }

    suspend fun saveSession(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[AUTH_SESSION_KEY] = json.encodeToString(session)
        }
    }

    suspend fun clearSession() {
        deleteSession()
    }
}
