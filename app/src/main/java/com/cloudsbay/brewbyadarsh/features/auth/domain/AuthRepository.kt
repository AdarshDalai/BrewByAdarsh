package com.cloudsbay.brewbyadarsh.features.auth.domain

import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthSession
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * Get current session
     */
    suspend fun getSession(): AuthResult<AuthSession?>

    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String): AuthResult<AuthUser>

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): AuthResult<AuthSession>

    /**
     * Sign in with OAuth provider
     */
    suspend fun signInWithOAuth(provider: String): AuthResult<AuthSession>

    /**
     * Sign out
     */
    suspend fun signOut(): AuthResult<Unit>

    /**
     * Reset password - sends recovery email
     */
    suspend fun resetPassword(email: String): AuthResult<Unit>

    /**
     * Update password with token
     */
    suspend fun updatePassword(password: String): AuthResult<Unit>

    /**
     * Update user metadata
     */
    suspend fun updateUserMetadata(metadata: Map<String, String>): AuthResult<AuthUser>

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): AuthResult<AuthUser?>

    /**
     * Refresh session
     */
    suspend fun refreshSession(): AuthResult<AuthSession>

    /**
     * Verify OTP (One-Time Password)
     */
    suspend fun verifyOtp(email: String, token: String, type: String): AuthResult<AuthSession>

    /**
     * Resend verification email
     */
    suspend fun resendVerificationEmail(email: String): AuthResult<Unit>

    /**
     * Listen to authentication state changes as a Flow
     */
    fun observeAuthState(): Flow<AuthUser?>

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Get access token
     */
    suspend fun getAccessToken(): String?
}
